package com.hieu.app.ui.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hieu.app.domain.math.countFunctions
import com.hieu.app.ui.i18n.LocalAppStrings
import com.hieu.app.ui.theme.BorderStrong
import com.hieu.app.ui.theme.CurvePalette
import com.hieu.app.ui.theme.InkBlue
import com.hieu.app.ui.theme.TextSecondary


@Composable
fun RecognitionResultPanel(
    formulas: List<String>,
    onFormulasChange: (List<String>) -> Unit,
    onPlotClick: () -> Unit,
    onSolveClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val canSolveSystem = remember(formulas) { countFunctions(formulas) >= 2 }
    Column(modifier = Modifier.padding(top = 10.dp)) {
        Text(
            strings.recognitionResultHeader,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
        )

        formulas.forEachIndexed { index, value ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(CurvePalette[index % CurvePalette.size])
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { newValue ->
                        onFormulasChange(formulas.toMutableList().also { it[index] = newValue })
                    },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InkBlue,
                        unfocusedBorderColor = BorderStrong
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (formulas.size > 1) {
                    Spacer(Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            onFormulasChange(formulas.toMutableList().also { it.removeAt(index) })
                        },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = strings.removeThisFormula, tint = TextSecondary)
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { onFormulasChange(formulas + "") },
            modifier = Modifier.fillMaxWidth().height(40.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = InkBlue)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.height(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(strings.addAnotherFormula, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPlotClick,
                enabled = formulas.any { it.isNotBlank() },
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InkBlue, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.Filled.ShowChart, contentDescription = null, modifier = Modifier.height(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(strings.plotGuideAction, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            OutlinedButton(
                onClick = onSolveClick,
                enabled = canSolveSystem,
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, InkBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = InkBlue)
            ) {
                Icon(Icons.Filled.Functions, contentDescription = null, modifier = Modifier.height(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(strings.solveAction, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (!canSolveSystem) {
            Text(
                strings.solveNeedTwoFunctions,
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp, start = 2.dp)
            )
        }
    }
}
