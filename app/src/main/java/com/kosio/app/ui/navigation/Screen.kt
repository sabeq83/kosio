package com.kosio.app.ui.navigation

sealed class Screen(val route: String) {
    // Primary tabs
    data object Overview : Screen("overview")
    data object Products : Screen("products")
    data object Ingredients : Screen("ingredients")
    data object Settings : Screen("settings")

    // Onboarding
    data object Onboarding : Screen("onboarding")

    // Secondary screens
    data object IngredientForm : Screen("ingredient_form?ingredientId={ingredientId}") {
        fun createRoute(ingredientId: String? = null) = "ingredient_form" + if (ingredientId != null) "?ingredientId=$ingredientId" else ""
    }
    data object IngredientDetail : Screen("ingredient_detail/{ingredientId}") {
        fun createRoute(ingredientId: String) = "ingredient_detail/$ingredientId"
    }
    data object ProductForm : Screen("product_form?productId={productId}") {
        fun createRoute(productId: String? = null) = "product_form" + if (productId != null) "?productId=$productId" else ""
    }
    data object HppResult : Screen("hpp_result/{productId}") {
        fun createRoute(productId: String) = "hpp_result/$productId"
    }
    data object PricingSimulation : Screen("pricing_simulation/{productId}") {
        fun createRoute(productId: String) = "pricing_simulation/$productId"
    }
    data object History : Screen("history/{productId}") {
        fun createRoute(productId: String) = "history/$productId"
    }
    data object SharePreview : Screen("share_preview/{productId}") {
        fun createRoute(productId: String) = "share_preview/$productId"
    }
    data object BackupRestore : Screen("backup_restore")
    data object About : Screen("about")
    data object ReceiptScan : Screen("receipt_scan")
}
