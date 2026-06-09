package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.ui.components.softindigo.Badge
import Smart.Campus.PWR.ui.components.softindigo.BadgeTone
import Smart.Campus.PWR.ui.components.softindigo.CardQ
import Smart.Campus.PWR.ui.components.softindigo.SectionHead
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.components.softindigo.SoftCard
import Smart.Campus.PWR.ui.components.softindigo.SoftDivider
import Smart.Campus.PWR.ui.components.softindigo.SoftIconButton
import Smart.Campus.PWR.ui.components.softindigo.SoftTopBar
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary100
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.SoftType
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Public entry point ───────────────────────────────────────────────────────

@Composable
fun EarningsTab(state: SmartCampusUiState, onBack: () -> Unit) {
    val bookings = state.dashboardState.myTutorBookings

    // ── Real data derived from myTutorBookings ────────────────────────────────
    val totalSessions = bookings.size
    val completedSessions = bookings.count { it.status == "completed" }

    // Estimate total hours from start/end strings (HH:mm)
    val estimatedHours = bookings
        .filter { it.status != "cancelled" }
        .sumOf { booking ->
            val startH = booking.startHour.split(":").getOrNull(0)?.toIntOrNull()
            val startM = booking.startHour.split(":").getOrNull(1)?.toIntOrNull()
            val endH   = booking.endHour.split(":").getOrNull(0)?.toIntOrNull()
            val endM   = booking.endHour.split(":").getOrNull(1)?.toIntOrNull()
            if (startH != null && startM != null && endH != null && endM != null) {
                val durationMins = (endH * 60 + endM) - (startH * 60 + startM)
                if (durationMins > 0) durationMins.toDouble() / 60.0 else 0.0
            } else {
                0.0
            }
        }
    val hoursLabel = if (estimatedHours > 0) {
        val rounded = Math.round(estimatedHours * 2).toDouble() / 2.0
        if (rounded == rounded.toLong().toDouble()) "${rounded.toLong()}h" else "${rounded}h"
    } else {
        "${totalSessions}h" // fallback: 1h per session
    }

    // Group by subject for the breakdown — real counts, placeholder zł amounts
    val bySubject = bookings
        .filter { it.status != "cancelled" }
        .groupBy { it.subject.ifBlank { "Other" } }
        .entries
        .sortedByDescending { it.value.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        SoftTopBar(
            title = "Earnings",
            leading = {
                SoftIconButton(icon = SoftIcons.back, onClick = onBack)
            },
            actions = {
                SoftIconButton(icon = SoftIcons.more, onClick = { /* DESIGN-PLACEHOLDER: no action */ })
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Summary card ──────────────────────────────────────────────────
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "March 2026",
                        style = SoftType.meta
                    )
                    Spacer(Modifier.height(4.dp))
                    // DESIGN-PLACEHOLDER: no payment model — static total
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "1 847,",
                            style = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 38.sp,
                                color = InkToken
                            )
                        )
                        Text(
                            text = "50 zł",
                            style = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = InkToken
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // DESIGN-PLACEHOLDER: no payment model — static % change
                        Badge(
                            text = "+22%",
                            tone = BadgeTone.Green,
                            leadingIcon = SoftIcons.trend
                        )
                        Text(
                            text = "vs February · $totalSessions sessions",
                            style = SoftType.meta
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    // DESIGN-PLACEHOLDER: static bar chart data — no payment time-series
                    Bars(
                        data = listOf(12, 9, 18, 14, 22, 17, 28, 24, 31, 27, 36, 41),
                        peak = 11,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Mar 1", style = SoftType.meta.copy(fontSize = 11.sp))
                        Text(text = "Mar 11", style = SoftType.meta.copy(fontSize = 11.sp))
                    }
                }
            }

            // ── Next payout card ──────────────────────────────────────────────
            item {
                CardQ(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Next payout",
                                style = SoftType.meta.copy(fontSize = 11.5.sp)
                            )
                            Spacer(Modifier.height(2.dp))
                            // DESIGN-PLACEHOLDER: no payment model — static payout amount
                            Text(
                                text = "1 392,50 zł",
                                style = TextStyle(
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = InkToken
                                )
                            )
                            // DESIGN-PLACEHOLDER: no payment model — static bank details
                            Text(
                                text = "to mBank ···· 4421",
                                style = SoftType.meta.copy(fontSize = 12.sp)
                            )
                        }
                        // DESIGN-PLACEHOLDER: no payment model — static payout date
                        Badge(text = "Fri · Mar 14", tone = BadgeTone.Prim)
                    }
                    Spacer(Modifier.height(14.dp))
                    // DESIGN-PLACEHOLDER: no payment model — no early withdrawal backend
                    SoftButton(
                        text = "Withdraw early",
                        onClick = { /* DESIGN-PLACEHOLDER: no payment model */ },
                        variant = SoftButtonVariant.Soft,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── Breakdown ─────────────────────────────────────────────────────
            item {
                SectionHead(title = "Breakdown")
            }

            item {
                CardQ(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 6.dp
                ) {
                    val breakdownRows: List<Triple<String, String, String>> = if (bySubject.isNotEmpty()) {
                        // Real subject/lesson counts; amounts are DESIGN-PLACEHOLDERs
                        bySubject.map { (subject, lessons) ->
                            val lessonCount = lessons.size
                            // DESIGN-PLACEHOLDER: no payment model — flat 45 zł/session rate
                            val amount = lessonCount * 45
                            Triple(subject, "$lessonCount lesson${if (lessonCount != 1) "s" else ""}", "$amount zł")
                        }
                    } else {
                        // DESIGN-PLACEHOLDER: no real data — static design reference rows
                        listOf(
                            Triple("Calculus I & II", "24 lessons", "1 080 zł"),
                            Triple("Lin. Algebra", "11 lessons", "495 zł"),
                            Triple("Real Analysis", "4 lessons", "180 zł")
                        )
                    }

                    // Cancelled row — DESIGN-PLACEHOLDER: no payment model
                    val cancelledCount = bookings.count { it.status == "cancelled" }

                    val allRows = buildList {
                        addAll(breakdownRows)
                        if (cancelledCount > 0) {
                            // DESIGN-PLACEHOLDER: no payment model — static deduction per cancellation
                            val deduction = cancelledCount * 46
                            add(Triple("Cancelled / no-show", "$cancelledCount", "– ${deduction},50 zł"))
                        } else {
                            // DESIGN-PLACEHOLDER: static row shown even when no real cancellations
                            add(Triple("Cancelled / no-show", "2", "– 92,50 zł"))
                        }
                    }

                    allRows.forEachIndexed { index, (label, sublabel, amount) ->
                        val isCancelled = label == "Cancelled / no-show"
                        val amountColor = if (isCancelled) Red else InkToken

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = InkToken
                                    )
                                )
                                Text(
                                    text = sublabel,
                                    style = SoftType.meta.copy(fontSize = 11.5.sp)
                                )
                            }
                            Text(
                                text = amount,
                                style = TextStyle(
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = amountColor
                                )
                            )
                        }

                        if (index < allRows.size - 1) {
                            SoftDivider()
                        }
                    }
                }
            }
        }
    }
}

// ─── Bars composable — local bar chart, heights from static data ──────────────

/**
 * Simple bar chart — a Row of rounded-top bars.
 * Peak bar rendered in Primary, others in Primary100.
 *
 * @param data   List of relative values; max value maps to full height.
 * @param peak   Index of the bar to highlight in Primary (others use Primary100).
 *
 * Heights and data values are DESIGN-PLACEHOLDER: no payment time-series in the backend.
 */
@Composable
private fun Bars(
    data: List<Int>,
    peak: Int,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOrNull()?.coerceAtLeast(1) ?: 1
    val chartHeight = 64.dp

    Box(
        modifier = modifier.height(chartHeight),
        contentAlignment = Alignment.BottomStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, value ->
                val fraction = value.toFloat() / maxVal.toFloat()
                val barColor = if (index == peak) Primary else Primary100
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(fraction)
                        .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                        .background(barColor)
                )
            }
        }
    }
}
