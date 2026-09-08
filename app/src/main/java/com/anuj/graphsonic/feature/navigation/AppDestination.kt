package com.anuj.graphsonic.feature.navigation


sealed class AppDestination(
    val route: String
) {
    data object Equation : AppDestination("equation")
    data object History :
        AppDestination("history")
    data object Visualization : AppDestination("visualization")
}