package kz.azan.askimam.common.web.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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
                logger.error("Jwt verification failed for: $it")
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