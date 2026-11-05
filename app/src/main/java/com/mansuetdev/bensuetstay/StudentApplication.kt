package com.mansuetdev.bensuetstay

enum class ApplicationStatus { SUBMITTED, VIEWED, APPROVED, DECLINED }
enum class ApplicationType { NSFAS_CONFIRMATION, SELF_FUNDED_REGISTRATION }

/** A file the student uploaded. url is null for records created before uploads existed. */
data class AppDocument(val name: String, val url: String?)

data class StudentApplication(
    val id: String,
    val studentId: String,
    val studentName: String,
    val studentEmail: String,
    val listingId: String,
    val accommodationName: String,
    val landlordId: String,
    val roomType: String,
    val applicationType: ApplicationType,
    val dateApplied: String,
    val createdAtMillis: Long,
    val status: ApplicationStatus,
    // NSFAS-specific
    val idNumber: String? = null,
    val studentNumber: String? = null,
    val campus: String? = null,
    // Self-funded specific
    val nextOfKinName: String? = null,
    val nextOfKinPhone: String? = null,
    val documents: List<AppDocument> = emptyList(),
    val feedback: String? = null,
    // Saved on this phone while offline, not sent yet
    val pendingSync: Boolean = false
) {
    val roomFundingType: FundingType
        get() = if (applicationType == ApplicationType.NSFAS_CONFIRMATION) FundingType.NSFAS else FundingType.SELF_OR_BURSARY

    val isActive: Boolean
        get() = status != ApplicationStatus.DECLINED
}
