package com.example.homieapp.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoutes {
    @Serializable
    data object HomeRoute: AppRoutes

    @Serializable
    data object DevicesRoute: AppRoutes

    @Serializable
    data object AlertsRoute: AppRoutes

    @Serializable
    data object GuideRoute: AppRoutes

    @Serializable
    data object HomieMobileRoute: AppRoutes
}