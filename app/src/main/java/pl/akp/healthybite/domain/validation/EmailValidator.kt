package pl.akp.healthybite.domain.validation

import java.util.regex.Pattern

/**
 * Validates email format using a pattern aligned with Android's [android.util.Patterns.EMAIL_ADDRESS].
 *
 * Implemented with [java.util.regex.Pattern] so the same checks run on the JVM in unit tests
 * (no Android stubs required).
 */
object EmailValidator {

    private val EMAIL_ADDRESS: Pattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )

    fun isValid(email: String): Boolean {
        return email.isNotBlank() && EMAIL_ADDRESS.matcher(email).matches()
    }
}
