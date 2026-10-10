package com.kosio.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.kosio.app.KosioApplication
import com.kosio.app.R
import com.kosio.app.core.designsystem.CharcoalTextMuted
import com.kosio.app.core.designsystem.DeepIndigo
import com.kosio.app.core.designsystem.PureWhiteSurface
import com.kosio.app.core.designsystem.SuccessTeal
import com.kosio.app.core.designsystem.WarmIvoryBackground
import com.kosio.app.ui.history.HistoryScreen
import com.kosio.app.ui.history.HistoryViewModel
import com.kosio.app.ui.history.SharePreviewScreen
import com.kosio.app.ui.ingredients.IngredientDetailScreen
import com.kosio.app.ui.ingredients.IngredientFormScreen
import com.kosio.app.ui.ingredients.IngredientListScreen
import com.kosio.app.ui.ingredients.IngredientViewModel
import com.kosio.app.ui.onboarding.OnboardingScreen
import com.kosio.app.ui.onboarding.OnboardingViewModel
import com.kosio.app.ui.overview.OverviewScreen
import com.kosio.app.ui.overview.OverviewViewModel
import com.kosio.app.ui.pricing.PricingSimulationScreen
import com.kosio.app.ui.pricing.PricingViewModel
import com.kosio.app.ui.products.HppResultScreen
import com.kosio.app.ui.products.ProductFormScreen
import com.kosio.app.ui.products.ProductListScreen
import com.kosio.app.ui.products.ProductViewModel
import com.kosio.app.ui.settings.AboutScreen
import com.kosio.app.ui.settings.BackupRestoreScreen
import com.kosio.app.ui.settings.SettingsScreen
import com.kosio.app.ui.settings.SettingsViewModel

sealed class BottomNavItem(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector
) {
    data object Overview : BottomNavItem(Screen.Overview.route, R.string.nav_overview, Icons.Default.Home)
    data object Products : BottomNavItem(Screen.Products.route, R.string.nav_products, Icons.Default.Fastfood)
    data object Ingredients : BottomNavItem(Screen.Ingredients.route, R.string.nav_ingredients, Icons.Default.Kitchen)
    data object Settings : BottomNavItem(Screen.Settings.route, R.string.nav_settings, Icons.Default.Settings)
}

@Composable
fun KosioAppNavigation(
    navController: NavHostController,
    isOnboardingCompleted: Boolean,
    app: KosioApplication
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem.Overview,
        BottomNavItem.Products,
        BottomNavItem.Ingredients,
        BottomNavItem.Settings
    )

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        containerColor = WarmIvoryBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = PureWhiteSurface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = stringResource(item.titleRes)) },
                            label = { Text(stringResource(item.titleRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DeepIndigo,
                                selectedTextColor = DeepIndigo,
                                indicatorColor = PureWhiteSurface,
                                unselectedIconColor = CharcoalTextMuted,
                                unselectedTextColor = CharcoalTextMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isOnboardingCompleted) Screen.Overview.route else Screen.Onboarding.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
            modifier = Modifier.padding(innerPadding)
        ) {
            // Onboarding
            composable(Screen.Onboarding.route) {
                val vm = OnboardingViewModel(app.userPreferencesRepository)
                OnboardingScreen(
                    viewModel = vm,
                    onFinish = {
                        navController.navigate(Screen.Overview.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Overview
            composable(Screen.Overview.route) {
                val vm = OverviewViewModel(
                    ingredientRepository = app.ingredientRepository,
                    productRepository = app.productRepository,
                    snapshotRepository = app.snapshotRepository,
                    preferencesRepository = app.userPreferencesRepository
                )
                OverviewScreen(
                    viewModel = vm,
                    onNavigateToAddIngredient = { navController.navigate(Screen.IngredientForm.createRoute()) },
                    onNavigateToCreateProduct = { navController.navigate(Screen.ProductForm.createRoute()) },
                    onNavigateToProductHpp = { id -> navController.navigate(Screen.HppResult.createRoute(id)) },
                    onNavigateToPricing = { id -> navController.navigate(Screen.PricingSimulation.createRoute(id)) }
                )
            }

            // Products
            composable(Screen.Products.route) {
                val vm = ProductViewModel(
                    productRepository = app.productRepository,
                    ingredientRepository = app.ingredientRepository,
                    snapshotRepository = app.snapshotRepository,
                    preferencesRepository = app.userPreferencesRepository
                )
                ProductListScreen(
                    viewModel = vm,
                    onNavigateToCreate = { navController.navigate(Screen.ProductForm.createRoute()) },
                    onNavigateToDetail = { id -> navController.navigate(Screen.HppResult.createRoute(id)) },
                    onNavigateToEdit = { id -> navController.navigate(Screen.ProductForm.createRoute(id)) }
                )
            }

            // Ingredients
            composable(Screen.Ingredients.route) {
                val vm = IngredientViewModel(app.ingredientRepository, app.userPreferencesRepository)
                IngredientListScreen(
                    viewModel = vm,
                    onNavigateToAdd = { navController.navigate(Screen.IngredientForm.createRoute()) },
                    onNavigateToScanReceipt = { navController.navigate(Screen.ReceiptScan.route) },
                    onNavigateToDetail = { id -> navController.navigate(Screen.IngredientDetail.createRoute(id)) }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                val vm = SettingsViewModel(app.userPreferencesRepository, app.database, app.backupManager)
                SettingsScreen(
                    viewModel = vm,
                    onNavigateToBackupRestore = { navController.navigate(Screen.BackupRestore.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) }
                )
            }

            // Ingredient Form
            composable(
                route = Screen.IngredientForm.route,
                arguments = listOf(navArgument("ingredientId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val ingredientId = backStackEntry.arguments?.getString("ingredientId")
                val vm = IngredientViewModel(app.ingredientRepository, app.userPreferencesRepository)
                IngredientFormScreen(
                    ingredientId = ingredientId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Ingredient Detail
            composable(
                route = Screen.IngredientDetail.route,
                arguments = listOf(navArgument("ingredientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val ingredientId = backStackEntry.arguments?.getString("ingredientId") ?: ""
                val vm = IngredientViewModel(app.ingredientRepository, app.userPreferencesRepository)
                IngredientDetailScreen(
                    ingredientId = ingredientId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(Screen.IngredientForm.createRoute(id)) }
                )
            }

            // Product Form
            composable(
                route = Screen.ProductForm.route,
                arguments = listOf(navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                val vm = ProductViewModel(
                    productRepository = app.productRepository,
                    ingredientRepository = app.ingredientRepository,
                    snapshotRepository = app.snapshotRepository,
                    preferencesRepository = app.userPreferencesRepository
                )
                ProductFormScreen(
                    productId = productId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddIngredient = { navController.navigate(Screen.IngredientForm.createRoute()) },
                    onSavedAndNavigateToHpp = { savedId ->
                        navController.navigate(Screen.HppResult.createRoute(savedId)) {
                            popUpTo(Screen.ProductForm.route) { inclusive = true }
                        }
                    }
                )
            }

            // HPP Result
            composable(
                route = Screen.HppResult.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val vm = ProductViewModel(
                    productRepository = app.productRepository,
                    ingredientRepository = app.ingredientRepository,
                    snapshotRepository = app.snapshotRepository,
                    preferencesRepository = app.userPreferencesRepository
                )
                HppResultScreen(
                    productId = productId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPricing = { id -> navController.navigate(Screen.PricingSimulation.createRoute(id)) },
                    onNavigateToEdit = { id -> navController.navigate(Screen.ProductForm.createRoute(id)) },
                    onNavigateToHistory = { id -> navController.navigate(Screen.History.createRoute(id)) },
                    onNavigateToShare = { id -> navController.navigate(Screen.SharePreview.createRoute(id)) }
                )
            }

            // Pricing Simulation
            composable(
                route = Screen.PricingSimulation.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val vm = PricingViewModel(
                    productRepository = app.productRepository,
                    ingredientRepository = app.ingredientRepository,
                    snapshotRepository = app.snapshotRepository,
                    preferencesRepository = app.userPreferencesRepository
                )
                PricingSimulationScreen(
                    productId = productId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToShare = { id -> navController.navigate(Screen.SharePreview.createRoute(id)) }
                )
            }

            // History
            composable(
                route = Screen.History.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val vm = HistoryViewModel(
                    snapshotRepository = app.snapshotRepository,
                    productRepository = app.productRepository,
                    ingredientRepository = app.ingredientRepository,
                    preferencesRepository = app.userPreferencesRepository
                )
                HistoryScreen(
                    productId = productId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Share Preview
            composable(
                route = Screen.SharePreview.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val vm = HistoryViewModel(
                    snapshotRepository = app.snapshotRepository,
                    productRepository = app.productRepository,
                    ingredientRepository = app.ingredientRepository,
                    preferencesRepository = app.userPreferencesRepository
                )
                SharePreviewScreen(
                    productId = productId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Backup & Restore
            composable(Screen.BackupRestore.route) {
                val vm = SettingsViewModel(app.userPreferencesRepository, app.database, app.backupManager)
                BackupRestoreScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // About
            composable(Screen.About.route) {
                AboutScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Receipt Scan OCR
            composable(Screen.ReceiptScan.route) {
                val vm = IngredientViewModel(app.ingredientRepository, app.userPreferencesRepository)
                com.kosio.app.ui.ingredients.ReceiptScanReviewScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
