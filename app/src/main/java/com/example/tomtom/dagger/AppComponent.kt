package com.example.tomtom.dagger

import com.example.tomtom.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(activity: MainActivity) // Specify which classes can request dependencies
}