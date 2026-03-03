package pl.akp.healthybite.domain.validation

object PasswordValidator {

    fun missingRules(password: String): List<PasswordRule> {
        val missing = mutableListOf<PasswordRule>()
        if (password.length < 8) missing.add(PasswordRule.MIN_LENGTH)
        if (password.none { it.isLowerCase() }) missing.add(PasswordRule.LOWERCASE)
        if (password.none { it.isUpperCase() }) missing.add(PasswordRule.UPPERCASE)
        if (password.none { it.isDigit() }) missing.add(PasswordRule.DIGIT)
        if (password.all { it.isLetterOrDigit() }) missing.add(PasswordRule.SPECIAL_CHAR)
        return missing
    }

    fun isValid(password: String): Boolean = missingRules(password).isEmpty()
}
