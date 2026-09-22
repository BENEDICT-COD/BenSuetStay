package com.mansuetdev.bensuetstay

import com.google.firebase.firestore.FirebaseFirestore

object ApplicationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("applications")

    val applications = mutableListOf<StudentApplication>()

    private val listeners = mutableListOf<() -> Unit>()
    fun addListener(l: () -> Unit) { listeners.add(l) }
    fun removeListener(l: () -> Unit) { listeners.remove(l) }
    private fun notifyListeners() { listeners.forEach { it() } }

    init {
        collection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val newList = mutableListOf<StudentApplication>()
                for (doc in snapshot.documents) {
                    val id = doc.id
                    val studentName = doc.getString("studentName") ?: ""
                    val accommodationName = doc.getString("accommodationName") ?: ""
                    val typeStr = doc.getString("applicationType") ?: "NSFAS_CONFIRMATION"
                    val applicationType = if (typeStr == "SELF_FUNDED_REGISTRATION") ApplicationType.SELF_FUNDED_REGISTRATION else ApplicationType.NSFAS_CONFIRMATION
                    val dateApplied = doc.getString("dateApplied") ?: ""
                    val statusStr = doc.getString("status") ?: "SUBMITTED"
                    val status = try { ApplicationStatus.valueOf(statusStr) } catch(e: Exception) { ApplicationStatus.SUBMITTED }
                    
                    val idNumber = doc.getString("idNumber")
                    val studentNumber = doc.getString("studentNumber")
                    val campus = doc.getString("campus")
                    val roomSelected = doc.getString("roomSelected")
                    val nextOfKinName = doc.getString("nextOfKinName")
                    val nextOfKinPhone = doc.getString("nextOfKinPhone")
                    val documents = doc.get("documents") as? List<String> ?: emptyList()
                    val feedback = doc.getString("feedback")

                    newList.add(
                        StudentApplication(
                            id = id,
                            studentName = studentName,
                            accommodationName = accommodationName,
                            applicationType = applicationType,
                            dateApplied = dateApplied,
                            status = status,
                            idNumber = idNumber,
                            studentNumber = studentNumber,
                            campus = campus,
                            roomSelected = roomSelected,
                            nextOfKinName = nextOfKinName,
                            nextOfKinPhone = nextOfKinPhone,
                            documents = documents,
                            feedback = feedback
                        )
                    )
                }
                applications.clear()
                applications.addAll(newList)
                notifyListeners()
            } else if (snapshot != null && snapshot.isEmpty) {
                applications.clear()
                notifyListeners()
            }
        }
    }

    private fun saveToFirestore(app: StudentApplication) {
        val data = mapOf(
            "studentName" to app.studentName,
            "accommodationName" to app.accommodationName,
            "applicationType" to app.applicationType.name,
            "dateApplied" to app.dateApplied,
            "status" to app.status.name,
            "idNumber" to app.idNumber,
            "studentNumber" to app.studentNumber,
            "campus" to app.campus,
            "roomSelected" to app.roomSelected,
            "nextOfKinName" to app.nextOfKinName,
            "nextOfKinPhone" to app.nextOfKinPhone,
            "documents" to app.documents,
            "feedback" to app.feedback
        )
        collection.document(app.id).set(data)
    }

    fun findById(id: String): StudentApplication? = applications.find { it.id == id }

    fun addApplication(app: StudentApplication) {
        applications.add(app)
        saveToFirestore(app)
    }

    fun updateStatus(id: String, status: ApplicationStatus, feedback: String? = null) {
        val index = applications.indexOfFirst { it.id == id }
        if (index != -1) {
            applications[index] = applications[index].copy(status = status, feedback = feedback)
            saveToFirestore(applications[index])
        }
    }
}
