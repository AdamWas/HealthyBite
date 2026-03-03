package pl.akp.healthybite.domain.validation

object PasswordValidator {
    fun isValid(password: String): Boolean {
        return password.length >= 6
    }
}
