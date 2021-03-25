package kz.azan.askimam.meta

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@WithSecurityContext(factory = JwtSecurityContextFactory::class)
annotation class WithPrincipal

class JwtSecurityContextFactory : WithSecurityContextFactory<WithPrincipal> {

    override fun createSecurityContext(annotation: WithPrincipal?): SecurityContext =
        SecurityContextHolder.createEmptyContext().apply {
            authentication = UsernamePasswordAuthenticationToken(1, null, null)
        }
}