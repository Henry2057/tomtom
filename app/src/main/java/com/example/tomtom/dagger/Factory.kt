package com.example.tomtom.dagger

import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.search.ui.SearchFragment
import com.tomtom.sdk.search.ui.model.SearchApiParameters

interface MapFragmentFactory {
    fun create(): MapFragment
}

interface SearchFragmentFactory {
    fun create(searchApiParameters: SearchApiParameters): SearchFragment
}

interface NavigationFragmentFactory {
    fun create(): NavigationFragment
}
