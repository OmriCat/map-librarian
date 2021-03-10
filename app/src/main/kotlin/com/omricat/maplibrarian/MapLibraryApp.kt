package com.omricat.maplibrarian

import android.app.Application
import android.content.Context

class MapLibraryApp : Application(), MapLibrarian {
}

interface MapLibrarian {

}

val Context.mapLibrarian: MapLibrarian get() = (this.applicationContext as MapLibrarian)
