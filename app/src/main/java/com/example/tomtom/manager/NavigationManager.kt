package com.example.tomtom.manager

import androidx.lifecycle.LiveData
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.routing.route.Route
import java.io.Closeable

interface NavigationManager: Closeable {
    fun startNavigation(route: Route, navigationFragment: NavigationFragment)
    fun stopNavigation()

    val navigationState: LiveData<NavigationState>
}

enum class NavigationState {
    NAVIGATION_STARTED,
    NAVIGATION_STOPPED,
    IDLE,
}