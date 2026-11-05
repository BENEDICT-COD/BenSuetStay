package com.mansuetdev.bensuetstay

<<<<<<< HEAD
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/** students/{uid}. Each student can only read and edit their own document (see firestore.rules). */
object StudentRepository {
    const val COLLECTION = "students"

    private val collection get() = FirebaseFirestore.getInstance().collection(COLLECTION)

    fun createProfile(student: Student): Task<Void> = collection.document(student.id).set(
        mapOf(
            "uid" to student.id,
            "fullNames" to student.fullName,
            "email" to student.email,
            "address" to student.address,
            "institution" to student.institution,
            "fundingType" to student.fundingType.name,
            "profileImageUri" to student.profileImageUri
        )
    )

    fun updateProfile(uid: String, fullName: String, address: String, institution: String): Task<Void> =
        collection.document(uid).update(
            mapOf("fullNames" to fullName, "address" to address, "institution" to institution)
        )

    /** Account deletion (Settings → Privacy). Allowed for the student themself by firestore.rules. */
    fun deleteProfile(uid: String): Task<Void> = collection.document(uid).delete()

    fun updateProfileImage(uid: String, url: String?): Task<Void> =
        collection.document(uid).update("profileImageUri", url)

    fun fromDoc(doc: DocumentSnapshot) = Student(
        id = doc.id,
        fullName = doc.getString("fullNames") ?: doc.getString("fullName") ?: "",
        email = doc.getString("email") ?: "",
        address = doc.getString("address") ?: "",
        fundingType = FundingType.fromStorage(doc.getString("fundingType")),
        institution = doc.getString("institution") ?: "",
        profileImageUri = doc.getString("profileImageUri")?.takeIf { it.isNotBlank() }
    )
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
}
