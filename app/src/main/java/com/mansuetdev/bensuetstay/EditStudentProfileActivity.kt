package com.mansuetdev.bensuetstay

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mansuetdev.bensuetstay.databinding.ActivityEditStudentProfileBinding

class EditStudentProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditStudentProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val student = StudentRepository.loggedInStudent
        if (student == null) {
            finish()
            return
        }

        // Pre-fill existing data
        binding.etFullName.setText(student.fullName)
        binding.etAddress.setText(student.address)
        binding.etInstitution.setText(student.institution)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val fullName = binding.etFullName.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val institution = binding.etInstitution.text.toString().trim()

        if (fullName.isEmpty() || address.isEmpty() || institution.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid ?: return
        
        binding.btnSaveChanges.isEnabled = false
        binding.btnSaveChanges.text = "Saving..."

        val updates = mapOf(
            "fullNames" to fullName,
            "address" to address,
            "institution" to institution
        )

        firestore.collection("students").document(uid)
            .update(updates)
            .addOnSuccessListener {
                // Update local repository
                val current = StudentRepository.loggedInStudent
                if (current != null) {
                    StudentRepository.loggedInStudent = current.copy(
                        fullName = fullName,
                        address = address,
                        institution = institution
                    )
                }
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnSaveChanges.isEnabled = true
                binding.btnSaveChanges.text = "Save Changes"
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}