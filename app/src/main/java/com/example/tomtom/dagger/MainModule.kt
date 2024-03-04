package com.example.tomtom.dagger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tomtom.manager.MapManager
import com.example.tomtom.manager.MapManagerImpl
import com.example.tomtom.manager.NavigationManager
import com.example.tomtom.manager.NavigationManagerImpl
import com.example.tomtom.manager.RoutingManager
import com.example.tomtom.manager.RoutingManagerImpl
import com.example.tomtom.manager.SearchManager
import com.example.tomtom.manager.SearchManagerImpl
import com.example.tomtom.viewmodel.MainViewModel
import com.example.tomtom.viewmodel.ViewModelFactory
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.search.Search
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Provider
import javax.inject.Singleton

@Module
class MainModule {
    @Provides
    fun bindViewModelFactory(
        viewModelFactories: Map<Class<out ViewModel>,
                @JvmSuppressWildcards Provider<ViewModel>>
    ): ViewModelProvider.Factory {
        return ViewModelFactory(viewModelFactories)
    }

    @Provides
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel = mainViewModel

    @Singleton
    @Provides
    fun provideMapManager(locationProvider: LocationProvider): MapManager {
        return MapManagerImpl(locationProvider)
    }

    @Singleton
    @Provides
    fun provideSearchManager(search: Search): SearchManager {
        return SearchManagerImpl(search)
    }

    @Singleton
    @Provides
    fun provideRoutingManager(routePlanner: RoutePlanner): RoutingManager {
        return RoutingManagerImpl(routePlanner)
    }

    @Provides
    fun provideNavigationManager(
        tomTomNavigation: TomTomNavigation,
        mapManager: MapManager,
        routingManager: RoutingManager,
    ): NavigationManager {
        return NavigationManagerImpl(tomTomNavigation, routingManager)
    }
}
