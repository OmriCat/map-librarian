package com.omricat.maplibrarian

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp

class MapLibraryApp : Application(), MapLibrarian {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}

interface MapLibrarian {

}

val Context.mapLibrarian: MapLibrarian get() = (this.applicationContext as MapLibrarian)
