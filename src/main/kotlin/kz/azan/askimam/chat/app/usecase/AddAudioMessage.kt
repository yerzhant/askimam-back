package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString

@UseCase
class AddAudioMessage(private val chatRepository: ChatRepository) {

    operator fun invoke(
        id: Chat.Id,
        audio: NonBlankString,
        duration: NonBlankString,
        fcmToken: FcmToken,
    ): Option<Declination> =
        chatRepository.findById(id).fold(
            { some(it) },
            { it.addAudioMessage(audio, duration, fcmToken).orElse { chatRepository.update(it) } }
        )
}