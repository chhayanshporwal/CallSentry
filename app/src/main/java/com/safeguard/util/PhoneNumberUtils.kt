package com.safeguard.util

object PhoneNumberUtils {

    /** Normalize a phone number by removing all non-digit characters except leading + */
    fun normalize(phoneNumber: String): String {
        val cleaned = phoneNumber.trim()
        val hasPlus = cleaned.startsWith("+")
        val digits = cleaned.replace(Regex("[^0-9]"), "")

        return if (hasPlus) "+$digits" else digits
    }

    /**
     * Get alternative formats of a phone number for matching E.g., +91XXXXXXXXXX, 91XXXXXXXXXX,
     * 0XXXXXXXXXX, XXXXXXXXXX
     */
    fun getAlternativeFormats(phoneNumber: String): List<String> {
        val normalized = normalize(phoneNumber)
        val result = mutableListOf(normalized)

        // If has country code
        if (normalized.startsWith("+91")) {
            val withoutPlus = normalized.removePrefix("+")
            val withoutCode = normalized.removePrefix("+91")
            result.add(withoutPlus)
            result.add(withoutCode)
            result.add("0$withoutCode")
        } else if (normalized.startsWith("91") && normalized.length > 10) {
            result.add("+$normalized")
            val withoutCode = normalized.removePrefix("91")
            result.add(withoutCode)
            result.add("0$withoutCode")
        } else if (normalized.startsWith("0") && normalized.length == 11) {
            val withoutZero = normalized.removePrefix("0")
            result.add(withoutZero)
            result.add("+91$withoutZero")
            result.add("91$withoutZero")
        } else if (normalized.length == 10) {
            // Assume Indian number
            result.add("+91$normalized")
            result.add("91$normalized")
            result.add("0$normalized")
        }

        // Also try with any country code format
        if (normalized.startsWith("+")) {
            result.add(normalized.removePrefix("+"))
        } else if (!result.any { it.startsWith("+") }) {
            result.add("+$normalized")
        }

        return result.distinct()
    }

    /** Format phone number for display */
    fun formatForDisplay(phoneNumber: String): String {
        val normalized = normalize(phoneNumber)

        return when {
            normalized.startsWith("+91") && normalized.length == 13 -> {
                val number = normalized.removePrefix("+91")
                "+91 ${number.take(5)} ${number.takeLast(5)}"
            }
            normalized.length == 10 -> {
                "${normalized.take(5)} ${normalized.takeLast(5)}"
            }
            else -> normalized
        }
    }

    /** Check if emergency number */
    fun isEmergencyNumber(phoneNumber: String): Boolean {
        val normalized = normalize(phoneNumber)
        val emergencyNumbers =
                listOf(
                        "100",
                        "101",
                        "102",
                        "103",
                        "104",
                        "108",
                        "112", // India
                        "911", // US
                        "999", // UK
                        "000" // Australia
                )
        return emergencyNumbers.any { normalized == it }
    }
}
