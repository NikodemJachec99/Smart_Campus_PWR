package Smart.Campus.PWR.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("europe-west1")
) {
    suspend fun signIn(loginOrEmail: String, password: String): AppUser {
        val mappedEmail = AuthMapping.aliasToEmail(loginOrEmail)
        auth.signInWithEmailAndPassword(mappedEmail, password).await()

        return requireNotNull(getCurrentUser()) {
            "Logged user profile is missing."
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getCurrentUser(): AppUser? {
        val firebaseUser = auth.currentUser ?: return null
        val uid = firebaseUser.uid

        val firestoreUser = try {
            getUserByUid(uid)
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
            if (claims["lecturer"] == true) add(UserRole.LECTURER)
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
        lecturer: Boolean
    ): AppUser {
        val result = functions
            .getHttpsCallable("adminCreateUser")
            .call(
                mapOf(
                    "login" to AuthMapping.normalizeLogin(login),
                    "password" to password,
                    "displayName" to displayName,
                    "roles" to mapOf(
                        "admin" to admin,
                        "student" to student,
                        "lecturer" to lecturer
                    )
                )
            )
            .await()

        val data = result.data as? Map<*, *>
            ?: throw IllegalStateException("Invalid backend response.")

        val uid = data["uid"] as? String
            ?: throw IllegalStateException("Missing uid in backend response.")

        return requireNotNull(getUserByUid(uid)) {
            "User created but profile document is missing."
        }
    }

    suspend fun adminUpdateUserRoles(uid: String, student: Boolean, lecturer: Boolean): Set<UserRole> {
        val result = functions
            .getHttpsCallable("adminUpdateUserRoles")
            .call(
                mapOf(
                    "uid" to uid,
                    "roles" to mapOf(
                        "student" to student,
                        "lecturer" to lecturer
                    )
                )
            )
            .await()

        val data = result.data as? Map<*, *>
            ?: throw IllegalStateException("Invalid backend response.")
        val rolesRaw = data["roles"]

        return AuthMapping.parseRoles(rolesRaw, null)
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
            is FirebaseAuthInvalidCredentialsException -> "Nieprawidlowe haslo lub login."
            is FirebaseAuthInvalidUserException -> "Uzytkownik nie istnieje."
            is FirebaseFirestoreException -> "Blad Firestore: ${error.code.name}"
            else -> error.message ?: "Wystapil nieoczekiwany blad."
        }
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
