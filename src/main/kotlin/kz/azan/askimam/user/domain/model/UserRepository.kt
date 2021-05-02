package kz.azan.askimam.user.domain.model

import io.vavr.control.Either
import kz.azan.askimam.common.domain.Declination
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
interface UserRepository {
    fun findById(id: User.Id): Either<Declination, User>
    fun findByUsernameAndStatus(username: String?, status: Int): Either<Declination, User>
    fun saveTokens(user: User)
}
