package com.example.tomtom

import android.app.Application
import com.example.tomtom.dagger.AppComponent
import com.example.tomtom.dagger.DaggerAppComponent

class MainApplication : Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.create()
    }
}
