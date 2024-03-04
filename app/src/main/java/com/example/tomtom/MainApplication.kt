package com.example.tomtom

import android.app.Application
import com.example.tomtom.dagger.AppComponent
import com.example.tomtom.dagger.DaggerAppComponent
import com.example.tomtom.dagger.MainModule

class MainApplication : Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(MainModule(this))
    }
}
