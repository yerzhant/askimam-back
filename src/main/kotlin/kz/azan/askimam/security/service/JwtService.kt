package kz.azan.askimam.security.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.vavr.control.Either
import io.vavr.kotlin.Try
import kz.azan.askimam.AppProperties
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class JwtService(
    appProperties: AppProperties,
    private val clock: Clock,
) {
    private val issuer = "askimam.azan.kz"
    private val idClaim = "id"
    private val typeClaim = "type"

    init {
        if (appProperties.jwt.key.toByteArray().size < 32)
            throw IllegalArgumentException("Key is too short")
    }

    private val algorithm = Algorithm.HMAC256(appProperties.jwt.key)

    private val verifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withClaimPresence(idClaim)
        .withClaimPresence(typeClaim)
        .build()

    fun sign(user: User): Either<Declination, String> =
        Try {
            JWT.create()
                .withIssuer(issuer)
                .withIssuedAt(Date.from(Instant.now(clock)))
                .withExpiresAt(Date.from(Instant.now(clock).plus(1, ChronoUnit.DAYS)))
                .withClaim(idClaim, user.id.value)
                .withClaim(typeClaim, user.type.name)
                .sign(algorithm)
        }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it }
            )

    fun verify(token: String): Either<Declination, DecodedJWT> =
        Try { verifier.verify(token) }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it }
            )

    fun decode(decodedJWT: DecodedJWT) =
        User(
            User.Id(decodedJWT.getClaim(idClaim).asLong()),
            User.Type.valueOf(decodedJWT.getClaim(typeClaim).asString()),
            NonBlankString.of("A hydrated user"),
            NonBlankString.of("--- unused ---"),
        )
}