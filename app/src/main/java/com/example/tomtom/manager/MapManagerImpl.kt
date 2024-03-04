package com.example.tomtom.manager

import androidx.fragment.app.FragmentActivity
import com.example.tomtom.util.PermissionUtil
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import java.io.Closeable
import javax.inject.Singleton

@Singleton
class MapManagerImpl(
    private val locationProvider: LocationProvider,
) : MapManager {
    private var _tomTomMap: TomTomMap? = null
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener

    override val tomTomMap: TomTomMap?
        get() = _tomTomMap

    override fun initMap(mapFragment: MapFragment, onMapReady: (TomTomMap) -> Unit) {
        mapFragment.getMapAsync { map ->
            _tomTomMap = map
            onMapReady(map)
        }
    }

    override fun enableUserLocation(activity: FragmentActivity) {
        if (PermissionUtil.hasLocationPermissions(activity)) {
            showUserLocation()
        } else {
            PermissionUtil.requestLocationPermissions(activity)
        }
    }

    private fun showUserLocation() {
        locationProvider.enable()
        onLocationUpdateListener = OnLocationUpdateListener { location ->
            tomTomMap?.moveCamera(CameraOptions(location.position, zoom = 8.0))
            locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)
        }
        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        tomTomMap?.setLocationProvider(locationProvider)
        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        tomTomMap?.enableLocationMarker(locationMarker)
    }

    override fun close() {
        locationProvider.close()
    }
}
