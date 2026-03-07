package pl.akp.healthybite.domain.validation

import android.util.Patterns

/**
 * Validates email format using the Android [Patterns.EMAIL_ADDRESS] regex.
 *
 * Called in LoginScreen and RegisterScreen for real-time validation as the
 * user types, so the Login / Register button is only enabled when the format
 * is structurally valid (not verified via a server round-trip).
 */
object EmailValidator {
    /**
     * Returns true when [email] is non-blank and matches Android's built-in
     * EMAIL_ADDRESS pattern, which covers most RFC-compliant addresses.
     */
    fun isValid(email: String): Boolean {
        // Blank check first to skip the heavier regex match on empty input
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
