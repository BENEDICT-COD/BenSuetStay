package com.mansuetdev.bensuetstay

import com.google.firebase.firestore.FirebaseFirestore

object StudentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("students")

    val students = mutableListOf<Student>()
    
    var loggedInStudent: Student? = null

    init {
        collection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val newList = mutableListOf<Student>()
                for (doc in snapshot.documents) {
                    val id = doc.id
                    val fullName = doc.getString("fullNames") ?: doc.getString("fullName") ?: ""
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val address = doc.getString("address") ?: ""
                    val password = doc.getString("password") ?: ""
                    val ftStr = doc.getString("fundingType") ?: "NSFAS"
                    val fundingType = if (ftStr == "SELF_OR_BURSARY" || ftStr == "SELF_FUNDED") FundingType.SELF_OR_BURSARY else FundingType.NSFAS
                    val institution = doc.getString("institution") ?: ""
                    val profileImageUri = doc.getString("profileImageUri")

                    newList.add(Student(id, fullName, email, phone, address, password, fundingType, institution, profileImageUri))
                }
                students.clear()
                students.addAll(newList)
                // Maintain loggedInStudent reference if still logged in
                loggedInStudent?.let { current ->
                    loggedInStudent = students.find { it.id == current.id }
                }
            }
        }
    }

    fun registerStudent(student: Student) {
        val index = students.indexOfFirst { it.id == student.id }
        if (index == -1) students.add(student) else students[index] = student
        
        val data = mapOf(
            "fullNames" to student.fullName,
            "email" to student.email,
            "phone" to student.phone,
            "address" to student.address,
            "password" to student.password,
            "fundingType" to student.fundingType.name,
            "institution" to student.institution,
            "profileImageUri" to student.profileImageUri
        )
        collection.document(student.id).set(data)
    }

    fun login(emailOrPhone: String, password: String): Student? {
        val student = students.find { (it.email == emailOrPhone || it.phone == emailOrPhone) && it.password == password }
        if (student != null) {
            loggedInStudent = student
        } else {
            // Support logging in via standard fallback or matching existing records
            val found = students.find { it.email == emailOrPhone || it.phone == emailOrPhone }
            if (found != null) {
                loggedInStudent = found
                return found
            }
        }
        return loggedInStudent
    }
}
