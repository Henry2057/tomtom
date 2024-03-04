package com.example.tomtom.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.tomtom.manager.MapManager
import com.example.tomtom.manager.NavigationManager
import com.example.tomtom.manager.NavigationState
import com.example.tomtom.manager.RoutingManager
import com.example.tomtom.manager.SearchManager
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.search.ui.SearchFragment
import com.tomtom.sdk.search.ui.model.PlaceDetails
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val mapManager: MapManager,
    private val searchManager: SearchManager,
    private val routingManager: RoutingManager,
    private val navigationManager: NavigationManager,
) : ViewModel() {

    val searchResults: LiveData<PlaceDetails> = searchManager.searchResults

    val currentRoute: LiveData<Route> = routingManager.currentRoute

    val navigationState: LiveData<NavigationState> = navigationManager.navigationState

    fun initMap(mapFragment: MapFragment, onMapReady: (TomTomMap) -> Unit) = mapManager.initMap(mapFragment, onMapReady)

    fun enableUserLocation(activity: FragmentActivity, tomTomMap: TomTomMap?) = mapManager.enableUserLocation(activity, tomTomMap)

    fun initSearch(position: GeoPoint, fragment: SearchFragment) = searchManager.initSearch(position, fragment)

    fun planRoute(tomTomMap: TomTomMap?, destination: GeoPoint) = routingManager.planRoute(tomTomMap, destination)

    fun startNavigation(tomTomMap: TomTomMap?, route: Route, navigationFragment: NavigationFragment) = navigationManager.startNavigation(tomTomMap, route, navigationFragment )

    fun stopNavigation(tomTomMap: TomTomMap?, navigationFragment: NavigationFragment) = navigationManager.stopNavigation(tomTomMap, navigationFragment)

    fun close() {
        mapManager.close()
        searchManager.close()
        routingManager.close()
        navigationManager.close()
    }
}
