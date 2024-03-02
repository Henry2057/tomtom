package com.example.tomtom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tomtom.repo.NavigationRepository
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.route.Route
import javax.inject.Inject

class MainViewModel @Inject constructor(private val navigationRepository: NavigationRepository) : ViewModel() {

    private val _location = MutableLiveData<GeoLocation>()
    private val _route = MutableLiveData<Route>()

    val location: LiveData<GeoLocation> = _location
    val route: LiveData<Route> = _route

    fun initializeLocationProvider() {
        // Initialize and start the location provider, update _location LiveData
    }

    fun planRoute(destination: GeoPoint) {
        // Use the repository to plan a route, update _route LiveData
    }

    fun updateLocation(location: GeoLocation) {
        _location.postValue(location)
    }

    // Add more functions for handling user actions and updating state
}
