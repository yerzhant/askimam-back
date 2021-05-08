package kz.azan.askimam.security.web.config

import kz.azan.askimam.security.service.UserService
import kz.azan.askimam.security.web.filter.JwtFilter
import kz.azan.askimam.user.domain.model.User.Type.Imam
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
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
class WebSecurityConfig(
    private val jwtFilter: JwtFilter,
    private val userService: UserService,
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http {
            authorizeRequests {
                authorize("/chats/public/*/*", permitAll)
                authorize("/chats/messages/*", permitAll)
                authorize("/chats/unanswered/*/*", hasAuthority(Imam.name))
                authorize("/chats/*/return-to-unanswered", hasAuthority(Imam.name))
                authorize("/messages/audio", hasAuthority(Imam.name))
                authorize("/auth/login", permitAll)
                authorize()
            }

            exceptionHandling {
                authenticationEntryPoint = AuthenticationEntryPoint { _, response, authException ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.message)
                }
            }

            addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }

            logout { disable() }
            csrf { disable() }
            cors { }
        }
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

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth?.userDetailsService(userService)
    }

    @Bean
    fun authManager(): AuthenticationManager = authenticationManagerBean()

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}