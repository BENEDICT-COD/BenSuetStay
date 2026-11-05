package com.mansuetdev.bensuetstay

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage

/** Profile pictures for every role live at profile_pictures/{uid}/avatar.jpg (see storage.rules). */
object ProfilePhoto {
    private fun path(uid: String) = "profile_pictures/$uid/avatar.jpg"

    fun upload(context: Context, uid: String, uri: Uri): Task<String> =
        MediaUploader.upload(context, uri.toString(), path(uid))

    /** Deletes the stored file; succeeds even if there was no file. */
    fun delete(uid: String): Task<Void> =
        FirebaseStorage.getInstance().reference.child(path(uid)).delete()
            .continueWith { null }
}
