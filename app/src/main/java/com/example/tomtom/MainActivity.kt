package com.example.tomtom

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tomtom.BuildConfig.TOMTOM_API_KEY
import com.example.tomtom.dagger.MapFragmentFactory
import com.example.tomtom.dagger.NavigationFragmentFactory
import com.example.tomtom.dagger.SearchFragmentFactory
import com.example.tomtom.databinding.ActivityMainBinding
import com.example.tomtom.utils.PermissionUtils
import com.example.tomtom.viewmodel.MainViewModel
import com.tomtom.quantity.Distance
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.mapmatched.MapMatchedLocationProvider
import com.tomtom.sdk.location.simulation.SimulationLocationProvider
import com.tomtom.sdk.location.simulation.strategy.InterpolationStrategy
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraChangeListener
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.common.screen.Padding
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteClickListener
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.currentlocation.CurrentLocationButton.VisibilityPolicy
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
import com.tomtom.sdk.search.online.OnlineSearch
import com.tomtom.sdk.search.ui.SearchFragment
import com.tomtom.sdk.search.ui.SearchFragmentListener
import com.tomtom.sdk.search.ui.model.PlaceDetails
import com.tomtom.sdk.search.ui.model.SearchApiParameters
import com.tomtom.sdk.vehicle.Vehicle
import javax.inject.Inject

private const val ZOOM_TO_ROUTE_PADDING = 100
private val TAG = MainActivity::class.simpleName

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MainViewModel> { viewModelFactory }

    @Inject
    lateinit var mapFragmentFactory: MapFragmentFactory
    @Inject
    lateinit var searchFragmentFactory: SearchFragmentFactory
    @Inject
    lateinit var navigationFragmentFactory: NavigationFragmentFactory
    @Inject
    lateinit var tomTomNavigation: TomTomNavigation
    @Inject
    lateinit var navigationTileStore: NavigationTileStore
    @Inject
    lateinit var locationProvider: LocationProvider
    @Inject
    lateinit var routePlanner: RoutePlanner

    private lateinit var binding: ActivityMainBinding
    private lateinit var tomTomMap: TomTomMap
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener
    private var route: Route? = null
    private lateinit var routePlanningOptions: RoutePlanningOptions
    private lateinit var mapFragment: MapFragment
    private lateinit var searchFragment: SearchFragment
    private lateinit var navigationFragment: NavigationFragment

    private val apiKey = TOMTOM_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (application as MainApplication).appComponent.inject(this)
        initMap()
    }

    private fun initMap() {
        mapFragment = mapFragmentFactory.create()

        supportFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync { map ->
            tomTomMap = map
            enableUserLocation()
            setUpMapListeners()
            map.currentLocation?.let { initSearch(it.position) }
        }
    }

    private fun initSearch(position: GeoPoint) {
        val searchApi = OnlineSearch.create(this, apiKey)

        val searchApiParameters = SearchApiParameters(
            position = position
        )

        searchFragment = searchFragmentFactory.create(searchApiParameters)
        supportFragmentManager.beginTransaction()
            .replace(R.id.search_fragment_container, searchFragment)
            .commitNow()
        searchFragment.setSearchApi(searchApi)
        searchFragment.setFragmentListener(searchFragmentListener)
        searchFragment.enableSearchBackButton(true)
    }

    private val searchFragmentListener = object : SearchFragmentListener {
        override fun onSearchBackButtonClick() {
        }

        override fun onSearchResultClick(placeDetails: PlaceDetails) {
            calculateRouteTo(placeDetails.position)
            searchFragment.clear()
        }

        override fun onSearchError(throwable: Throwable) {
        }

        override fun onSearchQueryChanged(input: String) {

        }

        override fun onCommandInsert(command: String) {
        }
    }

    private fun enableUserLocation() {
        if (PermissionUtils.hasLocationPermissions(this)) {
            showUserLocation()
        } else {
            PermissionUtils.requestLocationPermissions(this)
        }
    }

    private fun showUserLocation() {
        locationProvider.enable()
        onLocationUpdateListener = OnLocationUpdateListener { location ->
            tomTomMap.moveCamera(CameraOptions(location.position, zoom = 8.0))
            locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)
        }
        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        tomTomMap.setLocationProvider(locationProvider)
        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        tomTomMap.enableLocationMarker(locationMarker)
    }

    private fun setUpMapListeners() {
        tomTomMap.addRouteClickListener(routeClickListener)
    }

    private fun isNavigationRunning(): Boolean = tomTomNavigation.navigationSnapshot != null

    private val routeClickListener = RouteClickListener {
        if (!isNavigationRunning()) {
            route?.let { route ->
                mapFragment.currentLocationButton.visibilityPolicy = VisibilityPolicy.Invisible
                startNavigation(route)
            }
        }
    }

    private fun calculateRouteTo(destination: GeoPoint) {
        val userLocation =
            tomTomMap.currentLocation?.position ?: return
        val itinerary = Itinerary(origin = userLocation, destination = destination)
        routePlanningOptions = RoutePlanningOptions(
            itinerary = itinerary,
            guidanceOptions = GuidanceOptions(
                phoneticsType = InstructionPhoneticsType.Ipa,
                extendedSections = ExtendedSections.All
            ),
            vehicle = Vehicle.Car()
        )
        routePlanner.planRoute(routePlanningOptions, routePlanningCallback)
    }

    private val routePlanningCallback = object : RoutePlanningCallback {
        override fun onSuccess(result: RoutePlanningResponse) {
            route = result.routes.first()
            drawRoute(route!!)
            tomTomMap.zoomToRoutes(ZOOM_TO_ROUTE_PADDING)
        }

        override fun onFailure(failure: RoutingFailure) {
        }

        override fun onRoutePlanned(route: Route) = Unit
    }

    private fun drawRoute(route: Route) {
        val instructions = route.mapInstructions()
        val routeOptions = RouteOptions(
            geometry = route.geometry,
            destinationMarkerVisible = true,
            departureMarkerVisible = true,
            instructions = instructions,
            routeOffset = route.routePoints.map { it.routeOffset }
        )
        tomTomMap.addRoute(routeOptions)
    }

    private fun Route.mapInstructions(): List<Instruction> {
        val routeInstructions = legs.flatMap { routeLeg -> routeLeg.instructions }
        return routeInstructions.map {
            Instruction(
                routeOffset = it.routeOffset
            )
        }
    }

    private fun startNavigation(route: Route) {
        initNavigationFragment()
        navigationFragment.setTomTomNavigation(tomTomNavigation)
        val routePlan = RoutePlan(route, routePlanningOptions)
        navigationFragment.startNavigation(routePlan)
        navigationFragment.addNavigationListener(navigationListener)
        tomTomNavigation.addProgressUpdatedListener(progressUpdatedListener)
        tomTomNavigation.addActiveRouteChangedListener(activeRouteChangedListener)
        tomTomNavigation.addLocationContextUpdatedListener(locationContextUpdatedListener)
        tomTomNavigation.addGuidanceUpdatedListener(guidanceUpdatedListener)
    }

    private val navigationListener = object : NavigationFragment.NavigationListener {
        override fun onStarted() {
            tomTomMap.addCameraChangeListener(cameraChangeListener)
            tomTomMap.cameraTrackingMode = CameraTrackingMode.FollowRouteDirection
            tomTomMap.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Chevron))
            setMapMatchedLocationProvider()
            setSimulationLocationProviderToNavigation(route!!)
            setMapNavigationPadding()
        }

        override fun onStopped() {
            stopNavigation()
        }
    }

    private fun initNavigationFragment() {
        if (!::navigationFragment.isInitialized) {
            navigationFragment = navigationFragmentFactory.create()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.navigation_fragment_container, navigationFragment)
            .commitNow()
    }

    private val progressUpdatedListener = ProgressUpdatedListener {
        tomTomMap.routes.first().progress = it.distanceAlongRoute
    }

    private val activeRouteChangedListener by lazy {
        ActiveRouteChangedListener { route ->
            tomTomMap.removeRoutes()
            drawRoute(route)
        }
    }

    private val locationContextUpdatedListener = LocationContextUpdatedListener { locationContext ->
        Log.e(TAG, "$locationContext")
    }

    private val guidanceUpdatedListener = object : GuidanceUpdatedListener {
        override fun onInstructionsChanged(instructions: List<GuidanceInstruction>) {
        }

        override fun onAnnouncementGenerated(
            announcement: GuidanceAnnouncement,
            shouldPlay: Boolean
        ) {
            Toast.makeText(this@MainActivity, announcement.plainTextMessage, Toast.LENGTH_SHORT)
                .show()
            Log.e(TAG, "ssmlMessage=${announcement.ssmlMessage}")
        }

        override fun onDistanceToNextInstructionChanged(
            distance: Distance,
            instructions: List<GuidanceInstruction>,
            currentPhase: InstructionPhase
        ) {
        }
    }

    private fun setSimulationLocationProviderToNavigation(route: Route) {
        val routeGeoLocations = route.geometry.map { GeoLocation(it) }
        val simulationStrategy = InterpolationStrategy(routeGeoLocations)
        locationProvider = SimulationLocationProvider.create(strategy = simulationStrategy)
        tomTomNavigation.locationProvider = locationProvider
        locationProvider.enable()
    }

    private fun stopNavigation() {
        navigationFragment.stopNavigation()
        mapFragment.currentLocationButton.visibilityPolicy =
            VisibilityPolicy.InvisibleWhenRecentered
        tomTomMap.removeCameraChangeListener(cameraChangeListener)
        tomTomMap.cameraTrackingMode = CameraTrackingMode.None
        tomTomMap.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Pointer))
        resetMapPadding()
        navigationFragment.removeNavigationListener(navigationListener)
        tomTomNavigation.removeProgressUpdatedListener(progressUpdatedListener)
        tomTomNavigation.removeActiveRouteChangedListener(activeRouteChangedListener)
        tomTomNavigation.removeLocationContextUpdatedListener(locationContextUpdatedListener)
        tomTomNavigation.removeGuidanceUpdatedListener(guidanceUpdatedListener)
        clearMap()
        enableUserLocation()
    }

    private fun setMapNavigationPadding() {
        val paddingBottom = resources.getDimensionPixelOffset(R.dimen.map_padding_bottom)
        val padding = Padding(0, 0, 0, paddingBottom)
        setPadding(padding)
    }

    private fun setPadding(padding: Padding) {
        val scale: Float = resources.displayMetrics.density
        val paddingInPixels = Padding(
            top = (padding.top * scale).toInt(),
            left = (padding.left * scale).toInt(),
            right = (padding.right * scale).toInt(),
            bottom = (padding.bottom * scale).toInt()
        )
        tomTomMap.setPadding(paddingInPixels)
    }

    private fun resetMapPadding() {
        tomTomMap.setPadding(Padding(0, 0, 0, 0))
    }

    private fun setMapMatchedLocationProvider() {
        val mapMatchedLocationProvider = MapMatchedLocationProvider(tomTomNavigation)
        tomTomMap.setLocationProvider(mapMatchedLocationProvider)
        mapMatchedLocationProvider.enable()
    }

    private fun clearMap() {
        tomTomMap.clear()
    }

    private val cameraChangeListener by lazy {
        CameraChangeListener {
            val cameraTrackingMode = tomTomMap.cameraTrackingMode
            if (cameraTrackingMode == CameraTrackingMode.FollowRouteDirection) {
                navigationFragment.navigationView.showSpeedView()
            } else {
                navigationFragment.navigationView.hideSpeedView()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionUtils.handlePermissionsResult(requestCode, permissions, grantResults,
            onPermissionGranted = {
                showUserLocation()
            },
            onPermissionDenied = {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    override fun onDestroy() {
        tomTomMap.setLocationProvider(null)
        super.onDestroy()
        tomTomNavigation.close()
        navigationTileStore.close()
        locationProvider.close()
    }
}
