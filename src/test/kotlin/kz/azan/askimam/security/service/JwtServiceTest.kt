package kz.azan.askimam.security.service

import com.auth0.jwt.interfaces.DecodedJWT
import io.mockk.every
import io.vavr.control.Either.left
import io.vavr.control.Either.right
import kz.azan.askimam.AppProperties
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.userdetails.UsernameNotFoundException

internal class JwtServiceTest : ChatFixtures() {

    @Suppress("SpellCheckingInspection")
    private val properties = AppProperties(AppProperties.Jwt("xgIEe6JCu6PGkw8MxCDYQTzIXpUQ4PMqq0gRaezLNehilBlk"), "x")

    private val service = JwtService(properties, clock, userRepository)

    @Test
    internal fun `should fail key length check`() {
        assertThrows<IllegalArgumentException> {
            JwtService(AppProperties(AppProperties.Jwt("123"), "x"), clock, userRepository)
        }
    }

    @Test
    internal fun `should generate a signature`() {
        fixtureClock()

        assertThat(
            service.sign(fixtureImam).get()
        ).startsWith("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhc2tpbWFtLmF6YW4ua3oiLCJpZCI6MSwi")
    }

    @Test
    internal fun `should verify a signature`() {
        fixtureClock()
        val token = service.sign(fixtureImam).get()

        val decodedJWT = service.verify(token).get()

        assertThat(decodedJWT).isInstanceOf(DecodedJWT::class.java)
        assertThat(decodedJWT.getClaim("id").asLong()).isEqualTo(fixtureImam.id.value)
        assertThat(decodedJWT.getClaim("type").asString()).isEqualTo(fixtureImam.type.name)
    }

    @Test
    internal fun `should fail verification`() {
        fixtureClock()

        val token = service.sign(fixtureImam).get()

        assertThat(service.verify("$token.x").isLeft).isTrue
    }

    @Test
    internal fun `should return a user`() {
        every { userRepository.findById(fixtureImamId) } returns right(
            User(
                fixtureImamId,
                fixtureImam.type,
                fixtureImam.name,
                fixturePasswordHash,
                mutableSetOf(fixtureImamFcmToken),
            )
        )
        fixtureClock()
        val token = service.sign(fixtureImam).get()
        val decodedJWT = service.verify(token).get()

        val user = service.decode(decodedJWT)

        assertThat(user.id).isEqualTo(fixtureImam.id)
        assertThat(user.type).isEqualTo(fixtureImam.type)
    }

    @Test
    internal fun `should return a user - inconsistent roles`() {
        every { userRepository.findById(fixtureImamId) } returns right(
            User(
                fixtureImamId,
                fixtureInquirer.type,
                fixtureImam.name,
                fixturePasswordHash,
                mutableSetOf(fixtureImamFcmToken),
            )
        )
        fixtureClock()
        val token = service.sign(fixtureImam).get()
        val decodedJWT = service.verify(token).get()

        val user = service.decode(decodedJWT)

        assertThat(user.id).isEqualTo(fixtureImam.id)
        assertThat(user.type).isEqualTo(fixtureInquirer.type)
    }

    @Test
    internal fun `should not find a user on decoding`() {
        every { userRepository.findById(fixtureImamId) } returns left(Declination(NonBlankString.of("XXX")))
        fixtureClock()
        val token = service.sign(fixtureImam).get()
        val decodedJWT = service.verify(token).get()

        assertThrows<UsernameNotFoundException> {
            service.decode(decodedJWT)
        }
    }
}