package com.mansuetdev.bensuetstay

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Shared behaviour for every screen:
 *  - draws edge-to-edge (Android 15+ forces this when targetSdk >= 35) and pads the
 *    root view so content never sits under the status bar, gesture bar or keyboard;
 *  - [requireRole] sends the user back through WelcomeActivity when the screen is opened
 *    without the right sign-in, e.g. after Android killed the app in the background.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        if (view != null) applySystemBarInsets(view)
    }

    private fun applySystemBarInsets(root: View) {
        val left = root.paddingLeft
        val top = root.paddingTop
        val right = root.paddingRight
        val bottom = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                left + bars.left,
                top + bars.top,
                right + bars.right,
                bottom + maxOf(bars.bottom, ime.bottom)
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * For public screens: if Firebase still has a signed-in user but [Session] was lost (Android
     * killed the process), go through WelcomeActivity to restore it. Returns true when restarting.
     */
    protected fun restoreSessionIfNeeded(): Boolean {
        if (Session.role != null || com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) return false
        Navigator.restart(this)
        return true
    }

    /** Returns true when the signed-in user has one of [allowed]; otherwise restarts the app flow. */
    protected fun requireRole(vararg allowed: Role): Boolean {
        val role = Session.role
        if (role != null && role in allowed) return true
        Navigator.restart(this)
        return false
    }
}
