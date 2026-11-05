package com.mansuetdev.bensuetstay

import android.app.Activity
import android.content.Intent
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

enum class Role { STUDENT, LANDLORD, ADMIN }

/**
 * Who is signed in. Every role signs in with Firebase Authentication; the role is
 * decided by which collection (admins / landlords / students) holds a document whose
 * id is the user's uid. Firebase keeps the sign-in across app restarts, and
 * WelcomeActivity calls [load] to rebuild this object when the process is recreated.
 */
object Session {
    var role: Role? = null
        private set
    var student: Student? = null
        private set
    var landlord: LandlordAccount? = null
        private set
    var admin: AdminAccount? = null
        private set

    val uid: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    fun setStudent(value: Student) {
        clear(); student = value; role = Role.STUDENT
    }

    fun setLandlord(value: LandlordAccount) {
        clear(); landlord = value; role = Role.LANDLORD
    }

    fun setAdmin(value: AdminAccount) {
        clear(); admin = value; role = Role.ADMIN
    }

    /** Looks up the signed-in user's role and profile. onLoaded(null) means no profile exists. */
    fun load(uid: String, onLoaded: (Role?) -> Unit, onFailed: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val adminTask = db.collection(AdminRepository.COLLECTION).document(uid).get()
        val landlordTask = db.collection(LandlordRepository.COLLECTION).document(uid).get()
        val studentTask = db.collection(StudentRepository.COLLECTION).document(uid).get()

        Tasks.whenAllComplete(adminTask, landlordTask, studentTask).addOnCompleteListener {
            val adminDoc = adminTask.existingDoc()
            val landlordDoc = landlordTask.existingDoc()
            val studentDoc = studentTask.existingDoc()
            when {
                adminDoc != null -> {
                    setAdmin(AdminRepository.fromDoc(adminDoc)); onLoaded(Role.ADMIN)
                }
                landlordDoc != null -> {
                    setLandlord(LandlordRepository.fromDoc(landlordDoc)); onLoaded(Role.LANDLORD)
                }
                studentDoc != null -> {
                    setStudent(StudentRepository.fromDoc(studentDoc)); onLoaded(Role.STUDENT)
                }
                else -> {
                    val failure = listOf(adminTask, landlordTask, studentTask)
                        .firstOrNull { !it.isSuccessful }?.exception
                    clear()
                    if (failure != null) onFailed(failure) else onLoaded(null)
                }
            }
        }
    }

    private fun Task<DocumentSnapshot>.existingDoc(): DocumentSnapshot? {
        if (!isSuccessful) return null
        val doc = result
        return if (doc != null && doc.exists()) doc else null
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        clear()
    }

    private fun clear() {
        role = null; student = null; landlord = null; admin = null
    }
}

object Navigator {
    /** Opens the signed-in user's home screen and clears everything behind it. */
    fun openHome(activity: Activity, role: Role) {
        NotificationCenter.start(activity)      // real-time alerts for this user
        NotificationCenter.subscribeTopics(activity, role) // push topics
        SyncManager.syncNow(activity)           // send anything saved offline
        val target = when (role) {
            Role.STUDENT -> HomeActivity::class.java
            Role.LANDLORD -> LandlordDashboardActivity::class.java
            Role.ADMIN -> AdminDashboardActivity::class.java
        }
        activity.startActivity(
            Intent(activity, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        activity.finish()
    }

    /** Back to the welcome screen, which restores a saved sign-in or shows the intro. */
    fun restart(activity: Activity) {
        activity.startActivity(
            Intent(activity, WelcomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        activity.finish()
    }

    fun signOut(activity: Activity) {
        NotificationCenter.stop()
        NotificationCenter.unsubscribeRoleTopics()
        Session.signOut()
        restart(activity)
    }
}
