package com.mansuetdev.bensuetstay

import android.os.Bundle
import com.mansuetdev.bensuetstay.databinding.ActivityEditStudentProfileBinding

class EditStudentProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityEditStudentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.STUDENT)) return
        binding = ActivityEditStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val student = Session.student ?: return
        binding.etFullName.setText(student.fullName)
        binding.etAddress.setText(student.address)
        binding.etInstitution.setText(student.institution)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSaveChanges.setOnClickListener { saveChanges() }
    }

    private fun saveChanges() {
        val fullName = binding.etFullName.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val institution = binding.etInstitution.text.toString().trim()
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
