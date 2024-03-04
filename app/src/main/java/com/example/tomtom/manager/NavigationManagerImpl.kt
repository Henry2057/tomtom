package com.example.tomtom.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.mapmatched.MapMatchedLocationProvider
import com.tomtom.sdk.location.simulation.SimulationLocationProvider
import com.tomtom.sdk.location.simulation.strategy.InterpolationStrategy
import com.tomtom.sdk.map.display.camera.CameraChangeListener
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.common.screen.Padding
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.map.display.ui.currentlocation.CurrentLocationButton
import com.tomtom.sdk.navigation.ActiveRouteChangedListener
import com.tomtom.sdk.navigation.GuidanceUpdatedListener
import com.tomtom.sdk.navigation.LocationContextUpdatedListener
import com.tomtom.sdk.navigation.ProgressUpdatedListener
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.guidance.GuidanceAnnouncement
import com.tomtom.sdk.navigation.guidance.InstructionPhase
import com.tomtom.sdk.navigation.guidance.instruction.GuidanceInstruction
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.routing.route.Route

private const val ZOOM_TO_ROUTE_PADDING = 100
private var TAG = NavigationManagerImpl::class.java.simpleName
class NavigationManagerImpl(
    private val tomTomNavigation: TomTomNavigation,
    private val mapManager: MapManager,
    private val routingManager: RoutingManager,
) : NavigationManager {
    private lateinit var locationProvider: LocationProvider

    private val _navigationState = MutableLiveData<NavigationState>()
    override val navigationState: LiveData<NavigationState>
        get() = _navigationState

    override fun startNavigation(route: Route, navigationFragment: NavigationFragment) {
        navigationFragment.setTomTomNavigation(tomTomNavigation)
        val routePlan = RoutePlan(route, routingManager.routePlanningOptions.value!!)
        navigationFragment.startNavigation(routePlan)
        navigationFragment.addNavigationListener(navigationListener)
        tomTomNavigation.addProgressUpdatedListener(progressUpdatedListener)
        tomTomNavigation.addActiveRouteChangedListener(activeRouteChangedListener)
        tomTomNavigation.addLocationContextUpdatedListener(locationContextUpdatedListener)
        tomTomNavigation.addGuidanceUpdatedListener(guidanceUpdatedListener)
    }

    override fun stopNavigation() {
//        navigationFragment.stopNavigation()
//        mapFragment.currentLocationButton.visibilityPolicy =
//            CurrentLocationButton.VisibilityPolicy.InvisibleWhenRecentered
//        mapManager.tomTomMap?.removeCameraChangeListener(cameraChangeListener)
//        mapManager.tomTomMap?.cameraTrackingMode = CameraTrackingMode.None
//        mapManager.tomTomMap?.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Pointer))
//        resetMapPadding()
//        navigationFragment.removeNavigationListener(navigationListener)
//        tomTomNavigation.removeProgressUpdatedListener(progressUpdatedListener)
//        tomTomNavigation.removeActiveRouteChangedListener(activeRouteChangedListener)
//        tomTomNavigation.removeLocationContextUpdatedListener(locationContextUpdatedListener)
//        tomTomNavigation.removeGuidanceUpdatedListener(guidanceUpdatedListener)
//        clearMap()
//        mapManager.enableUserLocation(this)
        TODO()
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
        Log.d(TAG, "drawRoute: +++")
        mapManager.tomTomMap?.let {
            Log.d(TAG, "drawRoute: not null")
            it.addRoute(routeOptions)
        } ?: run {
            Log.d(TAG, "drawRoute: TomTomMap null")
        }
    }

    private val navigationListener = object : NavigationFragment.NavigationListener {
        override fun onStarted() {
//            mapManager.tomTomMap?.addCameraChangeListener(cameraChangeListener)
            mapManager.tomTomMap?.cameraTrackingMode = CameraTrackingMode.FollowRouteDirection
            mapManager.tomTomMap?.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Chevron))
            setMapMatchedLocationProvider()
            routingManager.currentRoute.value?.let { route ->
                setSimulationLocationProviderToNavigation(route)
            }
//            setMapNavigationPadding()
        }

        override fun onStopped() {
            stopNavigation()
        }
    }

    private val progressUpdatedListener = ProgressUpdatedListener {
        mapManager.tomTomMap?.routes?.first()?.progress = it.distanceAlongRoute
    }

    private val activeRouteChangedListener by lazy {
        ActiveRouteChangedListener { route ->
            mapManager.tomTomMap?.removeRoutes()
            drawRoute(route)
        }
    }

    private val locationContextUpdatedListener = LocationContextUpdatedListener { locationContext ->
        Log.d(TAG, "$locationContext")
    }

    private val guidanceUpdatedListener = object : GuidanceUpdatedListener {
        override fun onInstructionsChanged(instructions: List<GuidanceInstruction>) {
        }

        override fun onAnnouncementGenerated(
            announcement: GuidanceAnnouncement,
            shouldPlay: Boolean
        ) {
//            Toast.makeText(this@MainActivity, announcement.plainTextMessage, Toast.LENGTH_SHORT)
//                .show()
            Log.d(TAG, "ssmlMessage=${announcement.ssmlMessage}")
        }

        override fun onDistanceToNextInstructionChanged(
            distance: Distance,
            instructions: List<GuidanceInstruction>,
            currentPhase: InstructionPhase
        ) {
        }
    }

//    private val cameraChangeListener by lazy {
//        CameraChangeListener {
//            val cameraTrackingMode = mapManager.tomTomMap?.cameraTrackingMode
//            if (cameraTrackingMode == CameraTrackingMode.FollowRouteDirection) {
//                navigationFragment.navigationView.showSpeedView()
//            } else {
//                navigationFragment.navigationView.hideSpeedView()
//            }
//        }
//    }

    private fun Route.mapInstructions(): List<Instruction> {
        val routeInstructions = legs.flatMap { routeLeg -> routeLeg.instructions }
        return routeInstructions.map {
            Instruction(
                routeOffset = it.routeOffset
            )
        }
    }

    private fun setMapMatchedLocationProvider() {
        val mapMatchedLocationProvider = MapMatchedLocationProvider(tomTomNavigation)
        mapManager.tomTomMap?.setLocationProvider(mapMatchedLocationProvider)
        mapMatchedLocationProvider.enable()
    }

    private fun setSimulationLocationProviderToNavigation(route: Route) {
        val routeGeoLocations = route.geometry.map { GeoLocation(it) }
        val simulationStrategy = InterpolationStrategy(routeGeoLocations)
        locationProvider = SimulationLocationProvider.create(strategy = simulationStrategy)
        tomTomNavigation.locationProvider = locationProvider
        locationProvider.enable()
    }

    private fun isNavigationRunning(): Boolean = tomTomNavigation.navigationSnapshot != null

    private fun setMapNavigationPadding() {
//        val paddingBottom = resources.getDimensionPixelOffset(R.dimen.map_padding_bottom)
        val padding = Padding(0, 0, 0, 263)
        setPadding(padding)
    }

    private fun setPadding(padding: Padding) {
        val scale: Float =  5F//resources.displayMetrics.density
        val paddingInPixels = Padding(
            top = (padding.top * scale).toInt(),
            left = (padding.left * scale).toInt(),
            right = (padding.right * scale).toInt(),
            bottom = (padding.bottom * scale).toInt()
        )
        mapManager.tomTomMap?.setPadding(paddingInPixels)
    }

    override fun close() {
        tomTomNavigation.close()
    }
}