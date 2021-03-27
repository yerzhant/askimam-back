package kz.azan.askimam.user.domain.model

import io.vavr.control.Either
import kz.azan.askimam.common.domain.Declination

interface UserRepository {
    fun findById(id: User.Id): Either<Declination, User>
}