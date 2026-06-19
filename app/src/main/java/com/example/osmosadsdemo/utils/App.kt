package com.example.osmosadsdemo.utils

import android.app.Application
import android.util.Log
import com.ai.osmos.core.OsmosSDK

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            OsmosSDK
                .clientId("10088010")
                .buildGlobalInstance()

            Log.d("Osmos", "SDK initialized")
        } catch (e: Exception) {
            Log.e("Osmos", "SDK initialization failed", e)
        }
    }
}