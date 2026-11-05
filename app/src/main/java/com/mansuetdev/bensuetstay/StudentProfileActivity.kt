package com.mansuetdev.bensuetstay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.mansuetdev.bensuetstay.databinding.ActivityStudentProfileBinding

class StudentProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityStudentProfileBinding

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) uploadProfilePicture(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.STUDENT)) return
        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditStudentProfileActivity::class.java))
        }
        binding.btnBookings.setOnClickListener {
            startActivity(Intent(this, StudentBookingsActivity::class.java))
        }
        binding.btnApplications.setOnClickListener {
            startActivity(Intent(this, StudentApplicationsActivity::class.java))
        }
        binding.btnLanguage.setOnClickListener { LanguagePicker.show(this) }
        binding.fabEditPhoto.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.profile_picture))
                .setItems(arrayOf(getString(R.string.choose_from_gallery), getString(R.string.remove_profile_picture))) { _, which ->
                    if (which == 0) pickImage.launch("image/*") else removeProfilePicture()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
        binding.btnLogout.setOnClickListener { Navigator.signOut(this) }
    }

    override fun onResume() {
        super.onResume()
        if (Session.role == Role.STUDENT) setupProfileInfo()
    }

    private fun setupProfileInfo() {
        SettingsSections.buildForStudent(this, binding.settingsSections)
        binding.tvCurrentLanguage.text = LanguagePicker.currentName()
        val student = Session.student ?: return
        binding.tvProfileName.text = student.fullName
        binding.tvProfileEmail.text = student.email
        Images.load(binding.ivProfilePicture, student.profileImageUri, R.drawable.ic_person, circle = true)
    }

    private fun uploadProfilePicture(uri: Uri) {
        val uid = Session.student?.id ?: return
        toast(getString(R.string.uploading))
        ProfilePhoto.upload(this, uid, uri)
            .continueWithTask { t ->
                if (!t.isSuccessful) throw t.exception ?: IllegalStateException("Upload failed")
                val url = t.result
                StudentRepository.updateProfileImage(uid, url).continueWith { url }
            }
            .addOnSuccessListener { url -> saveLocally(url); toast(getString(R.string.profile_picture_updated)) }
            .addOnFailureListener { e -> toast(AuthErrors.message(this, e), long = true) }
    }

    private fun removeProfilePicture() {
        val uid = Session.student?.id ?: return
        ProfilePhoto.delete(uid)
            .continueWithTask { StudentRepository.updateProfileImage(uid, null) }
            .addOnSuccessListener { saveLocally(null); toast(getString(R.string.profile_picture_removed)) }
            .addOnFailureListener { e -> toast(AuthErrors.message(this, e), long = true) }
    }

    private fun saveLocally(url: String?) {
        Session.student?.let { Session.setStudent(it.copy(profileImageUri = url)) }
        setupProfileInfo()
    }
}
