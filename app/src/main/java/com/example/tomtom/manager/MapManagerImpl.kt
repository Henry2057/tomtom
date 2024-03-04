package com.example.tomtom.manager

import androidx.fragment.app.FragmentActivity
import com.example.tomtom.util.PermissionUtil
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import javax.inject.Singleton

@Singleton
class MapManagerImpl(
    private val locationProvider: LocationProvider,
) : MapManager {
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener

    override fun initMap(mapFragment: MapFragment, onMapReady: (TomTomMap) -> Unit) {
        mapFragment.getMapAsync { map ->
            onMapReady(map)
        }
    }

    override fun enableUserLocation(activity: FragmentActivity, tomtomMap: TomTomMap?) {
        tomtomMap?.let {
            if (PermissionUtil.hasLocationPermissions(activity)) {
                showUserLocation(it)
            } else {
                PermissionUtil.requestLocationPermissions(activity)
            }
        }
    }

    private fun showUserLocation(tomtomMap: TomTomMap) {
        locationProvider.enable()
        onLocationUpdateListener = OnLocationUpdateListener { location ->
            tomtomMap.moveCamera(CameraOptions(location.position, zoom = 8.0))
            locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)
        }
        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        tomtomMap.setLocationProvider(locationProvider)
        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        tomtomMap.enableLocationMarker(locationMarker)
    }

    override fun close() {
        locationProvider.close()
    }
}
