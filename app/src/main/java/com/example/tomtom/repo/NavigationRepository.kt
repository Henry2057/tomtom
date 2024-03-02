package com.example.tomtom.repo

import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.route.Route

class NavigationRepository(private val routePlanner: RoutePlanner) {

    interface RoutePlanningCallback {
        fun onSuccess(route: Route)
        fun onFailure(error: String)
    }

    fun planRoute(destination: GeoPoint, callback: RoutePlanningCallback) {
        // Use routePlanner to plan a route
        // On success, call callback.onSuccess
        // On failure, call callback.onFailure
    }
}
