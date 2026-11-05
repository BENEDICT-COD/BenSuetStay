package com.mansuetdev.bensuetstay

/** Input checks with no Android dependencies (see ValidatorsTest). */
object Validators {

    const val MIN_PASSWORD_LENGTH = 6 // Firebase Auth's own minimum

    private val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

    /** South African numbers: 0XX XXX XXXX or +27 XX XXX XXXX (spaces, dashes and brackets allowed). */
    fun isValidSaPhone(phone: String): Boolean {
        val compact = phone.replace(Regex("[\\s()-]"), "")
        return Regex("^(\\+27|0)\\d{9}$").matches(compact)
    }

    /**
     * South African ID number: 13 digits, a real YYMMDD date of birth,
     * citizenship digit 0/1/2, and a valid Luhn check digit.
     */
    fun isValidSaId(id: String): Boolean {
        if (!Regex("^\\d{13}$").matches(id)) return false
        val month = id.substring(2, 4).toInt()
        val day = id.substring(4, 6).toInt()
        if (month !in 1..12 || day !in 1..31) return false
        if (id[10] !in "012") return false
        return passesLuhn(id)
    }

    fun passesLuhn(number: String): Boolean {
        var sum = 0
        number.reversed().forEachIndexed { index, c ->
            var digit = c - '0'
            if (index % 2 == 1) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            sum += digit
        }
        return sum % 10 == 0
    }
}
