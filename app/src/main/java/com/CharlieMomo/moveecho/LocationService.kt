package com.CharlieMomo.moveecho

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

/**
 * Service to handle location updates and send them via MQTT
 */
class LocationService : Service() {
    companion object {
        private const val TAG = "LocationService"

        // Location request settings
        private const val UPDATE_INTERVAL = 10000L // 10 seconds
        private const val FASTEST_INTERVAL = 5000L // 5 seconds

        // Keys for SharedPreferences
        private const val PREF_TRANSPORT_TYPE = "transport_type"
        private const val DEFAULT_TRANSPORT_TYPE = "car"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mqttHelper: MqttHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Location service created")

        // Initialize shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Initialize MQTT helper
        mqttHelper = MqttHelper(applicationContext)
        mqttHelper.connect()

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    // Process each location update
                    onNewLocation(location)
                }
            }
        }
    }

    /**
     * Handle a new location update
     *
     * @param location The new location
     */
    private fun onNewLocation(location: Location) {
        Log.d(TAG, "New location: ${location.latitude}, ${location.longitude}")

        // Get the user's transport type from SharedPreferences
        val transportType = sharedPreferences.getString(PREF_TRANSPORT_TYPE, DEFAULT_TRANSPORT_TYPE) ?: DEFAULT_TRANSPORT_TYPE

        // Publish the location via MQTT
        mqttHelper.publishLocation(
            location.latitude,
            location.longitude,
            transportType
        )
    }

    /**
     * Request location updates from the FusedLocationProvider
     */
    private fun requestLocationUpdates() {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permission not granted")
            return
        }

        // Create location request
        val locationRequest = LocationRequest.Builder(UPDATE_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
            .build()

        // Request location updates
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Lost location permission", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Location service started")

        // Start as a foreground service with notification
        startForeground(
            NotificationHelper.getNotificationId(),
            NotificationHelper.createLocationNotification(this)
        )

        // Start requesting location updates
        requestLocationUpdates()

        return START_STICKY
    }

    override fun onDestroy() {
        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // Disconnect MQTT
        mqttHelper.disconnect()

        Log.d(TAG, "Location service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}