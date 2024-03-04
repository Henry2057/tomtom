package com.example.tomtom.manager

import androidx.lifecycle.LiveData
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.search.ui.SearchFragment
import com.tomtom.sdk.search.ui.model.PlaceDetails
import java.io.Closeable

interface SearchManager: Closeable {
    fun initSearch(position: GeoPoint, fragment: SearchFragment)

    val searchResults: LiveData<PlaceDetails>
}