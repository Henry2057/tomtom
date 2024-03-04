package com.example.tomtom.dagger

import com.example.tomtom.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, MainModule::class, TomTomModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)

    @Component.Factory
    interface Factory {
        fun create(appModule: AppModule): AppComponent
    }
}