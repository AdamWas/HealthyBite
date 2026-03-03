package pl.akp.healthybite.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EmailValidatorTest {

    @Test
    fun `valid email passes`() {
        assertTrue(EmailValidator.isValid("demo@healthy.pl"))
    }

    @Test
    fun `invalid email without domain fails`() {
        assertFalse(EmailValidator.isValid("demo@"))
    }

    @Test
    fun `blank email fails`() {
        assertFalse(EmailValidator.isValid(""))
    }

    @Test
    fun `email without at sign fails`() {
        assertFalse(EmailValidator.isValid("demohealthy.pl"))
    }
}
