package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.chat.AttachmentContract
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftIconButton
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.CourseMaterialUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Line
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary50
import Smart.Campus.PWR.ui.theme.Red
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Smart.Campus.PWR.ui.util.readPickedFile

@Composable
fun CourseMaterialsScreen(
    state: SmartCampusUiState,
    onBack: () -> Unit,
    onUpload: (uri: String, fileName: String, mimeType: String, sizeBytes: Long) -> Unit,
    onDelete: (String) -> Unit
) {
    val chat = state.chat
    val context = LocalContext.current
    val myUid = state.currentUser?.uid.orEmpty()
    var pickError by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<CourseMaterialUi?>(null) }
    val isUploading = chat.materialUploadProgress != null

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val file = context.readPickedFile(uri)
            val error = if (file == null) {
                "Could not read selected file."
            } else {
                AttachmentContract.validationError(file.fileName, file.mimeType, file.sizeBytes)
            }
            if (file != null && error == null) {
                pickError = null
                onUpload(uri.toString(), file.fileName, file.mimeType, file.sizeBytes)
            } else {
                pickError = error
            }
        }
    }

    pendingDelete?.let { material ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete file?") },
            text = { Text("\"${material.fileName}\" will be removed for everyone in this course.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(material.id)
                    pendingDelete = null
                }) { Text("Delete", color = Red) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
            containerColor = CardSurface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SoftIconButton(icon = SoftIcons.back, onClick = onBack)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Materials",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = InkToken
                    )
                )
                Text(
                    text = chat.activeTitle,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp,
                        color = Ink3
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SoftButton(
                text = if (isUploading) "Uploading…" else "Upload",
                onClick = {
                    if (!isUploading) launcher.launch(AttachmentContract.allowedMimeTypes.toTypedArray())
                },
                enabled = !isUploading
            )
        }

        chat.materialUploadProgress?.let { progress ->
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = Primary
            )
        }

        (pickError ?: state.errorMessage)?.let { error ->
            Text(
                text = error,
                color = Red,
                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.5.sp),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )
        }

        // ── File list ────────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when {
                chat.materialsLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                }

                chat.materials.isEmpty() -> Text(
                    text = "No files yet. Upload notes, exercises or any course files — everyone in this course can see them.",
                    color = Ink3,
                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp)
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(chat.materials.size) { index ->
                        val material = chat.materials[index]
                        val canDelete = material.uploaderUid == myUid || chat.activeIsOwner
                        MaterialRow(
                            material = material,
                            canDelete = canDelete,
                            onOpen = { openMaterial(context, material) },
                            onDelete = { pendingDelete = material }
                        )
                        if (index < chat.materials.lastIndex) {
                            HorizontalDivider(
                                color = Line,
                                modifier = Modifier.padding(start = 66.dp, end = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun MaterialRow(
    material: CourseMaterialUi,
    canDelete: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Primary50),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (material.isImage) Icons.Rounded.Image else Icons.AutoMirrored.Rounded.InsertDriveFile,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = material.fileName,
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = InkToken
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOf(material.sizeLabel, material.uploaderName, material.createdAtLabel)
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontSize = 11.5.sp,
                    color = Ink3
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (canDelete) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete file",
                    tint = Ink3,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun openMaterial(context: android.content.Context, material: CourseMaterialUi) {
    if (material.downloadUrl.isBlank()) return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(Uri.parse(material.downloadUrl), material.mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(intent) }
}
