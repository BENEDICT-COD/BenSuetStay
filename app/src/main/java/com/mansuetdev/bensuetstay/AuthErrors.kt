package com.mansuetdev.bensuetstay

import android.content.Context
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException

/** Turns Firebase exceptions into messages a student or landlord can act on. */
object AuthErrors {
    fun message(context: Context, e: Exception?): String = when (e) {
        is FirebaseAuthWeakPasswordException ->
            context.getString(R.string.that_password_is_too_weak_use, Validators.MIN_PASSWORD_LENGTH)
        is FirebaseAuthInvalidCredentialsException -> context.getString(R.string.incorrect_email_or_password)
        is FirebaseAuthInvalidUserException -> context.getString(R.string.no_active_account_was_found_for)
        is FirebaseAuthUserCollisionException -> context.getString(R.string.an_account_with_this_email_already)
        is FirebaseAuthRecentLoginRequiredException -> context.getString(R.string.please_log_out_and_log_in)
        is FirebaseNetworkException -> context.getString(R.string.no_internet_connection_please_try_again)
        is FirebaseTooManyRequestsException -> context.getString(R.string.too_many_attempts_please_wait_a)
        is FirebaseFirestoreException -> when {
            e.message == ApplicationRepository.ERR_ROOM_FULL -> context.getString(R.string.err_room_full)
            e.message == ApplicationRepository.ERR_ROOM_GONE -> context.getString(R.string.err_room_gone)
            e.message == ApplicationRepository.ERR_ALREADY_APPROVED -> context.getString(R.string.err_already_approved)
            else -> when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> context.getString(R.string.you_don_t_have_permission_to)
            FirebaseFirestoreException.Code.UNAVAILABLE -> context.getString(R.string.can_t_reach_the_server_check)
            else -> e.message ?: context.getString(R.string.something_went_wrong_please_try_again)
            }
        }
        is StorageException -> when (e.errorCode) {
            StorageException.ERROR_NOT_AUTHORIZED -> context.getString(R.string.you_don_t_have_permission_to_2)
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> context.getString(R.string.upload_timed_out_check_your_connection)
            else -> context.getString(R.string.upload_failed_please_try_again)
        }
        else -> e?.message ?: context.getString(R.string.something_went_wrong_please_try_again)
    }
}
