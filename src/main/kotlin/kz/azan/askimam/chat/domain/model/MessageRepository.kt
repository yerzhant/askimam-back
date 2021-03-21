package kz.azan.askimam.chat.domain.model

import io.vavr.control.Either
import io.vavr.control.Option
import kz.azan.askimam.common.domain.Declination

interface MessageRepository {
    fun add(message: Message): Option<Declination>
    fun delete(message: Message): Option<Declination>
    fun update(message: Message): Option<Declination>
    fun generateId(): Either<Declination, Message.Id>
}
