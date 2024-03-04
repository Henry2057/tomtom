package com.example.tomtom.manager

import androidx.fragment.app.FragmentActivity
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.ui.MapFragment
import java.io.Closeable

interface MapManager: Closeable {
    fun initMap(mapFragment: MapFragment, onMapReady: (TomTomMap) -> Unit)
    fun enableUserLocation(activity: FragmentActivity, tomtomMap: TomTomMap?)
}
