package kz.azan.askimam.security.web.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kz.azan.askimam.security.service.JwtService
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = header.split(" ").last().trim()

        jwtService.verify(token).map { jwtService.decode(it) }.bimap(
            {
                logger.error("Jwt verification failed for: ${it.reason.value}")
                filterChain.doFilter(request, response)
            },
            {
                SecurityContextHolder.getContext().apply {
                    authentication = UsernamePasswordAuthenticationToken(
                        it, null, setOf(SimpleGrantedAuthority(it.type.name))
                    ).apply {
                        details = WebAuthenticationDetailsSource().buildDetails(request)
                    }
                }

                filterChain.doFilter(request, response)
            }
        )
    }
}