package Smart.Campus.PWR.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow

/**
 * iOS-style liquid glass surface.
 *
 * Recipe (matches Apple Music's tab bar / control center):
 *   1. Heavy backdrop blur (frosts the content behind).
 *   2. Vibrancy + slight saturation pop.
 *   3. Lens refraction with chromatic aberration (Android 13+) — gives the dome / prism edge.
 *   4. Whisper-thin tint so the backdrop reads through.
 *   5. Specular highlight stroke + soft drop shadow.
 */
fun Modifier.stableLiquidGlassSurface(
    backdrop: Backdrop? = null,
    cornerRadius: Dp,
    tint: Color,
    tintAlpha: Float,
    glowColor: Color = Color(0xFF5B7CFF),
    glowAlpha: Float = 0.24f
): Modifier {
    val supportsLens = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU // API 33+

    val base = if (backdrop != null) {
        this.drawBackdrop(
            backdrop = backdrop,
            shape = { RoundedCornerShape(cornerRadius) },
            effects = {
                // Order matters: color filter → blur → lens
                colorControls(saturation = 1.35f)
                vibrancy()
                blur(24.dp.toPx())
                if (supportsLens) {
                    lens(
                        refractionHeight = cornerRadius.toPx() * 1.4f,
                        refractionAmount = cornerRadius.toPx() * 1.1f,
                        depthEffect = true,
                        chromaticAberration = true
                    )
                }
            },
            highlight = { Highlight.Default.copy(alpha = 0.85f) },
            shadow = {
                Shadow(
                    radius = 22.dp,
                    color = Color.Black.copy(alpha = 0.18f),
                    alpha = 1f
                )
            },
            innerShadow = {
                InnerShadow(
                    radius = 6.dp,
                    color = Color.White.copy(alpha = 0.18f),
                    alpha = 1f
                )
            },
            onDrawSurface = {
                // Whisper-thin tint so the refracted backdrop still reads.
                drawRect(tint.copy(alpha = (tintAlpha * 0.35f).coerceIn(0f, 1f)))
            }
        )
    } else {
        this
    }

    return base
        .clip(RoundedCornerShape(cornerRadius))
        .then(
            if (backdrop == null) {
                // Fallback: no live backdrop, paint a solid frosted gradient.
                Modifier.background(
                    Brush.linearGradient(
                        colors = listOf(
                            tint.copy(alpha = (tintAlpha + 0.12f).coerceAtMost(0.96f)),
                            tint.copy(alpha = tintAlpha.coerceAtLeast(0.5f))
                        ),
                        start = Offset.Zero,
                        end = Offset(1000f, 1000f)
                    )
                )
            } else Modifier
        )
        .drawBehind {
            val radiusPx = cornerRadius.toPx()
            // Soft top-left ambient glow — reads as a light source kissing the glass.
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = glowAlpha),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.20f, size.height * 0.10f),
                    radius = size.width * 0.85f
                ),
                cornerRadius = CornerRadius(radiusPx)
            )
            // Specular sheen — the bright thin streak iOS glass has on the top edge.
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.45f
                ),
                cornerRadius = CornerRadius(radiusPx)
            )
            // Hairline rim with diagonal shimmer — the stroke that defines the glass edge.
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.30f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.40f),
                        Color.White.copy(alpha = 0.85f)
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                cornerRadius = CornerRadius(radiusPx),
                style = Stroke(width = 1.dp.toPx())
            )
            // Subtle bottom shadow inside the glass — ground it.
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.12f)
                    ),
                    startY = size.height * 0.62f,
                    endY = size.height
                ),
                cornerRadius = CornerRadius(radiusPx)
            )
        }
}

/**
 * Lightweight selection surface — used for the active pill *inside* a glass bar.
 * No backdrop sampling needed; reads as a frosted highlight on top of the parent glass.
 */
fun Modifier.liquidGlassSelectionSurface(
    cornerRadius: Dp,
    tintAlpha: Float
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(Color.White.copy(alpha = tintAlpha))
    .drawBehind {
        val radiusPx = cornerRadius.toPx()
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.55f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.06f)
                )
            ),
            cornerRadius = CornerRadius(radiusPx)
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.80f),
            cornerRadius = CornerRadius(radiusPx),
            style = Stroke(width = 0.7.dp.toPx())
        )
    }
