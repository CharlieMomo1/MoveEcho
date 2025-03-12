package com.CharlieMomo.moveecho

import android.app.Application
import android.util.Log

/**
 * Application class for MoveEcho
 */
class MoveEchoApplication : Application() {
    companion object {
        private const val TAG = "MoveEchoApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MoveEcho application created")

        // Initialize MQTT service
        // This helps with the Paho Android Service initialization
        try {
            Class.forName("org.eclipse.paho.android.service.MqttService")
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "MQTT Service class not found", e)
        }
    }
}