package com.bigfatfish.release.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bigfatfish.release.ui.balance.BalanceHistoryScreen
import com.bigfatfish.release.ui.balance.BalanceHistoryViewModel
import com.bigfatfish.release.ui.groupbuy.GroupBuyScreen
import com.bigfatfish.release.ui.history.ChatHistoryScreen
import com.bigfatfish.release.ui.history.ChatHistoryViewModel
import com.bigfatfish.release.ui.home.DashboardScreen
import com.bigfatfish.release.ui.home.DashboardViewModel
import com.bigfatfish.release.ui.keylist.KeyListScreen
import com.bigfatfish.release.ui.keylist.KeyListViewModel
import com.bigfatfish.release.ui.settings.SettingsScreen
import com.bigfatfish.release.ui.settings.SettingsViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            val vm: DashboardViewModel = viewModel()
            DashboardScreen(
                dashboardViewModel = vm,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(Routes.CHAT_HISTORY) {
            val vm: ChatHistoryViewModel = viewModel()
            ChatHistoryScreen(vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.KEY_LIST) {
            val vm: KeyListViewModel = viewModel()
            KeyListScreen(vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel()
            SettingsScreen(vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.BALANCE_HISTORY) {
            val vm: BalanceHistoryViewModel = viewModel()
            BalanceHistoryScreen(vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.GROUP_BUY) {
            GroupBuyScreen(onBack = { navController.popBackStack() })
        }
    }
}