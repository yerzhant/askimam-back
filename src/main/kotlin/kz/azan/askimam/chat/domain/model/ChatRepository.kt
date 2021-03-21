package kz.azan.askimam.chat.domain.model

import io.vavr.control.Either
import io.vavr.control.Option
import kz.azan.askimam.common.domain.Declination

interface ChatRepository {
    fun findById(id: Chat.Id): Either<Declination, Chat>
    fun create(chat: Chat): Option<Declination>
    fun delete(chat: Chat): Option<Declination>
    fun update(chat: Chat): Option<Declination>
}
