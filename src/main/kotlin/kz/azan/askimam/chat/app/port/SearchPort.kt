package kz.azan.askimam.chat.app.port

import io.vavr.control.Either
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.common.domain.Declination

interface SearchPort {
    fun find(phrase: String): Either<Declination, List<Chat.Id>>
}
