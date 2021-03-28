package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination

@UseCase
class UpdateChatSubject(private val chatRepository: ChatRepository) {

    operator fun invoke(id: Chat.Id, subject: Subject): Option<Declination> = chatRepository.findById(id).fold(
        { some(it) },
        { it.updateSubject(subject).orElse { chatRepository.update(it) } }
    )
}