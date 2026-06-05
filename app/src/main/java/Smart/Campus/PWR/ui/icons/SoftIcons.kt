package Smart.Campus.PWR.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// ─── Soft Indigo icon set — ported from shared2.jsx `const I = { ... }` ───

private fun icon(
    name: String,
    build: ImageVector.Builder.() -> Unit
): ImageVector = ImageVector.Builder(
    name = name, defaultWidth = 24.dp, defaultHeight = 24.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply(build).build()

object SoftIcons {

    // search: stroke icon — circle(cx=11,cy=11,r=7) + line
    val search: ImageVector by lazy {
        icon("search") {
            // circle cx=11 cy=11 r=7
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(11f, 4f)         // cy - r = 11 - 7 = 4
                arcToRelative(7f, 7f, 0f, true, true, 0f, 14f)
                arcToRelative(7f, 7f, 0f, true, true, 0f, -14f)
                close()
            }
            // line: m20.5 20.5-3.6-3.6  => moveToRelative(20.5,20.5) lineToRelative(-3.6,-3.6)
            // But 'm' at the start of a path is absolute moveTo then relative lineTo
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(20.5f, 20.5f)
                lineToRelative(-3.6f, -3.6f)
            }
        }
    }

    // bell: stroke icon — bell body + arc for clapper
    val bell: ImageVector by lazy {
        icon("bell") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M6 16V11a6 6 0 0 1 12 0v5l1.6 2.2a.5.5 0 0 1-.4.8H4.8a.5.5 0 0 1-.4-.8L6 16Z
                moveTo(6f, 16f)
                verticalLineTo(11f)
                arcToRelative(6f, 6f, 0f, false, true, 12f, 0f)
                verticalLineToRelative(5f)
                lineToRelative(1.6f, 2.2f)
                arcToRelative(0.5f, 0.5f, 0f, false, true, -0.4f, 0.8f)
                horizontalLineTo(4.8f)
                arcToRelative(0.5f, 0.5f, 0f, false, true, -0.4f, -0.8f)
                lineTo(6f, 16f)
                close()
            }
            // M9.5 20a2.5 2.5 0 0 0 5 0
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9.5f, 20f)
                arcToRelative(2.5f, 2.5f, 0f, false, false, 5f, 0f)
            }
        }
    }

    // back: stroke icon — chevron left
    val back: ImageVector by lazy {
        icon("back") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m14 6-6 6 6 6
                moveTo(14f, 6f)
                lineToRelative(-6f, 6f)
                lineToRelative(6f, 6f)
            }
        }
    }

    // more: solid — three filled circles
    val more: ImageVector by lazy {
        icon("more") {
            // circle cx=5.5 cy=12 r=1.6
            path(fill = SolidColor(Color.Black)) {
                moveTo(5.5f, 12f - 1.6f)
                arcToRelative(1.6f, 1.6f, 0f, true, true, 0f, 3.2f)
                arcToRelative(1.6f, 1.6f, 0f, true, true, 0f, -3.2f)
                close()
            }
            // circle cx=12 cy=12 r=1.6
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 12f - 1.6f)
                arcToRelative(1.6f, 1.6f, 0f, true, true, 0f, 3.2f)
                arcToRelative(1.6f, 1.6f, 0f, true, true, 0f, -3.2f)
                close()
            }
            // circle cx=18.5 cy=12 r=1.6
            path(fill = SolidColor(Color.Black)) {
                moveTo(18.5f, 12f - 1.6f)
                arcToRelative(1.6f, 1.6f, 0f, true, true, 0f, 3.2f)
                arcToRelative(1.6f, 1.6f, 0f, true, true, 0f, -3.2f)
                close()
            }
        }
    }

    // filter: stroke — three horizontal lines of different widths
    val filter: ImageVector by lazy {
        icon("filter") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M5 7h14M8 12h8M11 17h2
                moveTo(5f, 7f)
                horizontalLineTo(19f)
                moveTo(8f, 12f)
                horizontalLineTo(16f)
                moveTo(11f, 17f)
                horizontalLineTo(13f)
            }
        }
    }

    // sliders: stroke — two lines with circle handles
    val sliders: ImageVector by lazy {
        icon("sliders") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M5 8h9M18 8h1M5 16h1M10 16h9
                moveTo(5f, 8f)
                horizontalLineTo(14f)
                moveTo(18f, 8f)
                horizontalLineTo(19f)
                moveTo(5f, 16f)
                horizontalLineTo(6f)
                moveTo(10f, 16f)
                horizontalLineTo(19f)
            }
            // circle cx=16 cy=8 r=2.2
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16f, 8f - 2.2f)
                arcToRelative(2.2f, 2.2f, 0f, true, true, 0f, 4.4f)
                arcToRelative(2.2f, 2.2f, 0f, true, true, 0f, -4.4f)
                close()
            }
            // circle cx=8 cy=16 r=2.2
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8f, 16f - 2.2f)
                arcToRelative(2.2f, 2.2f, 0f, true, true, 0f, 4.4f)
                arcToRelative(2.2f, 2.2f, 0f, true, true, 0f, -4.4f)
                close()
            }
        }
    }

    // star: solid — filled star shape
    val star: ImageVector by lazy {
        icon("star") {
            path(fill = SolidColor(Color.Black)) {
                // M12 3.2l2.6 5.5 6 .8-4.4 4.1 1.1 5.9L12 16.8 6.7 19.5l1.1-5.9L3.4 9.5l6-.8z
                moveTo(12f, 3.2f)
                lineToRelative(2.6f, 5.5f)
                lineToRelative(6f, 0.8f)
                lineToRelative(-4.4f, 4.1f)
                lineToRelative(1.1f, 5.9f)
                lineTo(12f, 16.8f)
                lineTo(6.7f, 19.5f)
                lineToRelative(1.1f, -5.9f)
                lineTo(3.4f, 9.5f)
                lineToRelative(6f, -0.8f)
                close()
            }
        }
    }

    // starO: stroke — outlined star
    val starO: ImageVector by lazy {
        icon("starO") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M12 3.8l2.5 5.2 5.7.8-4.1 3.9 1 5.6L12 16.6 6 19.3l1-5.6L2.8 9.8l5.7-.8z
                moveTo(12f, 3.8f)
                lineToRelative(2.5f, 5.2f)
                lineToRelative(5.7f, 0.8f)
                lineToRelative(-4.1f, 3.9f)
                lineToRelative(1f, 5.6f)
                lineTo(12f, 16.6f)
                lineTo(6f, 19.3f)
                lineToRelative(1f, -5.6f)
                lineTo(2.8f, 9.8f)
                lineToRelative(5.7f, -0.8f)
                close()
            }
        }
    }

    // home: stroke — house shape
    val home: ImageVector by lazy {
        icon("home") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M4 11.5 12 5l8 6.5V19a1.5 1.5 0 0 1-1.5 1.5H15V15a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v5.5H5.5A1.5 1.5 0 0 1 4 19Z
                moveTo(4f, 11.5f)
                lineTo(12f, 5f)
                lineToRelative(8f, 6.5f)
                verticalLineTo(19f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, 1.5f)
                horizontalLineTo(15f)
                verticalLineTo(15f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, -1f)
                horizontalLineToRelative(-4f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                verticalLineToRelative(5.5f)
                horizontalLineTo(5.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, -1.5f)
                close()
            }
        }
    }

    // compass: stroke — circle + diamond shape inside
    val compass: ImageVector by lazy {
        icon("compass") {
            // circle cx=12 cy=12 r=8.5
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 12f - 8.5f)
                arcToRelative(8.5f, 8.5f, 0f, true, true, 0f, 17f)
                arcToRelative(8.5f, 8.5f, 0f, true, true, 0f, -17f)
                close()
            }
            // m15.2 8.8-1.9 4.5-4.5 1.9 1.9-4.5z
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(15.2f, 8.8f)
                lineToRelative(-1.9f, 4.5f)
                lineToRelative(-4.5f, 1.9f)
                lineToRelative(1.9f, -4.5f)
                close()
            }
        }
    }

    // calendar: stroke — rect + lines + vertical tick marks
    val calendar: ImageVector by lazy {
        icon("calendar") {
            // rect x=3.5 y=5 width=17 height=15.5 rx=3.5
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7f, 5f)
                arcToRelative(3.5f, 3.5f, 0f, false, false, -3.5f, 3.5f)
                verticalLineTo(17f)
                arcToRelative(3.5f, 3.5f, 0f, false, false, 3.5f, 3.5f)
                horizontalLineTo(17f)
                arcToRelative(3.5f, 3.5f, 0f, false, false, 3.5f, -3.5f)
                verticalLineTo(8.5f)
                arcToRelative(3.5f, 3.5f, 0f, false, false, -3.5f, -3.5f)
                close()
            }
            // M3.5 9.5h17 + tick marks
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.5f, 9.5f)
                horizontalLineTo(20.5f)
                moveTo(8f, 3.2f)
                verticalLineToRelative(3.4f)
                moveTo(16f, 3.2f)
                verticalLineToRelative(3.4f)
            }
        }
    }

    // chat: stroke — speech bubble
    val chat: ImageVector by lazy {
        icon("chat") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M4.5 6.5A2.5 2.5 0 0 1 7 4h10a2.5 2.5 0 0 1 2.5 2.5v6A2.5 2.5 0 0 1 17 15H9l-4 3.5V6.5Z
                moveTo(4.5f, 6.5f)
                arcToRelative(2.5f, 2.5f, 0f, false, true, 2.5f, -2.5f)
                horizontalLineTo(17f)
                arcToRelative(2.5f, 2.5f, 0f, false, true, 2.5f, 2.5f)
                verticalLineToRelative(6f)
                arcToRelative(2.5f, 2.5f, 0f, false, true, -2.5f, 2.5f)
                horizontalLineTo(9f)
                lineToRelative(-4f, 3.5f)
                verticalLineTo(6.5f)
                close()
            }
        }
    }

    // user: stroke — circle head + arc body
    val user: ImageVector by lazy {
        icon("user") {
            // circle cx=12 cy=8.2 r=3.6
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 8.2f - 3.6f)
                arcToRelative(3.6f, 3.6f, 0f, true, true, 0f, 7.2f)
                arcToRelative(3.6f, 3.6f, 0f, true, true, 0f, -7.2f)
                close()
            }
            // M5 20c1.2-3.6 3.8-5.4 7-5.4s5.8 1.8 7 5.4
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 20f)
                curveTo(6.2f, 16.4f, 8.8f, 14.6f, 12f, 14.6f)
                reflectiveCurveTo(17.8f, 16.4f, 19f, 20f)
            }
        }
    }

    // task: stroke — rounded rect with checkmark and lines
    val task: ImageVector by lazy {
        icon("task") {
            // rect x=5 y=3.5 width=14 height=17 rx=3
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8f, 3.5f)
                arcToRelative(3f, 3f, 0f, false, false, -3f, 3f)
                verticalLineTo(17.5f)
                arcToRelative(3f, 3f, 0f, false, false, 3f, 3f)
                horizontalLineTo(16f)
                arcToRelative(3f, 3f, 0f, false, false, 3f, -3f)
                verticalLineTo(6.5f)
                arcToRelative(3f, 3f, 0f, false, false, -3f, -3f)
                close()
            }
            // m8.5 11 1.6 1.6L13 9.5M15.5 11h.01M8.5 15.5h5
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8.5f, 11f)
                lineToRelative(1.6f, 1.6f)
                lineTo(13f, 9.5f)
                moveTo(15.5f, 11f)
                horizontalLineToRelative(0.01f)
                moveTo(8.5f, 15.5f)
                horizontalLineToRelative(5f)
            }
        }
    }

    // plus: stroke — cross
    val plus: ImageVector by lazy {
        icon("plus") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M12 5.5v13M5.5 12h13
                moveTo(12f, 5.5f)
                verticalLineToRelative(13f)
                moveTo(5.5f, 12f)
                horizontalLineToRelative(13f)
            }
        }
    }

    // arrow: stroke — arrow right
    val arrow: ImageVector by lazy {
        icon("arrow") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M5 12h13
                moveTo(5f, 12f)
                horizontalLineTo(18f)
                // m13 6 6 6-6 6
                moveTo(13f, 6f)
                lineToRelative(6f, 6f)
                lineToRelative(-6f, 6f)
            }
        }
    }

    // check: stroke — checkmark
    val check: ImageVector by lazy {
        icon("check") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.1f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m5 12.5 4.5 4.5L19 7
                moveTo(5f, 12.5f)
                lineToRelative(4.5f, 4.5f)
                lineTo(19f, 7f)
            }
        }
    }

    // video: stroke — rectangle + triangle for play
    val video: ImageVector by lazy {
        icon("video") {
            // rect x=3 y=6.5 width=13 height=11 rx=3
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 6.5f)
                arcToRelative(3f, 3f, 0f, false, false, -3f, 3f)
                verticalLineTo(14.5f)
                arcToRelative(3f, 3f, 0f, false, false, 3f, 3f)
                horizontalLineTo(13f)
                arcToRelative(3f, 3f, 0f, false, false, 3f, -3f)
                verticalLineTo(9.5f)
                arcToRelative(3f, 3f, 0f, false, false, -3f, -3f)
                close()
            }
            // m16 10.5 5-2.8v8.6l-5-2.8z
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16f, 10.5f)
                lineToRelative(5f, -2.8f)
                verticalLineToRelative(8.6f)
                lineToRelative(-5f, -2.8f)
                close()
            }
        }
    }

    // send: solid — paper plane
    val send: ImageVector by lazy {
        icon("send") {
            path(fill = SolidColor(Color.Black)) {
                // M4.4 11.2 19 4.6c.9-.4 1.8.5 1.4 1.4l-6.6 14.6c-.4.9-1.7.8-2-.2l-1.7-5.2-5.2-1.7c-1-.3-1.1-1.6-.2-2Z
                moveTo(4.4f, 11.2f)
                lineTo(19f, 4.6f)
                curveTo(19.9f, 4.2f, 20.8f, 5.1f, 20.4f, 6f)
                lineToRelative(-6.6f, 14.6f)
                curveTo(13.4f, 21.5f, 12.1f, 21.4f, 11.8f, 20.4f)
                lineToRelative(-1.7f, -5.2f)
                lineToRelative(-5.2f, -1.7f)
                curveTo(3.9f, 13.2f, 3.8f, 11.9f, 4.4f, 11.2f) // approximation — last segment closes back
                // Actual: c-1-.3-1.1-1.6-.2-2Z
                close()
            }
        }
    }

    // attach: stroke — paperclip
    val attach: ImageVector by lazy {
        icon("attach") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M14 7 8.5 12.5a3 3 0 0 0 4.2 4.2L19 11a5 5 0 0 0-7-7L6.3 9.7a7 7 0 0 0 9.9 9.9
                moveTo(14f, 7f)
                lineTo(8.5f, 12.5f)
                arcToRelative(3f, 3f, 0f, false, false, 4.2f, 4.2f)
                lineTo(19f, 11f)
                arcToRelative(5f, 5f, 0f, false, false, -7f, -7f)
                lineTo(6.3f, 9.7f)
                arcToRelative(7f, 7f, 0f, false, false, 9.9f, 9.9f)
            }
        }
    }

    // trend: stroke — trending line + arrow
    val trend: ImageVector by lazy {
        icon("trend") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m4 16 5-5 3.5 3.5L20 7
                moveTo(4f, 16f)
                lineToRelative(5f, -5f)
                lineToRelative(3.5f, 3.5f)
                lineTo(20f, 7f)
                // M15 7h5v5
                moveTo(15f, 7f)
                horizontalLineTo(20f)
                verticalLineTo(12f)
            }
        }
    }

    // pin: stroke — location pin with inner circle
    val pin: ImageVector by lazy {
        icon("pin") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M12 21s7-6 7-11a7 7 0 0 0-14 0c0 5 7 11 7 11Z
                moveTo(12f, 21f)
                curveTo(12f, 21f, 19f, 15f, 19f, 10f)
                arcToRelative(7f, 7f, 0f, false, false, -14f, 0f)
                curveTo(5f, 15f, 12f, 21f, 12f, 21f)
                close()
            }
            // circle cx=12 cy=10 r=2.5
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 10f - 2.5f)
                arcToRelative(2.5f, 2.5f, 0f, true, true, 0f, 5f)
                arcToRelative(2.5f, 2.5f, 0f, true, true, 0f, -5f)
                close()
            }
        }
    }

    // globe: stroke — circle + longitude/latitude lines
    val globe: ImageVector by lazy {
        icon("globe") {
            // circle cx=12 cy=12 r=8.5
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 12f - 8.5f)
                arcToRelative(8.5f, 8.5f, 0f, true, true, 0f, 17f)
                arcToRelative(8.5f, 8.5f, 0f, true, true, 0f, -17f)
                close()
            }
            // M3.5 12h17
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.5f, 12f)
                horizontalLineTo(20.5f)
            }
            // M12 3.5c2.6 2.4 2.6 14.6 0 17
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3.5f)
                curveTo(14.6f, 5.9f, 14.6f, 18.1f, 12f, 20.5f)
            }
            // M12 3.5c-2.6 2.4-2.6 14.6 0 17
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3.5f)
                curveTo(9.4f, 5.9f, 9.4f, 18.1f, 12f, 20.5f)
            }
        }
    }

    // flag: stroke — flag shape
    val flag: ImageVector by lazy {
        icon("flag") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M5.5 21V4.5h11l-1.8 3.6 1.8 3.6h-11
                moveTo(5.5f, 21f)
                verticalLineTo(4.5f)
                horizontalLineToRelative(11f)
                lineToRelative(-1.8f, 3.6f)
                lineToRelative(1.8f, 3.6f)
                horizontalLineToRelative(-11f)
            }
        }
    }

    // chev: stroke — chevron right
    val chev: ImageVector by lazy {
        icon("chev") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m9 5.5 6.5 6.5L9 18.5
                moveTo(9f, 5.5f)
                lineToRelative(6.5f, 6.5f)
                lineTo(9f, 18.5f)
            }
        }
    }

    // clock: stroke — circle + hands
    val clock: ImageVector by lazy {
        icon("clock") {
            // circle cx=12 cy=12 r=8.5
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 12f - 8.5f)
                arcToRelative(8.5f, 8.5f, 0f, true, true, 0f, 17f)
                arcToRelative(8.5f, 8.5f, 0f, true, true, 0f, -17f)
                close()
            }
            // M12 7.5V12l3 2
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 7.5f)
                verticalLineTo(12f)
                lineToRelative(3f, 2f)
            }
        }
    }

    // x: stroke — close / X mark
    val x: ImageVector by lazy {
        icon("x") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m6 6 12 12M18 6 6 18
                moveTo(6f, 6f)
                lineToRelative(12f, 12f)
                moveTo(18f, 6f)
                lineTo(6f, 18f)
            }
        }
    }

    // edit: stroke — pencil
    val edit: ImageVector by lazy {
        icon("edit") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M4 20h4L19 9l-4-4L4 16v4Z
                moveTo(4f, 20f)
                horizontalLineToRelative(4f)
                lineTo(19f, 9f)
                lineToRelative(-4f, -4f)
                lineTo(4f, 16f)
                verticalLineToRelative(4f)
                close()
                // m14 6 4 4
                moveTo(14f, 6f)
                lineToRelative(4f, 4f)
            }
        }
    }

    // wallet: stroke rect + line + solid dot
    val wallet: ImageVector by lazy {
        icon("wallet") {
            // rect x=3.5 y=6 width=17 height=13 rx=3.5
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7f, 6f)
                arcToRelative(3.5f, 3.5f, 0f, false, false, -3.5f, 3.5f)
                verticalLineTo(15.5f)
                arcToRelative(3.5f, 3.5f, 0f, false, false, 3.5f, 3.5f)
                horizontalLineTo(17f)
                arcToRelative(3.5f, 3.5f, 0f, false, false, 3.5f, -3.5f)
                verticalLineTo(9.5f)
                arcToRelative(3.5f, 3.5f, 0f, false, false, -3.5f, -3.5f)
                close()
            }
            // M3.5 10h17
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.5f, 10f)
                horizontalLineTo(20.5f)
            }
            // circle cx=16.5 cy=14.5 r=1.3 fill solid
            path(fill = SolidColor(Color.Black)) {
                moveTo(16.5f, 14.5f - 1.3f)
                arcToRelative(1.3f, 1.3f, 0f, true, true, 0f, 2.6f)
                arcToRelative(1.3f, 1.3f, 0f, true, true, 0f, -2.6f)
                close()
            }
        }
    }

    // settings: stroke — gear / cog
    val settings: ImageVector by lazy {
        icon("settings") {
            // circle cx=12 cy=12 r=3
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 12f - 3f)
                arcToRelative(3f, 3f, 0f, true, true, 0f, 6f)
                arcToRelative(3f, 3f, 0f, true, true, 0f, -6f)
                close()
            }
            // M12 3v2.5M12 18.5V21M21 12h-2.5M5.5 12H3M18 6l-1.8 1.8M7.8 16.2 6 18M18 18l-1.8-1.8M7.8 7.8 6 6
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3f)
                verticalLineToRelative(2.5f)
                moveTo(12f, 18.5f)
                verticalLineTo(21f)
                moveTo(21f, 12f)
                horizontalLineToRelative(-2.5f)
                moveTo(5.5f, 12f)
                horizontalLineTo(3f)
                moveTo(18f, 6f)
                lineToRelative(-1.8f, 1.8f)
                moveTo(7.8f, 16.2f)
                lineTo(6f, 18f)
                moveTo(18f, 18f)
                lineToRelative(-1.8f, -1.8f)
                moveTo(7.8f, 7.8f)
                lineTo(6f, 6f)
            }
        }
    }

    // logout: stroke — arrow out of door
    val logout: ImageVector by lazy {
        icon("logout") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M14 5.5H7A1.5 1.5 0 0 0 5.5 7v10A1.5 1.5 0 0 0 7 18.5h7
                moveTo(14f, 5.5f)
                horizontalLineTo(7f)
                arcToRelative(1.5f, 1.5f, 0f, false, false, -1.5f, 1.5f)
                verticalLineToRelative(10f)
                arcToRelative(1.5f, 1.5f, 0f, false, false, 1.5f, 1.5f)
                horizontalLineTo(14f)
                // M16 8.5 19.5 12 16 15.5
                moveTo(16f, 8.5f)
                lineTo(19.5f, 12f)
                lineTo(16f, 15.5f)
                // M19.5 12H9.5
                moveTo(19.5f, 12f)
                horizontalLineTo(9.5f)
            }
        }
    }

    // shield: stroke — shield with checkmark inside
    val shield: ImageVector by lazy {
        icon("shield") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M12 3 5 5.5V11c0 4.4 3 8 7 9.5 4-1.5 7-5.1 7-9.5V5.5L12 3Z
                moveTo(12f, 3f)
                lineTo(5f, 5.5f)
                verticalLineTo(11f)
                curveTo(5f, 15.4f, 8f, 19f, 12f, 20.5f)
                curveTo(16f, 19f, 19f, 15.4f, 19f, 11f)
                verticalLineTo(5.5f)
                close()
                // m9 11.5 2 2 4-4.5
                moveTo(9f, 11.5f)
                lineToRelative(2f, 2f)
                lineToRelative(4f, -4.5f)
            }
        }
    }

    // repeat: stroke — cycle arrows
    val repeat: ImageVector by lazy {
        icon("repeat") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M4 9a5 5 0 0 1 5-5h7l-2.5-2.5
                moveTo(4f, 9f)
                arcToRelative(5f, 5f, 0f, false, true, 5f, -5f)
                horizontalLineToRelative(7f)
                lineToRelative(-2.5f, -2.5f)
                // M20 15a5 5 0 0 1-5 5H8l2.5 2.5
                moveTo(20f, 15f)
                arcToRelative(5f, 5f, 0f, false, true, -5f, 5f)
                horizontalLineTo(8f)
                lineToRelative(2.5f, 2.5f)
            }
        }
    }

    // cap: stroke — graduation cap
    val cap: ImageVector by lazy {
        icon("cap") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M3 9.5 12 5l9 4.5-9 4.5z
                moveTo(3f, 9.5f)
                lineTo(12f, 5f)
                lineToRelative(9f, 4.5f)
                lineToRelative(-9f, 4.5f)
                close()
                // M7 11.5V16c0 1.4 2.2 2.5 5 2.5s5-1.1 5-2.5v-4.5
                moveTo(7f, 11.5f)
                verticalLineTo(16f)
                curveTo(7f, 17.4f, 9.2f, 18.5f, 12f, 18.5f)
                reflectiveCurveTo(17f, 17.4f, 17f, 16f)
                verticalLineToRelative(-4.5f)
                // M21 9.5V14
                moveTo(21f, 9.5f)
                verticalLineTo(14f)
            }
        }
    }

    // teach: stroke — whiteboard/screen with stand
    val teach: ImageVector by lazy {
        icon("teach") {
            // rect x=3.5 y=4 width=17 height=11.5 rx=2.5
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 4f)
                arcToRelative(2.5f, 2.5f, 0f, false, false, -2.5f, 2.5f)
                verticalLineTo(13f)
                arcToRelative(2.5f, 2.5f, 0f, false, false, 2.5f, 2.5f)
                horizontalLineTo(18f)
                arcToRelative(2.5f, 2.5f, 0f, false, false, 2.5f, -2.5f)
                verticalLineTo(6.5f)
                arcToRelative(2.5f, 2.5f, 0f, false, false, -2.5f, -2.5f)
                close()
            }
            // M12 15.5V20M8 20h8
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 15.5f)
                verticalLineTo(20f)
                moveTo(8f, 20f)
                horizontalLineTo(16f)
            }
        }
    }

    // book: stroke — open book
    val book: ImageVector by lazy {
        icon("book") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M5 4.5h8a3 3 0 0 1 3 3V20a2.5 2.5 0 0 0-2.5-2.5H5Z
                moveTo(5f, 4.5f)
                horizontalLineToRelative(8f)
                arcToRelative(3f, 3f, 0f, false, true, 3f, 3f)
                verticalLineTo(20f)
                arcToRelative(2.5f, 2.5f, 0f, false, false, -2.5f, -2.5f)
                horizontalLineTo(5f)
                close()
                // M19 6.5V20a2.5 2.5 0 0 0-2.5-2.5H16
                moveTo(19f, 6.5f)
                verticalLineTo(20f)
                arcToRelative(2.5f, 2.5f, 0f, false, false, -2.5f, -2.5f)
                horizontalLineTo(16f)
            }
        }
    }

    // sparkle: solid — two 4-pointed sparkle shapes
    val sparkle: ImageVector by lazy {
        icon("sparkle") {
            path(fill = SolidColor(Color.Black)) {
                // M12 3l1.6 4.4L18 9l-4.4 1.6L12 15l-1.6-4.4L6 9l4.4-1.6z
                moveTo(12f, 3f)
                lineToRelative(1.6f, 4.4f)
                lineTo(18f, 9f)
                lineToRelative(-4.4f, 1.6f)
                lineTo(12f, 15f)
                lineToRelative(-1.6f, -4.4f)
                lineTo(6f, 9f)
                lineToRelative(4.4f, -1.6f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                // M18 14l.8 2.2L21 17l-2.2.8L18 20l-.8-2.2L15 17l2.2-.8z
                moveTo(18f, 14f)
                lineToRelative(0.8f, 2.2f)
                lineTo(21f, 17f)
                lineToRelative(-2.2f, 0.8f)
                lineTo(18f, 20f)
                lineToRelative(-0.8f, -2.2f)
                lineTo(15f, 17f)
                lineToRelative(2.2f, -0.8f)
                close()
            }
        }
    }

    // spark2: stroke — diagonal zigzag line (trending up)
    val spark2: ImageVector by lazy {
        icon("spark2") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M3 17 9 11l3.5 3.5L21 6
                moveTo(3f, 17f)
                lineTo(9f, 11f)
                lineToRelative(3.5f, 3.5f)
                lineTo(21f, 6f)
            }
        }
    }

    // heart: stroke — heart shape
    val heart: ImageVector by lazy {
        icon("heart") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M12 20S4 15 4 9.5A4 4 0 0 1 12 7a4 4 0 0 1 8 2.5C20 15 12 20 12 20Z
                moveTo(12f, 20f)
                curveTo(12f, 20f, 4f, 15f, 4f, 9.5f)
                arcToRelative(4f, 4f, 0f, false, true, 8f, -2.5f)
                arcToRelative(4f, 4f, 0f, false, true, 8f, 2.5f)
                curveTo(20f, 15f, 12f, 20f, 12f, 20f)
                close()
            }
        }
    }

    // doc: stroke — document with folded corner + lines
    val doc: ImageVector by lazy {
        icon("doc") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M7 3.5h7l4 4V19a1.5 1.5 0 0 1-1.5 1.5h-9A1.5 1.5 0 0 1 6 19V5A1.5 1.5 0 0 1 7 3.5Z
                moveTo(7f, 3.5f)
                horizontalLineToRelative(7f)
                lineToRelative(4f, 4f)
                verticalLineTo(19f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, 1.5f)
                horizontalLineToRelative(-9f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, -1.5f)
                verticalLineTo(5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 1.5f, -1.5f)
                close()
                // M14 3.5V8h4
                moveTo(14f, 3.5f)
                verticalLineTo(8f)
                horizontalLineToRelative(4f)
                // M9 13h6
                moveTo(9f, 13f)
                horizontalLineTo(15f)
                // M9 16.5h4
                moveTo(9f, 16.5f)
                horizontalLineTo(13f)
            }
        }
    }

    // upload: stroke — arrow up + line
    val upload: ImageVector by lazy {
        icon("upload") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M12 16V5m0 0L8 9m4-4 4 4M5 19h14
                moveTo(12f, 16f)
                verticalLineTo(5f)
                moveTo(12f, 5f)
                lineTo(8f, 9f)
                moveTo(12f, 5f)
                lineToRelative(4f, 4f)
                moveTo(5f, 19f)
                horizontalLineTo(19f)
            }
        }
    }
}
