package com.example.tomtom.manager

import androidx.lifecycle.LiveData
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.routing.route.Route
import java.io.Closeable

interface NavigationManager: Closeable {
    fun startNavigation(tomTomMap: TomTomMap?, route: Route, navigationFragment: NavigationFragment)
    fun stopNavigation(tomTomMap: TomTomMap?, navigationFragment: NavigationFragment)

    val navigationState: LiveData<NavigationState>
}

enum class NavigationState {
    NAVIGATION_STARTED,
    NAVIGATION_STOPPED,
    IDLE,
}