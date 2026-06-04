package Smart.Campus.PWR.ui.theme

import androidx.compose.ui.graphics.Color

// Soft Indigo design system palette.
// Canonical tokens — add new names here; never remove existing public vals below.

// --- Surfaces ---
val CardSurface = Color(0xFFFFFFFF)
val Card2 = Color(0xFFFBFBFE)
val Bg = Color(0xFFF4F3FB)
val Bg2 = Color(0xFFECEAF6)

// --- Primary (indigo) ---
val Primary = Color(0xFF5B4DF0)
val Primary600 = Color(0xFF4A3DD6)
val Primary700 = Color(0xFF3E33B8)
val Primary100 = Color(0xFFE7E4FD)
val Primary50 = Color(0xFFF1EFFE)

// --- Ink (text) ---
val InkToken = Color(0xFF1B1A2E)   // canonical "Ink" token; alias `Ink` below targets this
val Ink2 = Color(0xFF56546E)
val Ink3 = Color(0xFF8C8AA3)
val Ink4 = Color(0xFFB8B6CB)

// --- Lines / dividers ---
val Line = Color(0xFFECEBF3)
val Line2 = Color(0xFFE2E0EE)

// --- Status: success ---
val Green = Color(0xFF1E9E6A)
val GreenBg = Color(0xFFE0F4EB)

// --- Status: warning / amber ---
val Amber = Color(0xFFC9722E)
val AmberBg = Color(0xFFFBECDD)

// --- Status: danger ---
val Red = Color(0xFFDD5468)
val RedBg = Color(0xFFFBE6EA)

// --- Star / rating ---
val Star = Color(0xFFF6B73C)

// --- Subject pastel pairs ---
val MathBg = Color(0xFFE9E6FE)
val MathFg = Color(0xFF5B4DF0)
val PhysBg = Color(0xFFE1EFFE)
val PhysFg = Color(0xFF2C6BD4)
val CsBg = Color(0xFFDFF3EA)
val CsFg = Color(0xFF1E9E6A)
val StatBg = Color(0xFFFCEBDC)
val StatFg = Color(0xFFC9722E)
val LangBg = Color(0xFFFCE6EF)
val LangFg = Color(0xFFC84A77)

// --- Achromatic ---
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

// =============================================================================
// Backwards-compatible aliases — VALUES updated for Soft Indigo; NAMES unchanged.
// =============================================================================

// --- Surfaces ---
val Bone = Bg
val WarmCream = Card2
val PaperLayer = Bg2
val PaperPressed = Bg2          // slightly darker pressed state; keep as Bg2
val Surface = Card2
val CardWhite = CardSurface

// --- Ink ---
val InkPrimary = InkToken
val InkSecondary = Ink2
val InkTertiary = Ink3
val InkDisabled = Ink4

// --- Primary family ---
val ForestAccent = Primary
val ForestDeep = Primary700
val ClayAccent = Amber
val ClaySoft = AmberBg
val GoldAccent = Star
val OxfordAccent = Primary600
val BerryAccent = LangFg

// --- Lines ---
val PaperLine = Line2
val PaperLineSoft = Line
val ShadowSoft = Color(0x331B1A2E)

// --- Status ---
val SuccessSoft = GreenBg
val SuccessText = Green
val WarningSoft = AmberBg
val WarningText = Amber
val DangerSoft = RedBg
val DangerText = Red
val InfoSoft = Primary50
val InfoText = Primary600

// --- Backwards-compat secondary aliases ---
val Snow = Bg
val SnowDeep = Bg2
val Cloud = CardSurface
val CloudHover = Card2
val Mist = Bg2
val PwrNavy = Primary
val PwrNavyDeep = Primary700
val PwrBlue = Primary
val PwrBlueLight = Primary100
val PwrBlueSoft = Primary50
val PwrBlueWhisper = Bg2
val InkText = InkToken
val InkTextSoft = Ink2
val Hairline = Line
val HairlineStrong = Line2

val Highlight = Primary
val HighlightSoft = Primary100
val HighlightWhisper = Primary50
val OnHighlight = CardSurface

val Forest = Primary
val ForestSoft = Card2
val ForestMuted = Bg2
val Sage = Primary
val SageDim = Primary600
val SageDeep = Bg2
val Paper = Bg
val PaperSoft = Bg2
val PaperDim = Bg2
val PaperText = InkToken
val PaperTextSoft = Ink2
val Ink = InkToken
val InkSoft = Ink2
val InkMuted = Ink3

val PwrNavyDark = Primary700
val PwrBlueMuted = Ink3
val PwrRed = Red
val PwrRedSoft = RedBg
val AppBackground = Bg
val AppSurface = Card2
val AppSurfaceMuted = Bg2
val AppBorder = Line
val TextPrimary = InkToken
val TextSecondary = Ink2
val PurpleSoft = Primary50
val PurpleText = Primary600
val OrangeSoft = AmberBg
val OrangeText = Amber
val YellowSoft = AmberBg
val YellowText = Amber
val PwrGold = Star
val PwrTeal = Green
val PwrIndigo = Primary
val StatusBookedBg = GreenBg
val StatusBookedFg = Green
val StatusCancelledBg = RedBg
val StatusCancelledFg = Red
val StatusPendingBg = AmberBg
val StatusPendingFg = Amber
val SurfaceGradientTop = Card2
val SurfaceGradientBottom = Bg2
val PaperHairline = Line
