package com.example.tomtom.dagger

import android.app.Application
import android.content.Context
import com.example.tomtom.BuildConfig.TOMTOM_API_KEY
import com.example.tomtom.MainApplication
import com.example.tomtom.repo.MapRepository
import com.example.tomtom.repo.NavigationRepository
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule(private val application: Application) {

    @Singleton
    @Provides
    fun provideContext(): Context = application

    @Singleton
    @Provides
    fun provideApiKey(): String = TOMTOM_API_KEY

    @Singleton
    @Provides
    fun provideRoutePlanner(context: Context, apiKey: String): RoutePlanner {
        return OnlineRoutePlanner.create(context, apiKey)
    }

    @Singleton
    @Provides
    fun provideNavigationRepository(routePlanner: RoutePlanner): NavigationRepository {
        return NavigationRepository(routePlanner)
    }

    @Singleton
    @Provides
    fun provideMapRepository(context: Context): MapRepository = MapRepository(context)
}
