package com.mansuetdev.bensuetstay

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.mansuetdev.bensuetstay.databinding.ActivityLandlordProfileBinding

class LandlordProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandlordProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadProfilePicture(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandlordProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if Admin or Landlord is logged in
        val landlord = LandlordRepository.loggedInLandlord
        val admin = AdminRepository.loggedInAdmin
        val isAdmin = intent.getBooleanExtra("isAdmin", false) || (admin != null) 
        
        if (landlord == null && admin == null && !isAdmin) {
            finish()
            return
        }

        setupProfileInfo(isAdmin)
        setupActions(isAdmin)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnLogout.setOnClickListener {
            LandlordRepository.loggedInLandlord = null
            AdminRepository.loggedInAdmin = null
            auth.signOut()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val isAdmin = intent.getBooleanExtra("isAdmin", false) || (AdminRepository.loggedInAdmin != null)
        setupProfileInfo(isAdmin)
    }
    private fun setupProfileInfo(isAdmin: Boolean) {
        val imageUrl: String?
        val name: String
        val email: String

        if (isAdmin) {
            val admin = AdminRepository.loggedInAdmin ?: return
            name = admin.name
            email = admin.email
            imageUrl = admin.profileImageUri
            binding.ivProfilePicture.setImageResource(R.drawable.ic_security) // Default icon
        } else {
            val landlord = LandlordRepository.loggedInLandlord ?: return
            name = landlord.name
            email = landlord.email
            imageUrl = landlord.profileImageUri
            binding.ivProfilePicture.setImageResource(R.drawable.ic_person) // Default icon
        }

        binding.tvProfileName.text = name
        binding.tvProfileEmail.text = email
        binding.fabEditPhoto.visibility = android.view.View.VISIBLE

        if (!imageUrl.isNullOrEmpty()) {
            binding.ivProfilePicture.load(imageUrl) {
                crossfade(true)
                placeholder(if (isAdmin) R.drawable.ic_security else R.drawable.ic_person)
                error(if (isAdmin) R.drawable.ic_security else R.drawable.ic_person)
                transformations(CircleCropTransformation())
            }
        }

    }

    private fun setupActions(isAdmin: Boolean) {
        binding.fabEditPhoto.setOnClickListener {
            val options = arrayOf("Choose from Gallery", "Remove Profile Picture", "Cancel")
            AlertDialog.Builder(this)
                .setTitle("Profile Picture")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> pickImageLauncher.launch("image/*")
                        1 -> removeProfilePicture(isAdmin)
                        else -> dialog.dismiss()
                    }
                }
                .show()
        }

        binding.btnPersonalInfo.setOnClickListener {
            Toast.makeText(this, "Edit Personal Info clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog(isAdmin)
        }

        binding.btnHelp.setOnClickListener {
            Toast.makeText(this, "Help Center coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        val isAdmin = intent.getBooleanExtra("isAdmin", false) || (AdminRepository.loggedInAdmin != null)
        val uid = if (isAdmin) AdminRepository.loggedInAdmin?.id else LandlordRepository.loggedInLandlord?.id
        
        if (uid == null) return
        
        val folder = if (isAdmin) "admin_profiles" else "landlord_profiles"
        val ref = storage.reference.child("$folder/$uid.jpg")

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()

        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { downloadUri ->
                updateProfileImage(downloadUri.toString(), isAdmin)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeProfilePicture(isAdmin: Boolean) {
        val uid = if (isAdmin) AdminRepository.loggedInAdmin?.id else LandlordRepository.loggedInLandlord?.id
        if (uid == null) return
        
        val folder = if (isAdmin) "admin_profiles" else "landlord_profiles"
        val ref = storage.reference.child("$folder/$uid.jpg")
        
        ref.delete().addOnCompleteListener {
            updateProfileImage(null, isAdmin)
        }
    }

    private fun updateProfileImage(uriString: String?, isAdmin: Boolean) {
        if (isAdmin) {
            val admin = AdminRepository.loggedInAdmin ?: return
            val updated = admin.copy(profileImageUri = uriString)
            AdminRepository.updateAdmin(updated)
            AdminRepository.loggedInAdmin = updated
        } else {
            val landlord = LandlordRepository.loggedInLandlord ?: return
            val updated = landlord.copy(profileImageUri = uriString)
            LandlordRepository.updateLandlord(updated)
            LandlordRepository.loggedInLandlord = updated
        }
        setupProfileInfo(isAdmin)
        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
    }

    private fun showChangePasswordDialog(isAdmin: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etNewPassword = dialogView.findViewById<android.widget.EditText>(R.id.etNewPassword)
        
        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newPassword = etNewPassword.text.toString().trim()
                if (newPassword.isNotEmpty()) {
                    if (isAdmin) {
                        val current = AdminRepository.loggedInAdmin
                        if (current != null) {
                            val updated = current.copy(password = newPassword)
                            AdminRepository.updateAdmin(updated)
                            AdminRepository.loggedInAdmin = updated
                            Toast.makeText(this, "Admin password updated", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val current = LandlordRepository.loggedInLandlord
                        if (current != null) {
                            val updated = current.copy(password = newPassword, mustChangePassword = false)
                            LandlordRepository.updateLandlord(updated)
                            LandlordRepository.loggedInLandlord = updated
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
