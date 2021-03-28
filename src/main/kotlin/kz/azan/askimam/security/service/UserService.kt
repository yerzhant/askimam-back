package kz.azan.askimam.security.service

import kz.azan.askimam.user.domain.model.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) : UserDetailsService {

    private val active = 1

    override fun loadUserByUsername(username: String?): UserDetails {
        if (username.isNullOrBlank()) throw UsernameNotFoundException("The user name may not be blank")

        val user = find(username)
        return User(username, user.passwordHash.value, setOf(SimpleGrantedAuthority(user.type.toString())))
    }

    fun find(username: String): kz.azan.askimam.user.domain.model.User =
        userRepository.findByUsernameAndStatus(username, active).getOrElseThrow { reason ->
            UsernameNotFoundException("The user '$username' is not found: $reason")
        }
}