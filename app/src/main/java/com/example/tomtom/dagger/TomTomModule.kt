package com.example.tomtom.dagger

import android.content.Context
import com.example.tomtom.BuildConfig
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStoreConfiguration
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.android.AndroidLocationProvider
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.online.Configuration as NavigationConfiguration
import com.tomtom.sdk.navigation.online.OnlineTomTomNavigationFactory
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.navigation.ui.NavigationUiOptions
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.search.online.OnlineSearch
import com.tomtom.sdk.search.ui.SearchFragment
import com.tomtom.sdk.search.ui.model.SearchApiParameters
import com.tomtom.sdk.search.ui.model.SearchProperties
import com.tomtom.sdk.vehicle.Vehicle
import com.tomtom.sdk.vehicle.VehicleProviderFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TomTomModule {

    @Singleton
    @Provides
    fun provideNavigationTileStore(context: Context): NavigationTileStore {
        return NavigationTileStore.create(
            context, NavigationTileStoreConfiguration(apiKey = BuildConfig.TOMTOM_API_KEY)
        )
    }

    @Singleton
    @Provides
    fun provideLocationProvider(context: Context): LocationProvider =
        AndroidLocationProvider(context)

    @Singleton
    @Provides
    fun provideRoutePlanner(context: Context): RoutePlanner =
        OnlineRoutePlanner.create(context, BuildConfig.TOMTOM_API_KEY)

    @Singleton
    @Provides
    fun provideTomTomNavigation(
        context: Context,
        navigationTileStore: NavigationTileStore,
        locationProvider: LocationProvider,
        routePlanner: RoutePlanner
    ): TomTomNavigation {
        val configuration = NavigationConfiguration(
            context = context,
            navigationTileStore = navigationTileStore,
            locationProvider = locationProvider,
            routePlanner = routePlanner,
            vehicleProvider = VehicleProviderFactory.create(vehicle = Vehicle.Pedestrian())
        )
        return OnlineTomTomNavigationFactory.create(configuration)
    }

    @Singleton
    @Provides
    fun provideMapFragmentFactory(): MapFragmentFactory = object : MapFragmentFactory {
        override fun create(): MapFragment {
            val mapOptions = MapOptions(mapKey = BuildConfig.TOMTOM_API_KEY)
            return MapFragment.newInstance(mapOptions)
        }
    }

    @Singleton
    @Provides
    fun provideSearchFragmentFactory(context: Context): SearchFragmentFactory = object :
        SearchFragmentFactory {
        override fun create(searchApiParameters: SearchApiParameters): SearchFragment {
            val searchProperties = SearchProperties(
                searchApiKey = BuildConfig.TOMTOM_API_KEY,
                searchApiParameters = searchApiParameters
            )

            return SearchFragment.newInstance(searchProperties).apply {
                setSearchApi(OnlineSearch.create(context, BuildConfig.TOMTOM_API_KEY))
            }
        }
    }

    @Singleton
    @Provides
    fun provideNavigationFragmentFactory(): NavigationFragmentFactory = object :
        NavigationFragmentFactory {
        override fun create(): NavigationFragment {
            return NavigationFragment.newInstance(NavigationUiOptions(keepInBackground = true))
        }
    }

}
