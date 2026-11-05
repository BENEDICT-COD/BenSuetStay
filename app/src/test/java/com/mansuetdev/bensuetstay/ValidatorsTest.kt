package com.mansuetdev.bensuetstay

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test fun saId_acceptsValidNumber() {
        assertTrue(Validators.isValidSaId("8001015009087"))
    }

    @Test fun saId_rejectsBadInput() {
        assertFalse("wrong check digit", Validators.isValidSaId("8001015009086"))
        assertFalse("too short", Validators.isValidSaId("800101500908"))
        assertFalse("letters", Validators.isValidSaId("80010150090AB"))
        assertFalse("month 13", Validators.isValidSaId("8013015009087"))
        assertFalse("citizenship digit 5", Validators.isValidSaId("8001015009587"))
    }

    @Test fun phone_acceptsSouthAfricanFormats() {
        assertTrue(Validators.isValidSaPhone("082 123 4567"))
        assertTrue(Validators.isValidSaPhone("+27 82 123 4567"))
        assertTrue(Validators.isValidSaPhone("(031) 123-4567"))
        assertFalse(Validators.isValidSaPhone("12345"))
        assertFalse(Validators.isValidSaPhone("082 123 45678"))
    }

    @Test fun email_basicShape() {
        assertTrue(Validators.isValidEmail("student@myrosebank.ac.za"))
        assertFalse(Validators.isValidEmail("student@"))
        assertFalse(Validators.isValidEmail("no spaces@mail.com"))
    }
}
