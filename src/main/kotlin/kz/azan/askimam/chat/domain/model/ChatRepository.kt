package kz.azan.askimam.chat.domain.model

import io.vavr.control.Either
import kz.azan.askimam.common.domain.Declination

interface ChatRepository {
    fun findById(id: Chat.Id): Either<Declination, Chat>
}
