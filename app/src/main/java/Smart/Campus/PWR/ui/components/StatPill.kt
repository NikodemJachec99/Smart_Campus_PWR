package Smart.Campus.PWR.ui.components

import Smart.Campus.PWR.ui.theme.Hairline
import Smart.Campus.PWR.ui.theme.InkText
import Smart.Campus.PWR.ui.theme.InkTextSoft
import Smart.Campus.PWR.ui.theme.PwrBlueWhisper
import Smart.Campus.PWR.ui.theme.PwrNavy
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatPill(
    eyebrow: String,
    leadingIcon: ImageVector,
    metric: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 132.dp)
            .floatingCard(cornerRadius = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PwrBlueWhisper)
                    .border(0.5.dp, Hairline, RoundedCornerShape(50))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = PwrNavy,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    eyebrow.uppercase(),
                    color = PwrNavy,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
            }

            Text(
                metric,
                color = InkText,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                letterSpacing = (-1.2).sp,
                style = MaterialTheme.typography.displaySmall
            )

            Text(
                label,
                color = InkTextSoft,
                fontSize = 12.sp,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
