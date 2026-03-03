package pl.akp.healthybite.domain.validation

enum class PasswordRule(val message: String) {
    MIN_LENGTH("At least 8 characters"),
    LOWERCASE("Missing lowercase letter"),
    UPPERCASE("Missing uppercase letter"),
    DIGIT("Missing digit"),
    SPECIAL_CHAR("Missing special character")
}
