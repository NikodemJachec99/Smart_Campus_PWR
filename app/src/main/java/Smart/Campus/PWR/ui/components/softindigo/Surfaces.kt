package Smart.Campus.PWR.ui.components.softindigo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import Smart.Campus.PWR.ui.theme.Card2
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Line

private val shape22 = RoundedCornerShape(22.dp)
private val shape16 = RoundedCornerShape(16.dp)

/** Elevated white card — primary card surface in the design system. */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .cardShadow(shape22)
            .clip(shape22)
            .background(CardSurface)
            .padding(18.dp),
        content = content
    )
}

/** Quieter card — light shadow, more compact. */
@Composable
fun CardQ(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .softShadow(shape22)
            .clip(shape22)
            .background(CardSurface)
            .padding(padding),
        content = content
    )
}

/** Flat card — barely elevated, uses a 1-dp Line border instead of shadow. */
@Composable
fun CardFlat(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape22)
            .background(Card2)
            .border(1.dp, Line, shape22)
            .padding(18.dp),
        content = content
    )
}

/** Small tile — compact card used in grid layouts. */
@Composable
fun Tile(
    modifier: Modifier = Modifier,
    padding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .softShadow(shape16)
            .clip(shape16)
            .background(CardSurface)
            .padding(padding),
        content = content
    )
}

/** 1-dp horizontal rule using the Line color token. */
@Composable
fun SoftDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Line)
    )
}
