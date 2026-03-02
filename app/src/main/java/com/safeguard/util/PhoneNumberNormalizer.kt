package com.safeguard.util

/**
 * Utility for normalizing phone numbers to a consistent format. Converts all Indian phone numbers
 * to +91XXXXXXXXXX format.
 */
object PhoneNumberNormalizer {

    /**
     * Normalizes a phone number to +91XXXXXXXXXX format.
     *
     * Handles:
     * - 0XXXXXXXXXX → +91XXXXXXXXXX
     * - XXXXXXXXXX → +91XXXXXXXXXX
     * - +91XXXXXXXXXX → +91XXXXXXXXXX (no change)
     */
    fun normalize(phoneNumber: String): String {
        // Remove all non-digit characters except +
        var normalized = phoneNumber.replace(Regex("[^0-9+]"), "")

        // Remove leading zeros and replace with +91
        if (normalized.startsWith("0") && normalized.length == 11) {
            normalized = "+91${normalized.substring(1)}"
        }

        // Add +91 prefix if missing and number is 10 digits
        if (!normalized.startsWith("+") && normalized.length == 10) {
            normalized = "+91$normalized"
        }

        // Handle 91 prefix without plus (e.g. 919876543210)
        if (!normalized.startsWith("+") && normalized.startsWith("91") && normalized.length == 12) {
            normalized = "+$normalized"
        }

        return normalized
    }

    /**
     * Compares two phone numbers after normalization. Returns true if they represent the same
     * number.
     */
    fun areEqual(number1: String, number2: String): Boolean {
        return normalize(number1) == normalize(number2)
    }
}
