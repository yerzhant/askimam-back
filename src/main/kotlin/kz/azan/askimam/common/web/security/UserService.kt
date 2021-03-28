package kz.azan.askimam.common.web.security

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
        println("User Service ************: $username")
        val user = userRepository.findByUsernameAndStatus(username, active).getOrElseThrow { reason ->
            UsernameNotFoundException("The user '$username' is not found: $reason")
        }

        return User(username, user.passwordHash.value, setOf(SimpleGrantedAuthority(user.type.toString())))
    }
}