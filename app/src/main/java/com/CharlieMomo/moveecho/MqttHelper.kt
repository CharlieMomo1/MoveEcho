package com.CharlieMomo.moveecho

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Helper class to handle MQTT connection and messaging for MoveEcho
 */
class MqttHelper(private val context: Context) {
    companion object {
        private const val TAG = "MqttHelper"

        // MQTT connection details
        private const val BROKER_URL = "tcp://10.33.164.188:1883"
        private const val LOCATION_TOPIC = "moveecho/locations"

        // Connection retry settings
        private const val MAX_RETRY_COUNT = 5
        private const val RETRY_DELAY_MS = 5000L // 5 seconds
    }

    private var mqttClient: MqttClient? = null
    private var mqttCallback: MqttCallback? = null
    private val clientId: String = "MoveEcho-${UUID.randomUUID()}"
    private var retryCount = 0

    /**
     * Connect to the MQTT broker
     */
    fun connect() {
        try {
            // Set up the persistence layer
            val persistence = MemoryPersistence()

            // Initialize the MQTT client
            mqttClient = MqttClient(BROKER_URL, clientId, persistence)

            // Set up the connection options
            val connectOptions = MqttConnectOptions()
            connectOptions.isCleanSession = true
            connectOptions.isAutomaticReconnect = true
            connectOptions.connectionTimeout = 10

            // Set up callback for MQTT events
            mqttClient?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e(TAG, "Connection to MQTT broker lost", cause)
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    // Forward the message to the custom callback if set
                    if (topic != null && message != null && mqttCallback != null) {
                        mqttCallback?.messageArrived(topic, message)
                    }

                    Log.d(TAG, "Received message: ${message?.payload?.let { String(it) }}")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(TAG, "Message delivery complete")
                }
            })

            // Connect to the broker
            mqttClient?.connect(connectOptions)
            Log.i(TAG, "Connected to MQTT broker successfully")

            // Subscribe to the locations topic
            subscribeToLocationTopic()

        } catch (e: MqttException) {
            Log.e(TAG, "Error initializing MQTT client", e)

            // Retry connection if not exceeding max retry count
            if (retryCount < MAX_RETRY_COUNT) {
                retryCount++
                Log.i(TAG, "Retrying connection attempt $retryCount in ${RETRY_DELAY_MS / 1000} seconds")

                // Handler to retry after a delay
                Handler(Looper.getMainLooper()).postDelayed(
                    { connect() },
                    RETRY_DELAY_MS
                )
            } else {
                Log.e(TAG, "Max retry attempts reached. Please check your connection settings.")
            }
        }
    }

    /**
     * Subscribe to the location topic to receive updates from other users
     */
    private fun subscribeToLocationTopic() {
        try {
            mqttClient?.subscribe(LOCATION_TOPIC, 0)
            Log.i(TAG, "Subscribed to location topic successfully")
        } catch (e: MqttException) {
            Log.e(TAG, "Error subscribing to location topic", e)
        }
    }

    /**
     * Publish location data to the MQTT broker
     *
     * @param latitude The user's latitude
     * @param longitude The user's longitude
     * @param transportType The user's transport type (car, bike, etc.)
     */
    fun publishLocation(latitude: Double, longitude: Double, transportType: String) {
        try {
            // Create a JSON object with location data
            val locationData = JSONObject()
            locationData.put("clientId", clientId)
            locationData.put("lat", latitude)
            locationData.put("lng", longitude)
            locationData.put("type", transportType)
            locationData.put("timestamp", System.currentTimeMillis())

            // Convert to string
            val payload = locationData.toString()

            // Create an MQTT message
            val message = MqttMessage(payload.toByteArray())
            message.qos = 0 // Use QoS 0 for location updates (fire and forget)
            message.isRetained = false

            // Publish the message
            mqttClient?.publish(LOCATION_TOPIC, message)
            Log.d(TAG, "Location published successfully")

        } catch (e: MqttException) {
            Log.e(TAG, "Error publishing location", e)
        } catch (e: JSONException) {
            Log.e(TAG, "Error creating JSON payload", e)
        }
    }

    /**
     * Set a custom callback to handle incoming MQTT messages
     *
     * @param callback The callback to set
     */
    fun setCallback(callback: MqttCallback) {
        this.mqttCallback = callback
    }

    /**
     * Disconnect from the MQTT broker
     */
    fun disconnect() {
        try {
            if (mqttClient?.isConnected == true) {
                mqttClient?.disconnect()
                Log.i(TAG, "Disconnected from MQTT broker")
            }
        } catch (e: MqttException) {
            Log.e(TAG, "Error disconnecting from MQTT broker", e)
        }
    }
}