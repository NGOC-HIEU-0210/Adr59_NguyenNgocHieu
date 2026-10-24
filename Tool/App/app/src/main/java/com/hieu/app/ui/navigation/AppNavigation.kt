package com.hieu.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hieu.app.ui.FormulaViewModel
import com.hieu.app.ui.i18n.AppLanguage
import com.hieu.app.ui.i18n.English
import com.hieu.app.ui.i18n.LocalAppStrings
import com.hieu.app.ui.i18n.Vietnamese
import com.hieu.app.ui.screen.FormulaRecognitionScreen
import com.hieu.app.ui.screen.GraphToolsScreen
import com.hieu.app.ui.screen.PlotGuideScreen
import com.hieu.app.ui.screen.SettingsScreen
import com.hieu.app.ui.screen.SolveScreen


object Routes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val GRAPH = "graph"
    const val SOLVE = "solve"
    const val PLOT_GUIDE = "plot_guide"
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()


    val viewModel: FormulaViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val strings = if (state.language == AppLanguage.VI) Vietnamese else English

    CompositionLocalProvider(LocalAppStrings provides strings) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFEFEDE8))
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.MAIN
            ) {
                composable(Routes.MAIN) {
                    FormulaRecognitionScreen(
                        viewModel = viewModel,
                        onOpenSettings = { navController.navigateSingleTop(Routes.SETTINGS) },
                        onOpenGraphTools = { navController.navigateSingleTop(Routes.GRAPH) },
                        onOpenSolve = { navController.navigateSingleTop(Routes.SOLVE) },
                        onOpenPlotGuide = { navController.navigateSingleTop(Routes.PLOT_GUIDE) }
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        strings = strings,
                        currentLanguage = state.language,
                        onLanguageSelected = viewModel::setLanguage,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.SOLVE) {
                    SolveScreen(
                        formulas = state.formulas,
                        language = state.language,
                        strings = strings,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.PLOT_GUIDE) {
                    PlotGuideScreen(
                        formulas = state.formulas,
                        language = state.language,
                        strings = strings,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.GRAPH) {
                    GraphToolsScreen(
                        formulas = state.formulas,
                        strings = strings,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}


private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true }
}
