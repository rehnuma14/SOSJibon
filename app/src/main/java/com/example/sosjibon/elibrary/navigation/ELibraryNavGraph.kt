package com.example.sosjibon.elibrary.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.sosjibon.elibrary.location.EmergencyLocationHelper
import com.example.sosjibon.elibrary.resources.AssessmentScreen
import com.example.sosjibon.elibrary.resources.ResourceCenterScreen
import com.example.sosjibon.elibrary.resources.ResourceDetailScreen
import com.example.sosjibon.elibrary.resources.ResourceViewModel

object ELibraryRoutes {
    const val GRAPH = "resource_center_graph"
    const val HOME = "resources"
    const val ASSESSMENT = "resource_center_assessment"
    const val DETAIL = "resource_center_detail/{conditionId}"

    fun detailRoute(conditionId: String) = "resource_center_detail/$conditionId"
}

@Composable
private fun sharedResourceViewModel(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry
): ResourceViewModel {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(ELibraryRoutes.GRAPH)
    }
    return viewModel(parentEntry)
}

fun NavGraphBuilder.eLibraryGraph(navController: NavHostController) {
    navigation(startDestination = ELibraryRoutes.HOME, route = ELibraryRoutes.GRAPH) {

        composable(ELibraryRoutes.HOME) { backStackEntry ->
            val sharedViewModel = sharedResourceViewModel(navController, backStackEntry)
            ResourceCenterScreen(
                viewModel = sharedViewModel,
                onConditionClick = { id ->
                    navController.navigate(ELibraryRoutes.detailRoute(id))
                },
                onStartAssessment = {
                    navController.navigate(ELibraryRoutes.ASSESSMENT)
                }
            )
        }

        composable(ELibraryRoutes.ASSESSMENT) {
            val context = LocalContext.current
            AssessmentScreen(
                onOpenGuide = { id ->
                    navController.navigate(ELibraryRoutes.detailRoute(id))
                },
                onCallEmergency = {
                    EmergencyLocationHelper.launchDialer(
                        context,
                        EmergencyLocationHelper.getEmergencyNumber(null)
                    )
                },
                onExit = { navController.popBackStack(ELibraryRoutes.HOME, inclusive = false) }
            )
        }

        composable(
            route = ELibraryRoutes.DETAIL,
            arguments = listOf(navArgument("conditionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val sharedViewModel = sharedResourceViewModel(navController, backStackEntry)
            val conditionId = backStackEntry.arguments?.getString("conditionId") ?: return@composable
            ResourceDetailScreen(
                conditionId = conditionId,
                viewModel = sharedViewModel,
                onBack = { navController.popBackStack() },
                onRelatedGuideClick = { relatedId ->
                    navController.navigate(ELibraryRoutes.detailRoute(relatedId))
                },
                onCallEmergency = {
                    EmergencyLocationHelper.launchDialer(
                        context,
                        EmergencyLocationHelper.getEmergencyNumber(null)
                    )
                }
            )
        }
    }
}
