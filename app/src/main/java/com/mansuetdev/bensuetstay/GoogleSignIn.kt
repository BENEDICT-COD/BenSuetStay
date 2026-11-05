package com.mansuetdev.bensuetstay

import android.app.Activity
import android.content.Context
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Single sign-on with Google: Android Credential Manager shows the Google account sheet,
 * the returned ID token signs the user into Firebase Authentication.
 */
object GoogleSignIn {

    /**
     * The OAuth "Web client ID". Once Google sign-in is enabled in Firebase and the new
     * google-services.json is added, the Google Services plugin generates default_web_client_id
     * and it's used automatically; otherwise res/values/sso_config.xml is used.
     */
    fun webClientId(context: Context): String? {
        val generated = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        val id = if (generated != 0) context.getString(generated) else context.getString(R.string.google_web_client_id)
        return id.takeIf { it.endsWith(".apps.googleusercontent.com") }
    }

    fun signIn(activity: Activity, onSignedIn: (FirebaseUser) -> Unit, onError: (String?) -> Unit) {
        requestIdToken(activity, onError) { idToken ->
            FirebaseAuth.getInstance()
                .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnSuccessListener { auth ->
                    val user = auth.user
                    if (user != null) onSignedIn(user) else onError(activity.getString(R.string.google_sign_in_failed))
                }
                .addOnFailureListener { e -> onError(AuthErrors.message(activity, e)) }
        }
    }

    /** Shows Google's account sheet and returns a Google ID token (also used to re-confirm identity). */
    fun requestIdToken(activity: Activity, onError: (String?) -> Unit, onToken: (String) -> Unit) {
        val clientId = webClientId(activity)
        if (clientId == null) {
            onError(activity.getString(R.string.google_sign_in_not_configured))
            return
        }
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
            .build()

        CredentialManager.create(activity).getCredentialAsync(
            activity,
            request,
            CancellationSignal(),
            ContextCompat.getMainExecutor(activity),
            object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                override fun onResult(result: GetCredentialResponse) {
                    val credential = result.credential
                    if (credential !is CustomCredential ||
                        credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        onError(activity.getString(R.string.google_sign_in_failed))
                        return
                    }
                    onToken(GoogleIdTokenCredential.createFrom(credential.data).idToken)
                }

                override fun onError(e: GetCredentialException) {
                    when (e) {
                        is GetCredentialCancellationException -> onError(null) // user closed the sheet
                        is NoCredentialException -> onError(activity.getString(R.string.google_no_accounts))
                        else -> onError(activity.getString(R.string.google_sign_in_failed))
                    }
                }
            }
        )
    }

    /**
     * After a Google sign-in: existing users (any role) go to their home screen; a brand-new
     * Google user finishes a short student sign-up (campus, address, funding).
     */
    fun route(activity: Activity, user: FirebaseUser, onNewUser: () -> Unit, onError: (String) -> Unit) {
        Session.load(
            user.uid,
            onLoaded = { role -> if (role != null) Navigator.openHome(activity, role) else onNewUser() },
            onFailed = { e ->
                Session.signOut()
                onError(activity.getString(R.string.signed_in_but_your_profile_couldn, AuthErrors.message(activity, e)))
            }
        )
    }
}
