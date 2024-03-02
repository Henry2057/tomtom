package com.example.tomtom.dagger

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideExampleString(): String = "This is an example string"
}
