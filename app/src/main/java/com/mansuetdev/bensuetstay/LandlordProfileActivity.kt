package com.mansuetdev.bensuetstay

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.mansuetdev.bensuetstay.databinding.ActivityLandlordProfileBinding

/** Settings screen shared by landlords and admins. */
class LandlordProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityLandlordProfileBinding
    private val isAdmin get() = Session.role == Role.ADMIN

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) uploadProfilePicture(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.LANDLORD, Role.ADMIN)) return
        binding = ActivityLandlordProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnLogout.setOnClickListener { Navigator.signOut(this) }
        binding.fabEditPhoto.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.profile_picture))
                .setItems(arrayOf(getString(R.string.choose_from_gallery), getString(R.string.remove_profile_picture))) { _, which ->
                    if (which == 0) pickImage.launch("image/*") else removeProfilePicture()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
        binding.btnPersonalInfo.setOnClickListener { showPersonalInfo() }
        binding.btnLanguage.setOnClickListener { LanguagePicker.show(this) }
        binding.btnChangePassword.setOnClickListener {
            PasswordDialog.show(this, forced = false) {
                Session.landlord?.let { landlord ->
                    if (landlord.mustChangePassword) {
                        LandlordRepository.markPasswordChanged(landlord.id)
                        Session.setLandlord(landlord.copy(mustChangePassword = false))
                    }
                }
            }
        }
        // Help Center opens the landlord FAQ (admins see the same page; admin setup is in the README)
        binding.btnHelp.setOnClickListener { openInfo(InfoActivity.PAGE_FAQ_LANDLORD) }
    }

    override fun onResume() {
        super.onResume()
        if (Session.role != null) setupProfileInfo()
    }

    private fun setupProfileInfo() {
        SettingsSections.buildForStaff(this, binding.settingsSections)
        binding.tvCurrentLanguage.text = LanguagePicker.currentName()
        val name: String
        val email: String
        val imageUrl: String?
        val fallback: Int
        if (isAdmin) {
            val admin = Session.admin ?: return
            name = admin.name; email = admin.email; imageUrl = admin.profileImageUri; fallback = R.drawable.ic_security
        } else {
            val landlord = Session.landlord ?: return
            name = landlord.name; email = landlord.email; imageUrl = landlord.profileImageUri; fallback = R.drawable.ic_person
        }
        binding.tvProfileName.text = name
        binding.tvProfileEmail.text = email
        Images.load(binding.ivProfilePicture, imageUrl, fallback, circle = true)
    }

    private fun showPersonalInfo() {
        val message = if (isAdmin) {
            val a = Session.admin ?: return
            getString(R.string.name_email_role_administrator, a.name, a.email)
        } else {
            val l = Session.landlord ?: return
            getString(R.string.name_email_phone_to_change_these, l.name, l.email, l.phone.ifBlank { "\u2014" })
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.personal_information))
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
        if (!isAdmin) {
            dialog.setNeutralButton(R.string.request_change) { _, _ ->
                if (!emailSupport(getString(R.string.change_request_subject))) toast(getString(R.string.no_email_app))
            }
        }
        dialog.show()
    }

    private fun saveImageUrl(uid: String, url: String?): Task<Void> =
        if (isAdmin) AdminRepository.updateProfileImage(uid, url) else LandlordRepository.updateProfileImage(uid, url)

    private fun currentUid(): String? = if (isAdmin) Session.admin?.id else Session.landlord?.id

    private fun uploadProfilePicture(uri: Uri) {
        val uid = currentUid() ?: return
        toast(getString(R.string.uploading))
        ProfilePhoto.upload(this, uid, uri)
            .continueWithTask { t ->
                if (!t.isSuccessful) throw t.exception ?: IllegalStateException("Upload failed")
                val url = t.result
                saveImageUrl(uid, url).continueWith { url }
            }
            .addOnSuccessListener { url -> updateLocal(url); toast(getString(R.string.profile_picture_updated)) }
            .addOnFailureListener { e -> toast(AuthErrors.message(this, e), long = true) }
    }

    private fun removeProfilePicture() {
        val uid = currentUid() ?: return
        ProfilePhoto.delete(uid)
            .continueWithTask { saveImageUrl(uid, null) }
            .addOnSuccessListener { updateLocal(null); toast(getString(R.string.profile_picture_removed)) }
            .addOnFailureListener { e -> toast(AuthErrors.message(this, e), long = true) }
    }

    private fun updateLocal(url: String?) {
        if (isAdmin) Session.admin?.let { Session.setAdmin(it.copy(profileImageUri = url)) }
        else Session.landlord?.let { Session.setLandlord(it.copy(profileImageUri = url)) }
        setupProfileInfo()
    }
}
