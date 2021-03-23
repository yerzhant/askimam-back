package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString

class AddTextMessage(private val chatRepository: ChatRepository) {

    operator fun invoke(id: Chat.Id, text: NonBlankString): Option<Declination> =
        chatRepository.findById(id).fold(
            { some(it) },
            { it.addTextMessage(text).orElse { chatRepository.update(it) } }
        )
}