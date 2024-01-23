package kz.azan.askimam.security.web.config

import jakarta.servlet.http.HttpServletResponse
import kz.azan.askimam.security.web.filter.JwtFilter
import kz.azan.askimam.user.domain.model.User.Type.Imam
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val jwtFilter: JwtFilter,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                authorize("/chats/public/*/*", permitAll)
                authorize("/chats/messages/*", permitAll)
                authorize("/chats/find/*", permitAll)
                authorize("/chats/unanswered/*/*", hasAuthority(Imam.name))
                authorize("/chats/*/return-to-unanswered", hasAuthority(Imam.name))
                authorize("/messages/audio", hasAuthority(Imam.name))
                authorize("/messages/upload-audio", hasAuthority(Imam.name))
                authorize("/imam-ratings", permitAll)
                authorize("/imam-ratings/with-desc", permitAll)
                authorize("/auth/login", permitAll)
                authorize(anyRequest, fullyAuthenticated)
            }

            exceptionHandling {
                authenticationEntryPoint = AuthenticationEntryPoint { _, response, authException ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.message)
                }
            }

            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }

            logout { disable() }
            csrf { disable() }
            cors { }
        }

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsFilter() = UrlBasedCorsConfigurationSource().let {
        it.registerCorsConfiguration("/**",
            CorsConfiguration().apply {
                addAllowedOriginPattern("*")
                addAllowedHeader("*")
                addAllowedMethod("*")
                allowCredentials = true
            }
        )

        CorsFilter(it)
    }

    @Bean
    fun authManager(config: AuthenticationConfiguration): AuthenticationManager = config.authenticationManager

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}