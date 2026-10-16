package com.hieu.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hieu.app.ui.i18n.AppLanguage
import com.hieu.app.ui.i18n.AppStrings
import com.hieu.app.ui.theme.BorderStrong
import com.hieu.app.ui.theme.CardBg
import com.hieu.app.ui.theme.ChipActiveBg
import com.hieu.app.ui.theme.TextMuted
import com.hieu.app.ui.theme.TextSecondary
import com.hieu.app.ui.theme.TitleDark

@Composable
fun SettingsScreen(
    strings: AppStrings,
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEFEDE8))
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(0.75.dp, BorderStrong),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.backButtonDescription,
                            tint = TitleDark
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        strings.settingsScreenTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleDark
                    )
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    strings.languageSectionTitle.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(BorderStroke(0.75.dp, BorderStrong), RoundedCornerShape(16.dp))
                ) {
                    LanguageRow(
                        icon = Icons.Filled.Language,
                        label = strings.languageVietnamese,
                        selected = currentLanguage == AppLanguage.VI,
                        onClick = { onLanguageSelected(AppLanguage.VI) }
                    )
                    HorizontalDividerLine()
                    LanguageRow(
                        icon = Icons.Filled.Language,
                        label = strings.languageEnglish,
                        selected = currentLanguage == AppLanguage.EN,
                        onClick = { onLanguageSelected(AppLanguage.EN) }
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    strings.languageSectionHint,
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LanguageRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) ChipActiveBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) TitleDark else TextSecondary,
                modifier = Modifier.height(18.dp).width(18.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) TitleDark else TextSecondary
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(TitleDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.height(14.dp).width(14.dp)
                )
            }
        }
    }
}

@Composable
private fun HorizontalDividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.75.dp)
            .background(BorderStrong)
    )
}
