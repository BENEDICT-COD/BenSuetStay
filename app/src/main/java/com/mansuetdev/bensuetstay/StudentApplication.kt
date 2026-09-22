package com.mansuetdev.bensuetstay

enum class ApplicationStatus { SUBMITTED, VIEWED, APPROVED, DECLINED }
enum class ApplicationType { NSFAS_CONFIRMATION, SELF_FUNDED_REGISTRATION }

data class StudentApplication(
    val id: String,
    val studentName: String,
    val accommodationName: String,
    val applicationType: ApplicationType,
    val dateApplied: String,
    var status: ApplicationStatus,
    // NSFAS-specific
    val idNumber: String? = null,
    val studentNumber: String? = null,
    val campus: String? = null,
    // Self-funded specific
    val roomSelected: String? = null,
    val nextOfKinName: String? = null,
    val nextOfKinPhone: String? = null,
    val documents: List<String> = emptyList(),
    var feedback: String? = null
)