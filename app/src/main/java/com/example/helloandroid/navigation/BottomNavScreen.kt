package com.example.helloandroid.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavScreen(val route: String, val label: String, val icon: ImageVector, val isCenterSpecial: Boolean = false) {
    data object TrainingPlan : BottomNavScreen("training_plan", "计划", Icons.Filled.NoteAlt)
    data object ActionLib : BottomNavScreen("action_lib", "动作", Icons.Filled.FitnessCenter)
    data object Calendar : BottomNavScreen("calendar", "日程", Icons.Filled.CalendarMonth)
    data object Profile : BottomNavScreen("profile", "我的", Icons.Filled.Person)

}

sealed class Screen(val route: String, val hideBottomBar: Boolean = false) {
    // -----------------------------------------------------------------------------------------
    // plan ‘计划’页面
    data object TrainingPlan : Screen("training_plan")
    data object CreatePlan : Screen("create_plan")
    data object EditPlan : Screen("edit_plan/{planId}") {
        fun pass(planId: Long) = "edit_plan/$planId"
    }

    // -----------------------------------------------------------------------------------------
    // action ‘动作’页面
    data object ActionLib : Screen("action_lib")
    data object ActionLibSelect : Screen("action_lib_select")

    // -----------------------------------------------------------------------------------------
    // exercise ‘运动’页面
    data object Exercise : Screen("exercise")
    data object ExecutePlan : Screen("execute_plan/{planId}", hideBottomBar = true) {
        fun pass(planId: Long) = "execute_plan/$planId"
    }
    data object ExercisePrepare : Screen(route = "exercise_prepare/{planId}", hideBottomBar = true) {
        fun pass(planId: Long): String = "exercise_prepare/$planId"
    }
    data object TrainingResult : Screen(
        route = "training_result/{sessionId}?fromTrainingComplete={fromTrainingComplete}",
        hideBottomBar = true
    ) {
        fun pass(sessionId: Long, fromTrainingComplete: Boolean): String {

            return "training_result/$sessionId?fromTrainingComplete=$fromTrainingComplete"
        }
    }

    // -----------------------------------------------------------------------------------------
    // calendar ‘日程’页面
    data object Calendar : Screen("calendar")
    data object MonthlyReport : Screen(
        route = "monthly_report",
        hideBottomBar = true
    )
    data object Statistics : Screen(
        route = "statistics",
        hideBottomBar = true
    )

    // -----------------------------------------------------------------------------------------
    // profile ‘我的’页面
    data object Profile : Screen("profile")
    data object BodyData : Screen(
        route = "body_data",
        hideBottomBar = true
    )
    data object RmCalculator : Screen(
        route = "rm_calculator",
        hideBottomBar = true
    )
    data object BodyFatCalculator : Screen(
        route = "body_fat_calculator",
        hideBottomBar = true
    )
    data object Settings : Screen(
        route = "settings",
        hideBottomBar = true
    )
    data object UserProfile : Screen(
        route = "user_profile",
        hideBottomBar = true
    )
    data object EditProfile : Screen(
        route = "edit_profile",
        hideBottomBar = true
    )
    // -----------------------------------------------------------------------------------------
    // ✅ 获取路由前缀（用于匹配带参数的路径）
    fun getRoutePrefix(): String {
        return route.split("/{").first()
    }

    companion object {
        // ✅ 所有页面的列表
        val allScreens = listOf(
            TrainingPlan,
            ActionLib,
            Calendar,
            Profile,
            CreatePlan,
            ActionLibSelect,
            ExecutePlan,
            ExercisePrepare,
            EditPlan,
            TrainingResult,
            MonthlyReport,
            Statistics  // ✅ 确保添加了 Statistics
        )

        // ✅ 根据路由查找对应的 Screen
        fun fromRoute(route: String?): Screen? {
            if (route == null) return null

            // ✅ 按路由具体程度排序（先匹配带参数的）
            return allScreens
                .sortedByDescending { it.route.count { c -> c == '/' } }
                .firstOrNull { screen ->
                    when {
                        route == screen.route -> true
                        screen.route.contains("/{") -> {
                            val prefix = screen.route.split("/{").first()
                            route.startsWith(prefix)
                        }
                        else -> route == screen.route
                    }
                }
        }

        // ✅ 将 BottomNavScreen 转换为 Screen
        fun fromBottomNavScreen(bottomNavScreen: BottomNavScreen): Screen {
            return when (bottomNavScreen) {
                BottomNavScreen.TrainingPlan -> TrainingPlan
                BottomNavScreen.ActionLib -> ActionLib
                BottomNavScreen.Calendar -> Calendar
                BottomNavScreen.Profile -> Profile
            }
        }
    }
}

val bottomNavItems = listOf(
    BottomNavScreen.TrainingPlan,
    BottomNavScreen.ActionLib,
    BottomNavScreen.Calendar,
    BottomNavScreen.Profile
)