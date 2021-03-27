package kz.azan.askimam.meta

import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@WithSecurityContext(factory = JwtSecurityContextFactory::class)
annotation class WithPrincipal(
    val id: Long = 1,
    val authority: User.Type = Inquirer,
)

class JwtSecurityContextFactory : WithSecurityContextFactory<WithPrincipal> {

    override fun createSecurityContext(annotation: WithPrincipal?): SecurityContext =
        SecurityContextHolder.createEmptyContext().apply {
            authentication = UsernamePasswordAuthenticationToken(
                annotation?.id,
                null,
                setOf(SimpleGrantedAuthority(annotation?.authority.toString()))
            )
        }
}