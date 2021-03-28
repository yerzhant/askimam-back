package kz.azan.askimam.security.service

import com.auth0.jwt.interfaces.DecodedJWT
import kz.azan.askimam.AppProperties
import kz.azan.askimam.chat.ChatFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class JwtServiceTest : ChatFixtures() {

    private val properties = AppProperties(AppProperties.Jwt("xgIEe6JCu6PGkw8MxCDYQTzIXpUQ4PMqq0gRaezLNehilBlk"))

    private val service = JwtService(properties, clock)

    @Test
    internal fun `should fail key length check`() {
        assertThrows<IllegalArgumentException> {
            JwtService(AppProperties(AppProperties.Jwt("123")), clock)
        }
    }

    @Test
    internal fun `should generate a signature`() {
        fixtureClock()

        assertThat(
            service.sign(fixtureImam).get()
        ).startsWith("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhc2tpbWFtLmF6YW4ua3oiLCJpZCI6MSwiZXh")
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
    internal fun `should fail a verification`() {
        fixtureClock()

        val token = service.sign(fixtureImam).get()

        assertThat(service.verify("$token.x").isLeft).isTrue
    }


    @Test
    internal fun `should return a user`() {
        fixtureClock()
        val token = service.sign(fixtureImam).get()
        val decodedJWT = service.verify(token).get()

        val user = service.decode(decodedJWT)

        assertThat(user.id).isEqualTo(fixtureImam.id)
        assertThat(user.type).isEqualTo(fixtureImam.type)
    }
}