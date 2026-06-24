package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.chat.AttachmentContract
import Smart.Campus.PWR.ui.components.softindigo.Badge
import Smart.Campus.PWR.ui.components.softindigo.BadgeTone
import Smart.Campus.PWR.ui.components.softindigo.InitialsAvatar
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.components.softindigo.SoftCard
import Smart.Campus.PWR.ui.components.softindigo.SoftDivider
import Smart.Campus.PWR.ui.components.softindigo.SoftIconButton
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.CourseMaterialUi
import Smart.Campus.PWR.ui.state.LessonBookingUi
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
import Smart.Campus.PWR.ui.theme.SoftType
import Smart.Campus.PWR.ui.util.buildAddToCalendarIntent
import Smart.Campus.PWR.ui.util.readPickedFile
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun LessonDetailScreen(
    state: SmartCampusUiState,
    onBack: () -> Unit,
    onUpload: (uri: String, fileName: String, mimeType: String, sizeBytes: Long) -> Unit,
    onDelete: (String) -> Unit,
    onMessage: (uid: String, name: String) -> Unit
) {
    val context = LocalContext.current
    val ds = state.dashboardState
    val myUid = state.currentUser?.uid.orEmpty()
    val bookingId = ds.lessonDetailBookingId
    val booking = remember(bookingId, ds.myStudentBookings, ds.myTutorBookings) {
        (ds.myStudentBookings + ds.myTutorBookings).firstOrNull { it.id == bookingId }
    }
    val amTutor = booking != null && booking.tutorId == myUid

    var pickError by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<CourseMaterialUi?>(null) }
    val isUploading = ds.lessonMaterialUploadProgress != null

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
            text = { Text("\"${material.fileName}\" will be removed from this lesson.") },
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
            Text(
                text = "Lesson",
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = InkToken
                )
            )
        }

        if (booking == null) {
            Text(
                text = "This lesson is no longer available.",
                color = Ink3,
                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            )
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            LessonInfoCard(booking)

            Spacer(Modifier.height(14.dp))

            // Add to calendar
            val beginMillis = lessonEpochMillis(booking.dateLabel, booking.startHour)
            val endMillis = lessonEpochMillis(booking.dateLabel, booking.endHour)
            SoftButton(
                text = "Add to calendar",
                onClick = {
                    if (beginMillis != null && endMillis != null) {
                        val location = when {
                            booking.format == "IN_PERSON" && booking.location.isNotBlank() -> booking.location
                            booking.format == "ONLINE" && booking.meetingUrl.isNotBlank() -> booking.meetingUrl
                            else -> ""
                        }
                        val intent = buildAddToCalendarIntent(
                            title = "Tutoring: ${booking.subject} with ${booking.tutorDisplayName}",
                            beginMillis = beginMillis,
                            endMillis = endMillis,
                            location = location,
                            description = if (booking.topic.isNotBlank()) "Topic: ${booking.topic}" else ""
                        )
                        runCatching { context.startActivity(intent) }
                    }
                },
                variant = SoftButtonVariant.Primary,
                enabled = beginMillis != null && endMillis != null,
                trailingIcon = SoftIcons.calendar,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            SoftButton(
                text = if (amTutor) "Message student" else "Message tutor",
                onClick = {
                    if (amTutor) onMessage(booking.studentId, booking.studentDisplayName)
                    else onMessage(booking.tutorId, booking.tutorDisplayName)
                },
                variant = SoftButtonVariant.Outline,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(22.dp))

            // ── Files ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Files",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Ink3,
                        letterSpacing = 0.5.sp
                    )
                )
                SoftButton(
                    text = if (isUploading) "Uploading…" else "Upload",
                    onClick = {
                        if (!isUploading) launcher.launch(AttachmentContract.allowedMimeTypes.toTypedArray())
                    },
                    variant = SoftButtonVariant.Soft,
                    leadingIcon = SoftIcons.plus,
                    enabled = !isUploading
                )
            }

            ds.lessonMaterialUploadProgress?.let { progress ->
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Primary
                )
            }

            pickError?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error,
                    color = Red,
                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.5.sp)
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                ds.lessonMaterialsLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                }

                ds.lessonMaterials.isEmpty() -> Text(
                    text = "No files yet. Upload notes, exercises or recordings — both you and the other person can see them.",
                    color = Ink3,
                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

                else -> Column {
                    ds.lessonMaterials.forEachIndexed { index, material ->
                        LessonMaterialRow(
                            material = material,
                            canDelete = material.uploaderUid == myUid || amTutor,
                            onOpen = { openLessonFile(context, material) },
                            onDelete = { pendingDelete = material }
                        )
                        if (index < ds.lessonMaterials.lastIndex) {
                            SoftDivider()
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LessonInfoCard(booking: LessonBookingUi) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = booking.subject,
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = InkToken
                )
            )
            LessonStatusBadge(booking.status.uppercase())
        }

        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InitialsAvatar(name = booking.tutorDisplayName, size = 26.dp)
            Text(
                text = booking.tutorDisplayName,
                style = SoftType.bodySm,
                maxLines = 1
            )
        }

        Spacer(Modifier.height(12.dp))
        SoftDivider()
        Spacer(Modifier.height(12.dp))

        DetailLine(label = "Date", value = booking.dateLabel)
        DetailLine(label = "Time", value = "${booking.startHour} – ${booking.endHour}")
        DetailLine(
            label = "Format",
            value = if (booking.format == "IN_PERSON") "In person" else "Online"
        )
        if (booking.format == "IN_PERSON" && booking.location.isNotBlank()) {
            DetailLine(label = "Location", value = booking.location)
        }
        if (booking.format == "ONLINE" && booking.meetingUrl.isNotBlank()) {
            DetailLine(label = "Meeting link", value = booking.meetingUrl)
        }
        if (booking.topic.isNotBlank()) {
            DetailLine(label = "Topic", value = booking.topic)
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.size(width = 92.dp, height = 20.dp),
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Ink3
            )
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = InkToken
            )
        )
    }
}

@Composable
private fun LessonStatusBadge(statusNorm: String) {
    when (statusNorm) {
        "PENDING" -> Badge(text = "Pending", tone = BadgeTone.Amber)
        "CONFIRMED", "BOOKED" -> Badge(text = "Confirmed", tone = BadgeTone.Green)
        "COMPLETED" -> Badge(text = "Completed", tone = BadgeTone.Green)
        "CANCELLED", "DECLINED" -> Badge(text = "Cancelled", tone = BadgeTone.Red)
        "NO_SHOW" -> Badge(text = "No-show", tone = BadgeTone.Red)
        else -> Badge(text = statusNorm.lowercase().replaceFirstChar { it.uppercase() }, tone = BadgeTone.Gray)
    }
}

@Composable
private fun LessonMaterialRow(
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
            .padding(vertical = 10.dp),
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

private fun openLessonFile(context: Context, material: CourseMaterialUi) {
    if (material.downloadUrl.isBlank()) return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(Uri.parse(material.downloadUrl), material.mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(intent) }
}

private fun lessonEpochMillis(dateLabel: String, hour: String): Long? {
    val date = runCatching { LocalDate.parse(dateLabel) }.getOrNull() ?: return null
    val time = runCatching {
        LocalTime.parse(hour, DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrNull() ?: return null
    return LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
