package pl.akp.healthybite.domain.validation

/**
 * Checks a password against the complexity rules defined in [PasswordRule].
 *
 * Used on the Register screen to provide real-time feedback as the user types.
 */
object PasswordValidator {

    /**
     * Checks [password] against every [PasswordRule] and returns the ones that
     * are NOT satisfied. RegisterViewModel calls this on every keystroke so the
     * UI can show which requirements are still unmet in real time.
     */
    fun missingRules(password: String): List<PasswordRule> {
        val missing = mutableListOf<PasswordRule>()

        // Rule: password must be at least 8 characters long
        if (password.length < 8) missing.add(PasswordRule.MIN_LENGTH)

        // Rule: at least one lowercase letter (a-z)
        if (password.none { it.isLowerCase() }) missing.add(PasswordRule.LOWERCASE)

        // Rule: at least one uppercase letter (A-Z)
        if (password.none { it.isUpperCase() }) missing.add(PasswordRule.UPPERCASE)

        // Rule: at least one digit (0-9)
        if (password.none { it.isDigit() }) missing.add(PasswordRule.DIGIT)

        // Rule: at least one special character – if every char is a letter or
        // digit then no special character is present
        if (password.all { it.isLetterOrDigit() }) missing.add(PasswordRule.SPECIAL_CHAR)

        return missing
    }

    /**
     * Convenience shorthand: returns true only when the password satisfies ALL
     * rules. Used by RegisterViewModel to enable/disable the Register button.
     */
    fun isValid(password: String): Boolean = missingRules(password).isEmpty()
}
