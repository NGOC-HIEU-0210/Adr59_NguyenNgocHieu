package com.hieu.app.ui.components


import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hieu.app.ui.i18n.LocalAppStrings
import com.hieu.app.ui.theme.BorderStrong
import com.hieu.app.ui.theme.PadBg
import com.hieu.app.ui.theme.TextMuted


@Composable
fun UploadDropZone(bitmap: Bitmap?, onClick: () -> Unit) {
    val strings = LocalAppStrings.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PadBg)
            .border(1.5.dp, BorderStrong, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = strings.selectedImageDescription,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.AddPhotoAlternate,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.height(26.dp).width(26.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(strings.tapToChooseImage, fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}
