package com.example.homieapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.homieapp.core.model.HomieMobile
import com.example.homieapp.features.home.HomeScreen
import com.example.homieapp.features.devicepreview.DevicePreviewScreen
import com.example.homieapp.features.guides.GuideScreen
import com.example.homieapp.features.alerts.AlertsScreen
import com.example.homieapp.features.homiemobile.HomieMobileScreen
import com.example.homieapp.navigation.AppRoutes.HomeRoute
import com.example.homieapp.navigation.AppRoutes.DevicesRoute
import com.example.homieapp.navigation.AppRoutes.AlertsRoute
import com.example.homieapp.navigation.AppRoutes.GuideRoute
import com.example.homieapp.navigation.AppRoutes.HomieMobileRoute


@Composable
fun AppNavigation(
    homieMobile: HomieMobile,
    onRegisterDevice: (String) -> Unit,
    backStack: SnapshotStateList<AppRoutes>
) {
    var guidePage by rememberSaveable { mutableIntStateOf(0) }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(
                    homieMobile = homieMobile,
                    onNavigateToGuide = {
                        backStack.add(GuideRoute)
                    }
                )
            }

            entry<DevicesRoute> {
                DevicePreviewScreen(
                    homieMobile = homieMobile,
                    onNavigateToHomieMobile = {
                        backStack.add(HomieMobileRoute)
                    },
                    onRegisterDevice = onRegisterDevice
                )
            }

            entry<HomieMobileRoute> {
                HomieMobileScreen(
                    homieMobile = homieMobile,
                    onNavigateToDevice = {
                        backStack.add(DevicesRoute)
                    },
                    onNavigateToGuide = {
                        backStack.add(GuideRoute)
                    }
                )
            }

            entry<AlertsRoute> {
                AlertsScreen(homieMobile = homieMobile)
            }

            entry<GuideRoute> {
                GuideScreen(
                    guidePage = guidePage,
                    onNavigateToHome = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        } else {
                            // If we were at Home already, this shouldn't happen usually with nav3 back handling
                            // but ensuring we have a home.
                            if (backStack.none { it is HomeRoute }) {
                                backStack.add(0, HomeRoute)
                            }
                        }
                    },
                    addPage = { guidePage += 1 },
                    restPage = { guidePage -= 1 }
                )
            }
        }
    )
}
