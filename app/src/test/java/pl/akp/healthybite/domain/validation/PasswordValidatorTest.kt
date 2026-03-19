package pl.akp.healthybite.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun `password shorter than 8 chars reports MIN_LENGTH`() {
        val missing = PasswordValidator.missingRules("aB1@xyz")
        assertTrue(missing.contains(PasswordRule.MIN_LENGTH))
    }

    @Test
    fun `password missing lowercase reports LOWERCASE`() {
        val missing = PasswordValidator.missingRules("ABCDEF1@9")
        assertTrue(missing.contains(PasswordRule.LOWERCASE))
        assertFalse(missing.contains(PasswordRule.UPPERCASE))
    }

    @Test
    fun `password missing uppercase reports UPPERCASE`() {
        val missing = PasswordValidator.missingRules("abcdef1@9")
        assertTrue(missing.contains(PasswordRule.UPPERCASE))
        assertFalse(missing.contains(PasswordRule.LOWERCASE))
    }

    @Test
    fun `password missing digit reports DIGIT`() {
        val missing = PasswordValidator.missingRules("abcDef@@x")
        assertTrue(missing.contains(PasswordRule.DIGIT))
        assertFalse(missing.contains(PasswordRule.SPECIAL_CHAR))
    }

    @Test
    fun `password missing special char reports SPECIAL_CHAR`() {
        val missing = PasswordValidator.missingRules("abcDef123")
        assertTrue(missing.contains(PasswordRule.SPECIAL_CHAR))
        assertFalse(missing.contains(PasswordRule.DIGIT))
    }

    @Test
    fun `valid password passes all rules`() {
        assertTrue(PasswordValidator.isValid("zaq1@WSX9"))
    }

    @Test
    fun `valid password returns empty missing rules`() {
        val missing = PasswordValidator.missingRules("zaq1@WSX9")
        assertEquals(emptyList<PasswordRule>(), missing)
    }

    @Test
    fun `password with fewer than 8 chars fails MIN_LENGTH`() {
        // Full demo password is "zaq1@WSX" (8 chars) — this 7-char variant only fails length.
        val missing = PasswordValidator.missingRules("zaq1@WS")
        assertTrue(missing.contains(PasswordRule.MIN_LENGTH))
        assertEquals(listOf(PasswordRule.MIN_LENGTH), missing)
    }

    @Test
    fun `empty password fails all rules`() {
        val missing = PasswordValidator.missingRules("")
        assertEquals(5, missing.size)
        assertFalse(PasswordValidator.isValid(""))
    }
}
