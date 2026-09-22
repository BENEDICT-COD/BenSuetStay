package com.mansuetdev.bensuetstay

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mansuetdev.bensuetstay.databinding.ActivityStudentProfileBinding

class StudentProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Pick profile picture from gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadProfilePicture(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val student = StudentRepository.loggedInStudent
        if (student == null) {
            finish()
            return
        }

        setupProfileInfo()
        setupPhotoActions()
        setupPreferences()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditStudentProfileActivity::class.java))
        }

        binding.btnBookings.setOnClickListener {
            startActivity(Intent(this, StudentBookingsActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            StudentRepository.loggedInStudent = null
            auth.signOut()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        setupProfileInfo()
    }

    private fun setupProfileInfo() {
        val student = StudentRepository.loggedInStudent ?: return
        binding.tvProfileName.text = student.fullName
        binding.tvProfileEmail.text = student.email

        // Load profile picture using Coil
        if (!student.profileImageUri.isNullOrEmpty()) {
            binding.ivProfilePicture.load(student.profileImageUri) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
                transformations(CircleCropTransformation())
            }
        } else {
            binding.ivProfilePicture.setImageResource(R.drawable.ic_person)
        }
    }

    private fun setupPhotoActions() {
        binding.fabEditPhoto.setOnClickListener {
            val options = arrayOf("Choose from Gallery", "Remove Profile Picture", "Cancel")
            AlertDialog.Builder(this)
                .setTitle("Profile Picture")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> pickImageLauncher.launch("image/*")
                        1 -> removeProfilePicture()
                        else -> dialog.dismiss()
                    }
                }
                .show()
        }
    }

    private fun setupPreferences() {
        val isNightMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.switchDarkMode.isChecked = isNightMode
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.btnLanguage.setOnClickListener {
            val languages = arrayOf("English", "isiZulu", "isiXhosa", "Afrikaans", "Sepedi", "Setswana")
            AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setItems(languages) { _, which ->
                    binding.tvCurrentLanguage.text = languages[which]
                    Toast.makeText(this, "Language switched to ${languages[which]}", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profile_pictures/$uid.jpg")

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()

        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { downloadUri ->
                updateFirestoreProfileImage(downloadUri.toString())
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeProfilePicture() {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profile_pictures/$uid.jpg")

        ref.delete().addOnCompleteListener {
            // Even if delete from storage fails (e.g. file doesn't exist), we still clear Firestore
            updateFirestoreProfileImage(null)
        }
    }

    private fun updateFirestoreProfileImage(uriString: String?) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("students").document(uid)
            .update("profileImageUri", uriString)
            .addOnSuccessListener {
                // Update local repository
                StudentRepository.loggedInStudent = StudentRepository.loggedInStudent?.copy(profileImageUri = uriString)
                setupProfileInfo()
                Toast.makeText(this, if (uriString != null) "Profile updated" else "Profile picture removed", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile data", Toast.LENGTH_SHORT).show()
            }
    }
}
