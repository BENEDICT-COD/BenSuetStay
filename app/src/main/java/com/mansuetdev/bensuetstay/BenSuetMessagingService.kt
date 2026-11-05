package com.mansuetdev.bensuetstay

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Push notifications from Firebase Cloud Messaging (e.g. a campaign sent from the Firebase
 * console to the "students" topic). In the background Android shows them automatically;
 * this shows them while the app is open.
 */
class BenSuetMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["body"] ?: return
        Notifier.show(this, title, body)
    }

    override fun onNewToken(token: String) {
        // Topic subscriptions follow the device automatically; nothing to store.
    }
}
