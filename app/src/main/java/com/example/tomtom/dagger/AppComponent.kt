package com.example.tomtom.dagger

import com.example.tomtom.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [MainModule::class, TomTomModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)

    @Component.Factory
    interface Factory {
        fun create(mainModule: MainModule): AppComponent
    }
}