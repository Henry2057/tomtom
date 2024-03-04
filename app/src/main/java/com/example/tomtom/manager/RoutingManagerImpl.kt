package com.example.tomtom.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.guidance.ExtendedSections
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.routing.options.guidance.InstructionPhoneticsType
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.vehicle.Vehicle
import java.io.Closeable
import javax.inject.Singleton

private const val ZOOM_TO_ROUTE_PADDING = 100
private var TAG = RoutingManagerImpl::class.java.simpleName
@Singleton
class RoutingManagerImpl(
    private val routePlanner: RoutePlanner,
    private val mapManager: MapManager,
) : RoutingManager {
    private val _currentRoute = MutableLiveData<Route>()
    private val _routePlanningOptions = MutableLiveData<RoutePlanningOptions>()

    override val currentRoute: LiveData<Route>
        get() = _currentRoute
    override val routePlanningOptions: LiveData<RoutePlanningOptions>
        get() = _routePlanningOptions

    override fun planRoute(origin: GeoPoint?, destination: GeoPoint) {
        if (origin == null) return
        val itinerary = Itinerary(origin = origin, destination = destination)
        val routePlanningOptions = RoutePlanningOptions(
            itinerary = itinerary,
            guidanceOptions = GuidanceOptions(
                phoneticsType = InstructionPhoneticsType.Ipa,
                extendedSections = ExtendedSections.All
            ),
            vehicle = Vehicle.Car()
        )
        _routePlanningOptions.postValue(routePlanningOptions)
        routePlanner.planRoute(routePlanningOptions, routePlanningCallback)
    }

    private val routePlanningCallback = object : RoutePlanningCallback {
        override fun onSuccess(result: RoutePlanningResponse) {
            val route = result.routes.first()
            _currentRoute.postValue(route)
            drawRoute(route)
            mapManager.tomTomMap?.zoomToRoutes(ZOOM_TO_ROUTE_PADDING)
        }

        override fun onFailure(failure: RoutingFailure) {
            Log.d(TAG, "onFailure: $failure")
        }

        override fun onRoutePlanned(route: Route) {
            Log.d(TAG, "onRoutePlanned: $route")
        }
    }

    private fun drawRoute(route: Route) {
        Log.d(TAG, "drawRoute: ")
        val instructions = route.mapInstructions()
        val routeOptions = RouteOptions(
            geometry = route.geometry,
            destinationMarkerVisible = true,
            departureMarkerVisible = true,
            instructions = instructions,
            routeOffset = route.routePoints.map { it.routeOffset }
        )
        mapManager.tomTomMap?.addRoute(routeOptions)
    }

    private fun Route.mapInstructions(): List<Instruction> {
        val routeInstructions = legs.flatMap { routeLeg -> routeLeg.instructions }
        return routeInstructions.map {
            Instruction(
                routeOffset = it.routeOffset
            )
        }
    }

    override fun close() {
        routePlanner.close()
    }
}
