package kz.azan.askimam.common.type

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class NonBlankStringTest {
    @Test
    internal fun `should contain a string`() {
        assertThat(NonBlankString.of("A string").value).isEqualTo("A string")
    }

    @Test
    internal fun `should trim a string`() {
        assertThat(NonBlankString.of(" A string ").value).isEqualTo("A string")
    }

    @Test
    internal fun `should not be empty`() {
        assertThrows<IllegalArgumentException> { NonBlankString.of("") }
    }

    @Test
    internal fun `should not be blank`() {
        assertThrows<IllegalArgumentException> { NonBlankString.of(" ") }
    }

    @Test
    internal fun `should be equal`() {
        assertThat(NonBlankString.of("A string")).isEqualTo(NonBlankString.of("A string"))
    }

    @Test
    internal fun `should not be equal`() {
        assertThat(NonBlankString.of("A string")).isNotEqualTo(NonBlankString.of("A String"))
    }
}