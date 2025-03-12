package com.CharlieMomo.moveecho

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * Helper class to handle notification creation for the foreground service
 */
object NotificationHelper {
    private const val CHANNEL_ID = "MoveEchoLocationChannel"
    private const val CHANNEL_NAME = "Location Tracking"
    private const val CHANNEL_DESCRIPTION = "Shows when MoveEcho is tracking your location"
    private const val NOTIFICATION_ID = 1001

    /**
     * Create and return a notification for the location service
     *
     * @param context The context to use
     * @return A notification
     */
    fun createLocationNotification(context: Context): Notification {
        createNotificationChannel(context)

        // Create an intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        // Note: For testing, we'll use a generic Android icon instead of a custom one
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("MoveEcho Active")
            .setContentText("Sharing your location in real-time")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Using a system icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * Create the notification channel for Android O and above
     *
     * @param context The context to use
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Get the notification ID
     *
     * @return The notification ID
     */
    fun getNotificationId(): Int {
        return NOTIFICATION_ID
    }
}