package com.hieu.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hieu.app.domain.math.SolveSection
import com.hieu.app.domain.math.solveSystem
import com.hieu.app.ui.i18n.AppLanguage
import com.hieu.app.ui.i18n.AppStrings
import com.hieu.app.ui.theme.BorderSoft
import com.hieu.app.ui.theme.BorderStrong
import com.hieu.app.ui.theme.CardBg
import com.hieu.app.ui.theme.ChipActiveBg
import com.hieu.app.ui.theme.DangerRed
import com.hieu.app.ui.theme.InkBlue
import com.hieu.app.ui.theme.PadBg
import com.hieu.app.ui.theme.TextMuted
import com.hieu.app.ui.theme.TextSecondary
import com.hieu.app.ui.theme.TitleDark
@Composable
fun SolveScreen(
    formulas: List<String>,
    language: AppLanguage,
    strings: AppStrings,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = remember(formulas, language) {
        solveSystem(formulas, vi = language == AppLanguage.VI)
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
                        strings.solveScreenTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleDark
                    )
                }

                Spacer(Modifier.height(6.dp))

                if (result.sections.isEmpty() && result.errors.isEmpty()) {
                    Text(
                        if (formulas.any { it.isNotBlank() }) strings.solveNeedTwoFunctions else strings.solveNoFormula,
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
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

@Composable
internal fun SectionCard(section: SolveSection, strings: AppStrings) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PadBg)
                .border(0.75.dp, BorderSoft, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text(section.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TitleDark)

            section.steps.forEachIndexed { index, step ->
                Spacer(Modifier.height(10.dp))
                Text(
                    "${strings.stepLabel} ${index + 1}. ${step.heading}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkBlue
                )
                step.lines.forEach { line ->
                    Text(
                        line,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 3.dp, start = 6.dp)
                    )
                }
            }

            if (section.conclusion.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ChipActiveBg)
                        .padding(10.dp)
                ) {
                    Text(strings.conclusionLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                    section.conclusion.forEach {
                        Text(
                            it,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Medium,
                            color = TitleDark,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
