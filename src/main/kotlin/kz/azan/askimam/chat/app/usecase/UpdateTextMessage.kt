package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString

class UpdateTextMessage(private val chatRepository: ChatRepository) {

    operator fun invoke(chatId: Chat.Id, id: Message.Id, text: NonBlankString): Option<Declination> =
        chatRepository.findById(chatId).fold(
            { some(it) },
            { it.updateTextMessage(id, text).orElse { chatRepository.update(it) } }
        )
}