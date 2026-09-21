package com.example.helloandroid.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.helloandroid.ui.actionlib.PageActionLib
import com.example.helloandroid.ui.calendar.PageCalendar
import com.example.helloandroid.ui.calendar.PageMonthlyReport
import com.example.helloandroid.ui.calendar.PageStatistics
import com.example.helloandroid.ui.exercise.PageExecutePlan
import com.example.helloandroid.ui.exercise.PageExercisePrepare
import com.example.helloandroid.ui.exercise.PageTrainingResult
import com.example.helloandroid.ui.plan.PageCreatePlan
import com.example.helloandroid.ui.plan.PageEditPlan
import com.example.helloandroid.ui.plan.PageTrainingPlan
import com.example.helloandroid.ui.profile.PageBodyData
import com.example.helloandroid.ui.profile.PageBodyFatCalculator
import com.example.helloandroid.ui.profile.PageEditProfile
import com.example.helloandroid.ui.profile.PageProfile
import com.example.helloandroid.ui.profile.PageRmCalculator
import com.example.helloandroid.ui.profile.PageSettings
import com.example.helloandroid.ui.profile.PageUserProfile
import com.example.helloandroid.viewmodel.ExecutePlanViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    executePlanViewModel: ExecutePlanViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.TrainingPlan.route,
    ) {
        // -----------------------------------------------------------------------------------------
        // plan ‘计划’页面
        composable(Screen.TrainingPlan.route) { PageTrainingPlan(navController = navController) }
        composable(Screen.CreatePlan.route) { PageCreatePlan(navController = navController) }
        composable(
            route = Screen.EditPlan.route,
            arguments = listOf(
                navArgument("planId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getLong("planId") ?: 0L
            PageEditPlan(
                planId = planId,
                navController = navController
            )
        }

        // -----------------------------------------------------------------------------------------
        // action ‘动作’页面
        // 动作库
        composable(Screen.ActionLib.route) { PageActionLib() }
        // 选择动作
        composable(Screen.ActionLibSelectForPlan.route) {
            PageActionLib(
                selectMode = true,
                navController = navController,
                onActionsSelected = { actionIds ->  // ✅ 改为 onActionsSelected
                    val idsString = actionIds.joinToString(",")
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "selected_action_ids",
                        idsString
                    )
                }
            )
        }
        composable(Screen.ActionLibSelectForTraining.route) {
            PageActionLib(
                selectMode = true,
                navController = navController,
                onActionsSelected = { actionIds ->
                    val idsString = actionIds.joinToString(",")
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "selected_actions_for_training",
                        idsString
                    )
                }
            )
        }

        // -----------------------------------------------------------------------------------------
        // exercise ‘运动’页面
        composable(
            route = Screen.ExecutePlan.route,  // ✅ "execute_plan/{planId}"
            arguments = listOf(
                navArgument("planId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getLong("planId") ?: 0L

            // ✅ 从 SavedStateHandle 获取 planName
            var planName by remember { mutableStateOf("") }
            LaunchedEffect(Unit) {
                planName = backStackEntry.savedStateHandle.get<String>("planName") ?: ""
            }
            PageExecutePlan(
                planId = planId,
                planName = backStackEntry.arguments?.getString("planName") ?: "",
                navController = navController,
                viewModel = executePlanViewModel
            )
        }

        composable(
            Screen.ExercisePrepare.route,
            arguments = listOf(
                navArgument("planId") {
                    type = NavType.LongType  // ✅ 使用 LongType
                }
            )
        ) {backStackEntry ->
            val planId = backStackEntry.arguments?.getLong("planId") ?: 0L
            PageExercisePrepare(
                planId = planId,
                navController = navController
            )
        }

        composable(
            route = Screen.TrainingResult.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType },
                navArgument("fromTrainingComplete") {
                    type = NavType.BoolType
                    defaultValue = true
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val fromTrainingComplete = backStackEntry.arguments?.getBoolean("fromTrainingComplete") ?: true

            PageTrainingResult(
                sessionId = sessionId,
                fromTrainingComplete = fromTrainingComplete,
                navController = navController
            )
        }


        // -----------------------------------------------------------------------------------------
        // calendar ‘日程’页面
        composable(Screen.Calendar.route) { PageCalendar(navController = navController) }
        composable(Screen.MonthlyReport.route) { PageMonthlyReport( navController = navController) }
        composable(Screen.Statistics.route) { PageStatistics(navController = navController) }

        // -----------------------------------------------------------------------------------------
        // Profile ‘我的’页面
        composable(Screen.Profile.route) { PageProfile(navController = navController) }
        composable(Screen.BodyData.route) { PageBodyData(navController = navController) }
        composable(Screen.RmCalculator.route) { PageRmCalculator(navController = navController) }
        composable(Screen.Settings.route) { PageSettings(navController = navController) }
        composable(Screen.BodyFatCalculator.route) { PageBodyFatCalculator(navController = navController) }
        composable(Screen.UserProfile.route) { PageUserProfile(navController = navController) }
        composable(Screen.EditProfile.route) { PageEditProfile(navController = navController) }
    }
}