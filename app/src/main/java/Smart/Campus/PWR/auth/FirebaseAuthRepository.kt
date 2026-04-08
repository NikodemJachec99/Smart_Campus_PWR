package Smart.Campus.PWR.auth

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FirebaseAuthRepository"
        private const val AUTH_TIMEOUT_MS = 15_000L
        private const val LOGIN_DOMAIN = "smartcampus.local"
    }

    private var preferredFunctionsRegion: String = "europe-west1"

    suspend fun signIn(loginOrEmail: String, password: String): AppUser {
        val mappedEmail = AuthMapping.aliasToEmail(loginOrEmail)

        return try {
            withTimeout(AUTH_TIMEOUT_MS) {
                try {
                    auth.signInWithEmailAndPassword(mappedEmail, password).await()
                } catch (error: Throwable) {
                    Log.e(TAG, "Sign in failed for $mappedEmail", error)
                    throw error
                }

                requireNotNull(getCurrentUser()) {
                    "Logged user profile is missing."
                }
            }
        } catch (error: TimeoutCancellationException) {
            Log.e(TAG, "Sign in timed out for $mappedEmail", error)
            auth.signOut()
            throw error
        }
    }

    suspend fun register(
        loginOrEmail: String,
        password: String,
        displayName: String,
        student: Boolean,
        tutor: Boolean
    ): AppUser {
        require(student || tutor) { "Select at least one role." }

        val login = AuthMapping.normalizeLogin(loginOrEmail)
        require(login.isNotBlank()) { "Login is required." }
        require(password.length >= 6) { "Password must be at least 6 characters." }

        val email = AuthMapping.aliasToEmail(login)
        val safeDisplayName = displayName.trim().ifBlank { login }

        withTimeout(AUTH_TIMEOUT_MS) {
            auth.createUserWithEmailAndPassword(email, password).await()
        }

        val firebaseUser = auth.currentUser ?: throw IllegalStateException("User has not been created.")
        firebaseUser.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(safeDisplayName)
                .build()
        ).await()

        val roles = buildList {
            if (student) add("student")
            if (tutor) add("tutor")
        }

        firestore.collection("users").document(firebaseUser.uid).set(
            mapOf(
                "login" to login,
                "loginLowercase" to login,
                "email" to email,
                "displayName" to safeDisplayName,
                "roles" to roles,
                "isActive" to true,
                "createdBy" to firebaseUser.uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()

        return requireNotNull(getCurrentUser()) {
            "Registered user profile is missing."
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getCurrentUser(): AppUser? {
        val firebaseUser = auth.currentUser ?: return null
        val uid = firebaseUser.uid

        val firestoreUser = try {
            withTimeout(AUTH_TIMEOUT_MS) {
                getUserByUid(uid)
            }
        } catch (_: Exception) {
            null
        }

        if (firestoreUser != null) {
            return firestoreUser
        }

        val claims = firebaseUser.getIdToken(false).await().claims
        val fallbackRoles = buildSet {
            if (claims["admin"] == true) add(UserRole.ADMIN)
            if (claims["student"] == true) add(UserRole.STUDENT)
            if (claims["tutor"] == true || claims["lecturer"] == true) add(UserRole.TUTOR)
        }

        return AppUser(
            uid = uid,
            login = AuthMapping.loginFromEmail(firebaseUser.email.orEmpty()),
            email = firebaseUser.email.orEmpty(),
            displayName = firebaseUser.displayName ?: AuthMapping.loginFromEmail(firebaseUser.email.orEmpty()),
            roles = fallbackRoles,
            isActive = true,
            avatarUrl = firebaseUser.photoUrl?.toString()
        )
    }

    suspend fun listUsers(): List<AppUser> {
        val snapshot = try {
            firestore.collection("users").orderBy("loginLowercase").get().await()
        } catch (_: Exception) {
            firestore.collection("users").get().await()
        }

        return snapshot.documents
            .mapNotNull { doc -> docToAppUser(doc.data, doc.id) }
            .sortedBy { user -> user.login }
    }

    suspend fun adminCreateUser(
        login: String,
        password: String,
        displayName: String,
        admin: Boolean,
        student: Boolean,
        tutor: Boolean
    ): AppUser {
        val normalizedLogin = AuthMapping.normalizeLogin(login)

        return try {
            val data = callAdminFunction(
                functionName = "adminCreateUser",
                payload = mapOf(
                    "login" to normalizedLogin,
                    "password" to password,
                    "displayName" to displayName,
                    "roles" to mapOf(
                        "admin" to admin,
                        "student" to student,
                        "tutor" to tutor
                    )
                )
            ) as? Map<*, *> ?: throw IllegalStateException("Invalid backend response.")

            val uid = data["uid"] as? String
                ?: throw IllegalStateException("Missing uid in backend response.")

            requireNotNull(getUserByUid(uid)) {
                "User created but profile document is missing."
            }
        } catch (error: Throwable) {
            if (!shouldUseClientFallback(error)) {
                throw error
            }

            Log.w(TAG, "Functions unavailable, using client fallback for adminCreateUser", error)
            createUserViaIdentityToolkitAndFirestore(
                normalizedLogin = normalizedLogin,
                password = password,
                displayName = displayName,
                admin = admin,
                student = student,
                tutor = tutor
            )
        }
    }

    suspend fun adminUpdateUserRoles(uid: String, student: Boolean, tutor: Boolean): Set<UserRole> {
        return try {
            val data = callAdminFunction(
                functionName = "adminUpdateUserRoles",
                payload = mapOf(
                    "uid" to uid,
                    "roles" to mapOf(
                        "student" to student,
                        "tutor" to tutor
                    )
                )
            ) as? Map<*, *> ?: throw IllegalStateException("Invalid backend response.")

            val rolesRaw = data["roles"]
            AuthMapping.parseRoles(rolesRaw, null)
        } catch (error: Throwable) {
            if (!shouldUseClientFallback(error)) {
                throw error
            }

            Log.w(TAG, "Functions unavailable, using client fallback for adminUpdateUserRoles", error)
            updateUserRolesViaFirestore(uid, student, tutor)
        }
    }

    suspend fun getUserByUid(uid: String): AppUser? {
        val snapshot = firestore.collection("users").document(uid).get().await()
        if (!snapshot.exists()) {
            return null
        }

        return docToAppUser(snapshot.data, uid)
    }

    fun userMessage(error: Throwable): String {
        return when (error) {
            is IllegalArgumentException -> error.message ?: "Invalid input."
            is TimeoutCancellationException -> "Sign-in timed out. Check emulator, Google Play Services, or try a physical device."
            is FirebaseAuthInvalidCredentialsException -> "Invalid login or password."
            is FirebaseAuthInvalidUserException -> "User does not exist in Firebase Auth."
            is FirebaseAuthException -> when (error.errorCode) {
                "ERROR_INVALID_LOGIN_CREDENTIALS", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
                    "Invalid login or password."
                "ERROR_USER_NOT_FOUND" -> "User does not exist in Firebase Auth."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "User with this login already exists."
                "ERROR_NETWORK_REQUEST_FAILED" -> "Network issue or Google Play Services problem."
                "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try again shortly."
                else -> "Firebase auth error: ${error.errorCode}"
            }
            is FirebaseFunctionsException -> when (error.code) {
                FirebaseFunctionsException.Code.NOT_FOUND -> "Backend function not found. Admin fallback is active."
                FirebaseFunctionsException.Code.PERMISSION_DENIED -> "No admin permission for this operation."
                FirebaseFunctionsException.Code.UNAUTHENTICATED -> "Session expired. Sign in again."
                FirebaseFunctionsException.Code.UNAVAILABLE -> "Functions backend is temporarily unavailable."
                else -> "Functions error: ${error.code.name}"
            }
            is FirebaseFirestoreException -> "Firestore error: ${error.code.name}"
            else -> error.message ?: "Unexpected error occurred."
        }
    }

    private fun shouldUseClientFallback(error: Throwable): Boolean {
        if (error is FirebaseFunctionsException) {
            return error.code == FirebaseFunctionsException.Code.NOT_FOUND ||
                error.code == FirebaseFunctionsException.Code.UNAVAILABLE
        }

        val message = error.message?.uppercase().orEmpty()
        return message.contains("NOT_FOUND") || message.contains("UNAVAILABLE")
    }

    private suspend fun createUserViaIdentityToolkitAndFirestore(
        normalizedLogin: String,
        password: String,
        displayName: String,
        admin: Boolean,
        student: Boolean,
        tutor: Boolean
    ): AppUser {
        if (password.length < 6) {
            throw IllegalArgumentException("Password must be at least 6 characters.")
        }

        val roles = mutableListOf<String>()
        if (admin) roles.add("admin")
        if (student) roles.add("student")
        if (tutor) roles.add("tutor")

        if (roles.isEmpty()) {
            throw IllegalArgumentException("Select at least one role.")
        }

        val email = "$normalizedLogin@$LOGIN_DOMAIN"
        val uid = createEmailPasswordUserViaRest(email, password, displayName.ifBlank { normalizedLogin })

        val creatorUid = auth.currentUser?.uid ?: "admin-panel"
        val userDoc = mapOf(
            "login" to normalizedLogin,
            "loginLowercase" to normalizedLogin,
            "email" to email,
            "displayName" to displayName.ifBlank { normalizedLogin },
            "roles" to roles,
            "isActive" to true,
            "createdBy" to creatorUid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users").document(uid).set(userDoc, SetOptions.merge()).await()

        return requireNotNull(getUserByUid(uid)) {
            "User created, but Firestore profile is missing."
        }
    }

    private suspend fun updateUserRolesViaFirestore(uid: String, student: Boolean, tutor: Boolean): Set<UserRole> {
        if (!student && !tutor) {
            throw IllegalArgumentException("User must have at least one non-admin role.")
        }

        val userRef = firestore.collection("users").document(uid)
        val snapshot = userRef.get().await()
        if (!snapshot.exists()) {
            throw IllegalStateException("User profile does not exist in Firestore.")
        }

        val existingRoles = AuthMapping.parseRoles(snapshot.get("roles"), snapshot.getString("role"))
        if (existingRoles.contains(UserRole.ADMIN)) {
            throw IllegalStateException("Admin role cannot be changed from this panel.")
        }

        val roles = buildList {
            if (student) add("student")
            if (tutor) add("tutor")
        }

        userRef.set(
            mapOf(
                "roles" to roles,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()

        return AuthMapping.parseRoles(roles, null)
    }

    private suspend fun createEmailPasswordUserViaRest(email: String, password: String, displayName: String): String {
        val apiKey = FirebaseApp.getInstance().options.apiKey
            ?: throw IllegalStateException("Missing Firebase API key in app configuration.")

        val endpoint = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey"
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
            .put("displayName", displayName)
            .put("returnSecureToken", true)
            .toString()

        return withContext(Dispatchers.IO) {
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = AUTH_TIMEOUT_MS.toInt()
            connection.readTimeout = AUTH_TIMEOUT_MS.toInt()
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(body)
            }

            val status = connection.responseCode
            val responseBody = try {
                if (status in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                }
            } finally {
                connection.disconnect()
            }

            if (status !in 200..299) {
                throw IllegalStateException(mapIdentityToolkitError(responseBody))
            }

            val json = JSONObject(responseBody)
            val uid = json.optString("localId")
            if (uid.isBlank()) {
                throw IllegalStateException("Identity Toolkit response missing localId.")
            }

            uid
        }
    }

    private fun mapIdentityToolkitError(rawResponse: String): String {
        return try {
            val root = JSONObject(rawResponse)
            val message = root.optJSONObject("error")?.optString("message") ?: "UNKNOWN"
            when (message) {
                "EMAIL_EXISTS" -> "User with this login/email already exists."
                "INVALID_PASSWORD" -> "Password is invalid."
                "WEAK_PASSWORD : Password should be at least 6 characters" -> "Password must be at least 6 characters."
                else -> "User creation error: $message"
            }
        } catch (_: Exception) {
            "User creation error."
        }
    }

    private suspend fun callAdminFunction(functionName: String, payload: Map<String, Any>): Any? {
        val regionsToTry = buildList {
            add(preferredFunctionsRegion)
            if (!contains("europe-west1")) add("europe-west1")
            if (!contains("us-central1")) add("us-central1")
        }

        var lastError: Throwable? = null

        for (region in regionsToTry) {
            try {
                val functions = FirebaseFunctions.getInstance(region)
                val result = functions.getHttpsCallable(functionName).call(payload).await()
                preferredFunctionsRegion = region
                return result.data
            } catch (error: Throwable) {
                val isNotFound = error is FirebaseFunctionsException && error.code == FirebaseFunctionsException.Code.NOT_FOUND
                if (isNotFound) {
                    lastError = error
                    continue
                }

                throw error
            }
        }

        throw lastError ?: IllegalStateException("Unable to call backend function: $functionName")
    }

    private fun docToAppUser(data: Map<String, Any?>?, fallbackUid: String): AppUser? {
        if (data == null) {
            return null
        }

        val email = (data["email"] as? String).orEmpty()
        val login = (data["login"] as? String)
            ?: (data["loginLowercase"] as? String)
            ?: if (email.isNotBlank()) AuthMapping.loginFromEmail(email) else fallbackUid

        val roles = AuthMapping.parseRoles(data["roles"], data["role"] as? String)
        val displayName = (data["displayName"] as? String).orEmpty().ifBlank { login }
        val isActive = data["isActive"] as? Boolean ?: true
        val avatarUrl = (data["avatarUrl"] as? String)
            ?: (data["photoUrl"] as? String)

        return AppUser(
            uid = fallbackUid,
            login = login,
            email = email,
            displayName = displayName,
            roles = roles,
            isActive = isActive,
            avatarUrl = avatarUrl
        )
    }
}