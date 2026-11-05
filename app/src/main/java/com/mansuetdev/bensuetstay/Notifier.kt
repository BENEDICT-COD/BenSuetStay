package com.mansuetdev.bensuetstay

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** Shows system notifications (push messages, real-time alerts, sync results). */
object Notifier {

    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            context.getString(R.string.notification_channel_id),
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = context.getString(R.string.notification_channel_description)
        manager.createNotificationChannel(channel)
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Android 13+ asks the user once; older versions allow notifications by default. */
    fun requestPermission(activity: Activity) {
        if (!canNotify(activity)) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7001)
        }
    }

    fun show(context: Context, title: String, body: String) {
        if (!canNotify(context)) return
        // Tapping opens the app; WelcomeActivity sends the user to their own home screen
        val intent = Intent(context, WelcomeActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.brand_blue))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        try {
            NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
        } catch (e: SecurityException) {
            // Permission was revoked between the check and the call
        }
    }
}
