package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.repo.ChatRepository
import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination

@UseCase
class DeleteMessage(private val chatRepository: ChatRepository) {

    operator fun invoke(chatId: Chat.Id, id: Message.Id): Option<Declination> =
        chatRepository.findById(chatId).fold(
            { some(it) },
            { it.deleteMessage(id).orElse { chatRepository.update(it) } }
        )
}