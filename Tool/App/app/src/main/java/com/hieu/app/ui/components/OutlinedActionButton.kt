package com.hieu.app.ui.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hieu.app.ui.theme.BorderStrong
import com.hieu.app.ui.theme.TitleDark


@Composable
fun OutlinedActionButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.75.dp, BorderStrong),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TitleDark)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.height(16.dp).width(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 13.sp)
    }
}
