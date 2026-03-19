package pl.akp.healthybite.domain.validation

/**
 * Password complexity rules with user-facing error messages.
 *
 * Each enum value maps to one specific requirement checked by
 * [PasswordValidator.missingRules]. The [message] property is displayed
 * directly on the Register screen so the user knows exactly which
 * requirement their password is still missing.
 */
enum class PasswordRule(val message: String) {
    MIN_LENGTH("At least 8 characters"),       // Minimum length gate – checked first for fast feedback
    LOWERCASE("Missing lowercase letter"),      // Ensures mixed-case: at least one a-z character
    UPPERCASE("Missing uppercase letter"),      // Ensures mixed-case: at least one A-Z character
    DIGIT("Missing digit"),                     // Ensures numeric content: at least one 0-9 character
    SPECIAL_CHAR("Missing special character")   // Ensures non-alphanumeric content (e.g. !@#$%)
}
