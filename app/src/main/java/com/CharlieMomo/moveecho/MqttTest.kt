package com.CharlieMomo.moveecho

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

/**
 * Simple utility class to test MQTT connection
 */
class MqttTest(
    private val context: Context,
    private val resultCallback: (String) -> Unit
) {
    companion object {
        private const val TAG = "MqttTest"
    }

    private val mqttHelper = MqttHelper(context)
    private val callback = TestCallback(resultCallback)

    init {
        mqttHelper.setCallback(callback)
    }

    /**
     * Start the MQTT connection test
     */
    fun startTest() {
        // Connect to the broker
        mqttHelper.connect()

        // Show toast message
        Toast.makeText(context, "MQTT test started - connecting to broker", Toast.LENGTH_SHORT).show()

        // Create a message with test coordinates
        val testLat = 37.7749
        val testLng = -122.4194
        val testType = "car"

        // Send test message after a short delay to ensure connection is established
        Handler(Looper.getMainLooper()).postDelayed(
            {
                mqttHelper.publishLocation(testLat, testLng, testType)
                Toast.makeText(context, "Test message sent", Toast.LENGTH_SHORT).show()
            },
            3000
        )
    }

    /**
     * Stop the MQTT test
     */
    fun stopTest() {
        mqttHelper.disconnect()
        Toast.makeText(context, "MQTT test stopped", Toast.LENGTH_SHORT).show()
    }

    /**
     * Callback implementation for MQTT messages
     */
    private class TestCallback(private val resultCallback: (String) -> Unit) : MqttCallback {
        override fun connectionLost(cause: Throwable?) {
            Log.e(TAG, "MQTT test connection lost", cause)
            resultCallback("Connection lost: ${cause?.message}")
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
            val payload = message?.payload?.let { String(it) } ?: "Empty message"
            Log.d(TAG, "MQTT test message received: $payload")
            resultCallback("Message received: $payload")
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            Log.d(TAG, "MQTT test message delivered")
            resultCallback("Message delivered successfully")
        }
    }
}