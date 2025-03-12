package com.example.moveecho.mqtt

import android.util.Log
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONException
import org.json.JSONObject

/**
 * Listener for MQTT location messages that parses and forwards them to observers
 */
class MqttLocationMessageListener(private val myClientId: String) : MqttCallback {
    companion object {
        private const val TAG = "MqttLocationListener"
    }

    // List of observers to notify when a new location message arrives
    private val observers = mutableListOf<LocationMessageObserver>()

    /**
     * Add an observer to be notified of location messages
     *
     * @param observer The observer to add
     */
    fun addObserver(observer: LocationMessageObserver) {
        observers.add(observer)
    }

    /**
     * Remove an observer
     *
     * @param observer The observer to remove
     */
    fun removeObserver(observer: LocationMessageObserver) {
        observers.remove(observer)
    }

    override fun connectionLost(cause: Throwable?) {
        Log.e(TAG, "Connection to MQTT broker lost", cause)
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        if (topic == null || message == null) return

        try {
            // Parse the JSON message
            val payload = String(message.payload)
            val locationData = JSONObject(payload)

            // Get the client ID from the message
            val clientId = locationData.getString("clientId")

            // Ignore messages from self
            if (clientId == myClientId) {
                return
            }

            // Create a location data object
            val data = LocationData(
                clientId = clientId,
                lat = locationData.getDouble("lat"),
                lng = locationData.getDouble("lng"),
                type = locationData.getString("type"),
                timestamp = locationData.getLong("timestamp")
            )

            // Notify all observers
            observers.forEach { observer ->
                observer.onLocationMessageReceived(data)
            }

            Log.d(TAG, "Received location from $clientId: ${data.lat}, ${data.lng}")

        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing location message", e)
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        // Not used for this implementation
    }

    /**
     * Interface for observers of location messages
     */
    interface LocationMessageObserver {
        fun onLocationMessageReceived(locationData: LocationData)
    }

    /**
     * Data class for location information
     */
    data class LocationData(
        val clientId: String,
        val lat: Double,
        val lng: Double,
        val type: String,
        val timestamp: Long
    )
}