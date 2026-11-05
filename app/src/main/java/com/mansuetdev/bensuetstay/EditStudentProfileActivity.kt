package com.mansuetdev.bensuetstay

import android.os.Bundle
<<<<<<< HEAD
import com.mansuetdev.bensuetstay.databinding.ActivityEditStudentProfileBinding

class EditStudentProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityEditStudentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.STUDENT)) return
        binding = ActivityEditStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val student = Session.student ?: return
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        binding.etFullName.setText(student.fullName)
        binding.etAddress.setText(student.address)
        binding.etInstitution.setText(student.institution)

        binding.btnBack.setOnClickListener { finish() }
<<<<<<< HEAD
        binding.btnSaveChanges.setOnClickListener { saveChanges() }
=======

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
    }

    private fun saveChanges() {
        val fullName = binding.etFullName.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val institution = binding.etInstitution.text.toString().trim()
<<<<<<< HEAD
        if (fullName.isEmpty() || address.isEmpty() || institution.isEmpty()) {
            toast(getString(R.string.please_fill_in_all_fields))
            return
        }
        val current = Session.student ?: return

        setLoading(true)
        StudentRepository.updateProfile(current.id, fullName, address, institution)
            .addOnSuccessListener {
                Session.setStudent(current.copy(fullName = fullName, address = address, institution = institution))
                toast(getString(R.string.profile_updated))
                finish()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                toast(AuthErrors.message(this, e), long = true)
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSaveChanges.isEnabled = !loading
        binding.btnSaveChanges.text = if (loading) getString(R.string.saving) else getString(R.string.save_changes)
    }
}
=======

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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
