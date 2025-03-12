package com.CharlieMomo.moveecho

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"

        // Permission request code
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

        // Keys for SharedPreferences
        private const val PREF_TRANSPORT_TYPE = "transport_type"

        // Transport type options
        private val TRANSPORT_TYPES = arrayOf("car", "bike", "truck", "motorcycle", "walking")
    }

    private lateinit var messageLogTextView: TextView
    private lateinit var startTrackingButton: Button
    private lateinit var stopTrackingButton: Button
    private lateinit var testMqttButton: Button
    private lateinit var transportTypeSpinner: Spinner
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var mqttTest: MqttTest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Initialize UI elements
        messageLogTextView = findViewById(R.id.message_log)
        startTrackingButton = findViewById(R.id.start_tracking_button)
        stopTrackingButton = findViewById(R.id.stop_tracking_button)
        testMqttButton = findViewById(R.id.test_mqtt_button)
        transportTypeSpinner = findViewById(R.id.transport_type_spinner)

        // Set up the transport type spinner
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, TRANSPORT_TYPES
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        transportTypeSpinner.adapter = adapter

        // Set the default selection based on saved preference
        val savedTransportType = sharedPreferences.getString(PREF_TRANSPORT_TYPE, TRANSPORT_TYPES[0])
        TRANSPORT_TYPES.forEachIndexed { index, type ->
            if (type == savedTransportType) {
                transportTypeSpinner.setSelection(index)
                return@forEachIndexed
            }
        }

        // Set up spinner selection listener
        transportTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = TRANSPORT_TYPES[position]
                // Save the selection to SharedPreferences
                sharedPreferences.edit().putString(PREF_TRANSPORT_TYPE, selectedType).apply()
                Log.d(TAG, "Transport type set to: $selectedType")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Set up button click listeners
        startTrackingButton.setOnClickListener { startLocationTracking() }
        stopTrackingButton.setOnClickListener { stopLocationTracking() }
        testMqttButton.setOnClickListener { testMqttConnection() }

        // Request location permissions if needed
        requestLocationPermissions()
    }

    /**
     * Request location permissions
     */
    private fun requestLocationPermissions() {
        // Check if we already have permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )

            // For Android 10 (API 29) and above, also request background location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    /**
     * Handle permission request results
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d(TAG, "Location permission granted")
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required for this app", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Start location tracking service
     */
    private fun startLocationTracking() {
        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
            requestLocationPermissions()
            return
        }

        // Start the location service
        val serviceIntent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        // Update UI
        startTrackingButton.isEnabled = false
        stopTrackingButton.isEnabled = true
        Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show()

        // Log message
        appendToLog("Started location tracking as ${
            sharedPreferences.getString(
                PREF_TRANSPORT_TYPE,
                TRANSPORT_TYPES[0]
            )
        }")
    }

    /**
     * Stop location tracking service
     */
    private fun stopLocationTracking() {
        // Stop the location service
        val serviceIntent = Intent(this, LocationService::class.java)
        stopService(serviceIntent)

        // Update UI
        startTrackingButton.isEnabled = true
        stopTrackingButton.isEnabled = false
        Toast.makeText(this, "Location tracking stopped", Toast.LENGTH_SHORT).show()

        // Log message
        appendToLog("Stopped location tracking")
    }

    /**
     * Test the MQTT connection
     */
    private fun testMqttConnection() {
        appendToLog("Starting MQTT connection test...")

        // Create a new MQTT test with a callback
        mqttTest = MqttTest(this) { message ->
            // Run on UI thread to update UI safely
            runOnUiThread { appendToLog(message) }
        }

        // Start the test
        mqttTest.startTest()
    }

    /**
     * Append message to the log TextView
     */
    private fun appendToLog(message: String) {
        val currentLog = messageLogTextView.text.toString()
        val newLog = if (currentLog.isEmpty()) message else "$currentLog\n$message"
        messageLogTextView.text = newLog

        // Auto-scroll to the bottom
        val layout = messageLogTextView.layout ?: return
        val scrollAmount = layout.getLineTop(messageLogTextView.lineCount) - messageLogTextView.height
        if (scrollAmount > 0) {
            messageLogTextView.scrollTo(0, scrollAmount)
        } else {
            messageLogTextView.scrollTo(0, 0)
        }
    }
}