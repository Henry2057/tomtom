package com.example.tomtom

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tomtom.dagger.MapFragmentFactory
import com.example.tomtom.dagger.NavigationFragmentFactory
import com.example.tomtom.dagger.SearchFragmentFactory
import com.example.tomtom.databinding.ActivityMainBinding
import com.example.tomtom.manager.MapManager
import com.example.tomtom.util.PermissionUtil
import com.example.tomtom.viewmodel.MainViewModel
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.route.RouteClickListener
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.currentlocation.CurrentLocationButton.VisibilityPolicy
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.search.ui.SearchFragment
import com.tomtom.sdk.search.ui.model.SearchApiParameters
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
    lateinit var mapManager: MapManager

    private lateinit var binding: ActivityMainBinding
    private lateinit var tomTomMap: TomTomMap
    private lateinit var mapFragment: MapFragment
    private lateinit var searchFragment: SearchFragment
    private lateinit var navigationFragment: NavigationFragment

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

        viewModel.initMap(mapFragment) { map ->
            viewModel.enableUserLocation(this)
            tomTomMap = map
            setUpMapListeners()
            map.currentLocation?.let { initSearch(it.position) }
        }
    }

    private fun initSearch(position: GeoPoint) {
        val searchApiParameters = SearchApiParameters(
            position = position
        )
        searchFragment = searchFragmentFactory.create(searchApiParameters)
        supportFragmentManager.beginTransaction()
            .replace(R.id.search_fragment_container, searchFragment)
            .commitNow()
        viewModel.initSearch(position, searchFragment)
        viewModel.searchResults.observe(this) { placeDetails ->
            viewModel.planRoute(
                mapManager.tomTomMap?.currentLocation?.position,
                placeDetails.position
            )
            searchFragment.clear()
        }
    }

    private fun setUpMapListeners() {
        mapManager.tomTomMap?.addRouteClickListener(routeClickListener)
    }

    private fun isNavigationRunning(): Boolean = tomTomNavigation.navigationSnapshot != null

    private val routeClickListener = RouteClickListener {
        if (!isNavigationRunning()) {
            viewModel.currentRoute.value?.let { route ->
                mapFragment.currentLocationButton.visibilityPolicy = VisibilityPolicy.Invisible
                startNavigation(route)
            }
        }
    }


    private fun startNavigation(route: Route) {
        initNavigationFragment()
        viewModel.startNavigation(route, navigationFragment)
    }


    private fun initNavigationFragment() {
        navigationFragment = navigationFragmentFactory.create()
        supportFragmentManager.beginTransaction()
            .replace(R.id.navigation_fragment_container, navigationFragment)
            .commitNow()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionUtil.handlePermissionsResult(requestCode, permissions, grantResults,
            onPermissionGranted = {
                viewModel.enableUserLocation(this)
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
        super.onDestroy()
        viewModel.close()
    }
}
