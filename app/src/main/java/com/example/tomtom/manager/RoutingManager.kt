package com.example.tomtom.manager

import androidx.lifecycle.LiveData
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.route.Route
import java.io.Closeable

interface RoutingManager: Closeable {

    val currentRoute: LiveData<Route>

    val routePlanningOptions: LiveData<RoutePlanningOptions>
    fun planRoute(origin: GeoPoint?, destination: GeoPoint)
}