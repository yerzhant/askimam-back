package kz.azan.askimam.common.web.config

import kz.azan.askimam.common.web.security.JwtFilter
import kz.azan.askimam.common.web.security.JwtService
import kz.azan.askimam.user.domain.model.User.Type.Imam
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import javax.servlet.http.HttpServletResponse

@EnableWebSecurity
class WebSecurityConfig(private val jwtService: JwtService) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http {
            authorizeRequests {
                authorize("/chats/public/*/*", permitAll)
                authorize("/chats/messages/*/*", permitAll)
                authorize("/chats/unanswered/*/*", hasAuthority(Imam.name))
                authorize("/chats/*/return-to-unanswered", hasAuthority(Imam.name))
                authorize("/messages/audio", hasAuthority(Imam.name))
                authorize()
            }
            cors { }
            csrf { disable() }
            addFilterBefore(JwtFilter(jwtService), UsernamePasswordAuthenticationFilter::class.java)
            exceptionHandling {
                authenticationEntryPoint = AuthenticationEntryPoint { _, response, authException ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.message)
                }
            }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
        }
    }

    @Bean
    fun corsFilter() = UrlBasedCorsConfigurationSource().let {
        it.registerCorsConfiguration("/**",
            CorsConfiguration().apply {
                allowCredentials = true
                addAllowedOrigin("*")
                addAllowedHeader("*")
                addAllowedMethod("*")
            }
        )
        CorsFilter(it)
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}