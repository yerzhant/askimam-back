package kz.azan.askimam.common.type

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class NotBlankStringTest {
    @Test
    internal fun `should contain a string`() {
        assertThat(NotBlankString.of("A string").value).isEqualTo("A string")
    }

    @Test
    internal fun `should trim a string`() {
        assertThat(NotBlankString.of(" A string ").value).isEqualTo("A string")
    }

    @Test
    internal fun `should not be empty`() {
        assertThrows<IllegalArgumentException> { NotBlankString.of("") }
    }

    @Test
    internal fun `should not be blank`() {
        assertThrows<IllegalArgumentException> { NotBlankString.of(" ") }
    }

    @Test
    internal fun `should be equal`() {
        assertThat(NotBlankString.of("A string")).isEqualTo(NotBlankString.of("A string"))
    }

    @Test
    internal fun `should not be equal`() {
        assertThat(NotBlankString.of("A string")).isNotEqualTo(NotBlankString.of("A String"))
    }
}