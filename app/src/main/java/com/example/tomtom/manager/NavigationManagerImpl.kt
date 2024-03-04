package com.example.tomtom.manager

import android.content.res.Resources
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tomtom.R
import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.mapmatched.MapMatchedLocationProvider
import com.tomtom.sdk.location.simulation.SimulationLocationProvider
import com.tomtom.sdk.location.simulation.strategy.InterpolationStrategy
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraChangeListener
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.common.screen.Padding
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.navigation.ActiveRouteChangedListener
import com.tomtom.sdk.navigation.GuidanceUpdatedListener
import com.tomtom.sdk.navigation.ProgressUpdatedListener
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.guidance.GuidanceAnnouncement
import com.tomtom.sdk.navigation.guidance.InstructionPhase
import com.tomtom.sdk.navigation.guidance.instruction.GuidanceInstruction
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.routing.route.Route

private var TAG = NavigationManagerImpl::class.java.simpleName

class NavigationManagerImpl(
    private val tomTomNavigation: TomTomNavigation,
    private val routingManager: RoutingManager,
) : NavigationManager {
    private lateinit var locationProvider: LocationProvider

    private lateinit var navigationListener: NavigationFragment.NavigationListener
    private lateinit var progressUpdatedListener: ProgressUpdatedListener
    private lateinit var activeRouteChangedListener: ActiveRouteChangedListener
    private lateinit var guidanceUpdatedListener: GuidanceUpdatedListener
    private lateinit var cameraChangeListener: CameraChangeListener

    private val _navigationState = MutableLiveData<NavigationState>()
    override val navigationState: LiveData<NavigationState>
        get() = _navigationState

    override fun startNavigation(
        tomTomMap: TomTomMap?,
        route: Route,
        navigationFragment: NavigationFragment
    ) {
        navigationFragment.setTomTomNavigation(tomTomNavigation)
        val routePlan = RoutePlan(route, routingManager.routePlanningOptions.value!!)
        navigationFragment.startNavigation(routePlan)
        initListeners(tomTomMap, navigationFragment)
        navigationFragment.addNavigationListener(navigationListener)
        tomTomNavigation.addProgressUpdatedListener(progressUpdatedListener)
        tomTomNavigation.addActiveRouteChangedListener(activeRouteChangedListener)
        tomTomNavigation.addGuidanceUpdatedListener(guidanceUpdatedListener)
    }

    override fun stopNavigation(
        tomTomMap: TomTomMap?,
        navigationFragment: NavigationFragment
    ) {
        navigationFragment.stopNavigation()
        tomTomMap?.removeCameraChangeListener(cameraChangeListener)
        tomTomMap?.cameraTrackingMode = CameraTrackingMode.None
        tomTomMap?.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Pointer))
        resetMapPadding(tomTomMap)
        navigationFragment.removeNavigationListener(navigationListener)
        tomTomNavigation.removeProgressUpdatedListener(progressUpdatedListener)
        tomTomNavigation.removeActiveRouteChangedListener(activeRouteChangedListener)
        tomTomNavigation.removeGuidanceUpdatedListener(guidanceUpdatedListener)
        tomTomMap?.clear()
        _navigationState.postValue(NavigationState.NAVIGATION_STOPPED)
    }

    private fun resetMapPadding(tomTomMap: TomTomMap?) {
        tomTomMap?.setPadding(Padding(0, 0, 0, 0))
    }

    private fun initListeners(tomTomMap: TomTomMap?, navigationFragment: NavigationFragment) {
        cameraChangeListener = createCameraChangeListener(tomTomMap, navigationFragment)
        navigationListener = createNavigationListener(tomTomMap, navigationFragment)
        progressUpdatedListener = createProgressUpdatedListener(tomTomMap)
        activeRouteChangedListener = createActiveRouteChangedListener(tomTomMap)
        guidanceUpdatedListener = createGuidanceListener(navigationFragment)
    }

    private fun drawRoute(tomTomMap: TomTomMap?, route: Route) {
        val instructions = route.mapInstructions()
        val routeOptions = RouteOptions(
            geometry = route.geometry,
            destinationMarkerVisible = true,
            departureMarkerVisible = true,
            instructions = instructions,
            routeOffset = route.routePoints.map { it.routeOffset }
        )
        tomTomMap?.addRoute(routeOptions)
    }

    private fun createNavigationListener(tomTomMap: TomTomMap?, navigationFragment: NavigationFragment): NavigationFragment.NavigationListener {
        return object : NavigationFragment.NavigationListener {
            override fun onStarted() {
                tomTomMap?.apply {
                    addCameraChangeListener(cameraChangeListener)
                    cameraTrackingMode = CameraTrackingMode.FollowRouteDirection
                    enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Chevron))
                }
                setMapMatchedLocationProvider(tomTomMap)
                routingManager.currentRoute.value?.let { route ->
                    setSimulationLocationProviderToNavigation(route)
                }
                 setMapNavigationPadding(tomTomMap, navigationFragment.resources)
            }

            override fun onStopped() {
                stopNavigation(tomTomMap, navigationFragment)
            }
        }
    }

    private fun createProgressUpdatedListener(tomTomMap: TomTomMap?): ProgressUpdatedListener {
        return ProgressUpdatedListener {
            tomTomMap?.routes?.first()?.progress = it.distanceAlongRoute
        }
    }

    private fun createActiveRouteChangedListener(tomTomMap: TomTomMap?): ActiveRouteChangedListener {
        return ActiveRouteChangedListener { route ->
            tomTomMap?.removeRoutes()
            drawRoute(tomTomMap, route)
        }
    }

    private fun createGuidanceListener(fragment: NavigationFragment): GuidanceUpdatedListener {
        return object : GuidanceUpdatedListener {
            override fun onInstructionsChanged(instructions: List<GuidanceInstruction>) {

            }

            override fun onAnnouncementGenerated(
                announcement: GuidanceAnnouncement,
                shouldPlay: Boolean
            ) {
                fragment.activity?.runOnUiThread {
                    Toast.makeText(
                        fragment.activity,
                        announcement.plainTextMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.d(TAG, "ssmlMessage=${announcement.ssmlMessage}")
            }

            override fun onDistanceToNextInstructionChanged(
                distance: Distance,
                instructions: List<GuidanceInstruction>,
                currentPhase: InstructionPhase
            ) {

            }
        }
    }

    private fun createCameraChangeListener(
        tomTomMap: TomTomMap?,
        navigationFragment: NavigationFragment
    ) =
        CameraChangeListener {
            val cameraTrackingMode = tomTomMap?.cameraTrackingMode
            if (cameraTrackingMode == CameraTrackingMode.FollowRouteDirection) {
                navigationFragment.navigationView.showSpeedView()
            } else {
                navigationFragment.navigationView.hideSpeedView()
            }
        }

    private fun Route.mapInstructions(): List<Instruction> {
        val routeInstructions = legs.flatMap { routeLeg -> routeLeg.instructions }
        return routeInstructions.map {
            Instruction(
                routeOffset = it.routeOffset
            )
        }
    }

    private fun setMapMatchedLocationProvider(tomTomMap: TomTomMap?) {
        val mapMatchedLocationProvider = MapMatchedLocationProvider(tomTomNavigation)
        tomTomMap?.setLocationProvider(mapMatchedLocationProvider)
        mapMatchedLocationProvider.enable()
    }

    private fun setSimulationLocationProviderToNavigation(route: Route) {
        val routeGeoLocations = route.geometry.map { GeoLocation(it) }
        val simulationStrategy = InterpolationStrategy(routeGeoLocations)
        locationProvider = SimulationLocationProvider.create(strategy = simulationStrategy)
        tomTomNavigation.locationProvider = locationProvider
        locationProvider.enable()
    }

    private fun setMapNavigationPadding(tomTomMap: TomTomMap?, resources: Resources) {
        val paddingBottom = resources.getDimensionPixelOffset(R.dimen.map_padding_bottom)
        val padding = Padding(0, 0, 0, paddingBottom)
        setPadding(tomTomMap, resources, padding)
    }

    private fun setPadding(tomTomMap: TomTomMap?, resources: Resources, padding: Padding) {
        val scale: Float = resources.displayMetrics.density
        val paddingInPixels = Padding(
            top = (padding.top * scale).toInt(),
            left = (padding.left * scale).toInt(),
            right = (padding.right * scale).toInt(),
            bottom = (padding.bottom * scale).toInt()
        )
        tomTomMap?.setPadding(paddingInPixels)
    }

    override fun close() {
        tomTomNavigation.close()
    }
}