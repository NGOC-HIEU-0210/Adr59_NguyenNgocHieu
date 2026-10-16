package com.hieu.app.ui.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hieu.app.ui.theme.BorderStrong
import com.hieu.app.ui.theme.ChipActiveBg
import com.hieu.app.ui.theme.TextSecondary
import com.hieu.app.ui.theme.TitleDark


@Composable
fun SegmentButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(38.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) ChipActiveBg else Color.Transparent,
        border = if (selected) null else BorderStroke(0.75.dp, BorderStrong)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) TitleDark else TextSecondary,
                modifier = Modifier.height(15.dp).width(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) TitleDark else TextSecondary
            )
        }
    }
}
