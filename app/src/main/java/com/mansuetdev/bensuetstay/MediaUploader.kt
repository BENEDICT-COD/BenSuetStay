package com.mansuetdev.bensuetstay

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata

/**
 * Uploads files picked on this device to Firebase Storage and returns their download URLs.
 * Anything that is already a remote URL (an existing photo on an edited listing) is
 * passed through untouched, so callers can hand over a mixed list.
 */
object MediaUploader {

    fun isLocal(uri: String): Boolean = uri.startsWith("content:") || uri.startsWith("file:")

    fun upload(context: Context, uriString: String, storagePath: String): Task<String> {
        if (!isLocal(uriString)) return Tasks.forResult(uriString)

        val uri = Uri.parse(uriString)
        val ref = FirebaseStorage.getInstance().reference.child(storagePath)
        val contentType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val metadata = StorageMetadata.Builder().setContentType(contentType).build()

        return ref.putFile(uri, metadata)
            .continueWithTask { upload ->
                if (!upload.isSuccessful) throw upload.exception ?: IllegalStateException("Upload failed")
                ref.downloadUrl
            }
            .continueWith { urlTask ->
                if (!urlTask.isSuccessful) throw urlTask.exception ?: IllegalStateException("Upload failed")
                urlTask.result.toString()
            }
    }

    /** Uploads (uri, storagePath) pairs; the result keeps the input order. */
    fun uploadAll(context: Context, items: List<Pair<String, String>>): Task<List<String>> {
        if (items.isEmpty()) return Tasks.forResult(emptyList())
        val tasks = items.map { (uri, path) -> upload(context, uri, path) }
        return Tasks.whenAllSuccess<String>(tasks)
    }
}
