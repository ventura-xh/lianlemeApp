package com.example.helloandroid

import android.os.Bundle
import android.os.Trace.isEnabled
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.helloandroid.navigation.AppNavGraph
import com.example.helloandroid.navigation.BottomNavScreen
import com.example.helloandroid.navigation.Screen
import com.example.helloandroid.navigation.bottomNavItems
import com.example.helloandroid.ui.theme.HelloAndroidTheme
import com.example.helloandroid.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private var currentRoute: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isOnBottomNavScreen()) {
                    finish()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        setContent {
            HelloAndroidTheme {
                MainScreen(
                    onRouteChange = { route ->
                        currentRoute = route
                    }
                )
            }
        }
    }

    private fun isOnBottomNavScreen(): Boolean {
        val route = currentRoute ?: return false
        return route == BottomNavScreen.TrainingPlan.route ||
                route == BottomNavScreen.ActionLib.route ||
                route == BottomNavScreen.Calendar.route ||
                route == BottomNavScreen.Profile.route
    }
}

@Composable
fun MainScreen(
    onRouteChange: (String?) -> Unit = {},
    mainViewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val firstPlanId by mainViewModel.firstPlanId.collectAsStateWithLifecycle()
    val hasPlans by mainViewModel.hasPlans.collectAsStateWithLifecycle()

    // ✅ 通过路由查找对应的 Screen，获取 hideBottomBar 属性
    val currentScreen = Screen.fromRoute(currentRoute)
    val shouldHideBottomBar = currentScreen?.hideBottomBar ?: false

    // ✅ 通知 Activity 当前路由变化
    LaunchedEffect(currentRoute) {
        onRouteChange(currentRoute)
    }

    Scaffold(
        bottomBar = {
            if (!shouldHideBottomBar) {
                CustomBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { bottomNavScreen ->
                        val targetScreen = Screen.fromBottomNavScreen(bottomNavScreen)
                        navController.navigate(targetScreen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AppNavGraph(navController = navController)
        }
    }
}

@Composable
fun CustomBottomNavBar(
    currentRoute: String?,
    onNavigate: (BottomNavScreen) -> Unit
) {
    Box(
         modifier = Modifier
             .fillMaxWidth()
             .height(64.dp)
             .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            bottomNavItems.forEach { screen ->
                if (screen.isCenterSpecial) {
                    Spacer(modifier = Modifier.width(50.dp))
                } else {
                    val selected = currentRoute == screen.route
                    NavItem(
                        icon = screen.icon,
                        label = screen.label,
                        selected = selected,
                        onClick = { onNavigate(screen)}
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = color,
            maxLines = 1
        )
    }
}




@Preview(showBackground = true, name = "选中-计划表")
@Composable
private fun PreviewBottomNav_Schedule() {
    MaterialTheme {
        CustomBottomNavBar(
            currentRoute = BottomNavScreen.TrainingPlan.route,
            onNavigate = {} // 预览中不需要真实导航
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BarPreview() {
    HelloAndroidTheme() {
        MainScreen()
    }
}