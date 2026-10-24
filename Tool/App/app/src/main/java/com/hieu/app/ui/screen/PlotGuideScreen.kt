package com.hieu.app.ui.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hieu.app.domain.math.buildPlotGuide
import com.hieu.app.ui.components.GraphView
import com.hieu.app.ui.i18n.AppLanguage
import com.hieu.app.ui.i18n.AppStrings
import com.hieu.app.ui.theme.BorderStrong
import com.hieu.app.ui.theme.CardBg
import com.hieu.app.ui.theme.DangerRed
import com.hieu.app.ui.theme.TextMuted
import com.hieu.app.ui.theme.TitleDark


@Composable
fun PlotGuideScreen(
    formulas: List<String>,
    language: AppLanguage,
    strings: AppStrings,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = remember(formulas, language) {
        buildPlotGuide(formulas, vi = language == AppLanguage.VI)
    }

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
                        .padding(horizontal = 4.dp, vertical = 2.dp)
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
                        strings.plotGuideScreenTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleDark
                    )
                }

                Spacer(Modifier.height(6.dp))

                if (formulas.none { it.isNotBlank() }) {
                    Text(
                        strings.plotGuideNoFormula,
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    // Đồ thị luôn hiện ở trên, các bước vẽ cuộn bên dưới
                    GraphView(formulas = formulas)
                    Spacer(Modifier.height(10.dp))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(result.sections) { section ->
                        SectionCard(section, strings)
                    }
                    if (result.errors.isNotEmpty()) {
                        item {
                            Column {
                                result.errors.forEach {
                                    Text(it, fontSize = 12.sp, color = DangerRed, modifier = Modifier.padding(top = 4.dp, start = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
