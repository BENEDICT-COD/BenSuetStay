package com.mansuetdev.bensuetstay

data class AdminStats(
    val totalListings: Int,
    val totalLandlords: Int,
    val pendingApprovals: Int,
    val totalStudents: Int,
    val totalApplications: Int,
    val totalBookings: Int
)