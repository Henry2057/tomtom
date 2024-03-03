package com.example.tomtom.repo

import android.content.Context
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.routing.RoutePlanningCallback

class MapRepository(private val context: Context) {
    private var tomTomMap: TomTomMap? = null

    fun initMap(mapFragment: MapFragment, onMapReady: (TomTomMap) -> Unit) {
        if (tomTomMap == null) {
            mapFragment.getMapAsync { map ->
                tomTomMap = map
                configureInitialSettings(map)
                onMapReady(map)
            }
        } else {
            tomTomMap?.let { onMapReady(it) }
        }
    }

    private fun configureInitialSettings(tomTomMap: TomTomMap) {
        // Set initial map settings, like zoom level, center point, etc.
    }

    fun planRoute(destination: GeoPoint, routeCallback: RoutePlanningCallback) {
        tomTomMap?.let { map ->
            // Use the map object to plan a route
            // routeCallback.onSuccess() or routeCallback.onFailure() based on the outcome
        } ?: run {
            // Handle the case where tomTomMap is not initialized
        }
    }

    fun updateLocation(location: GeoLocation) {
        tomTomMap?.let { map ->
            // Update the map's location marker based on the new location
        }
    }
}
