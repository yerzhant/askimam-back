package kz.azan.askimam.chat.web

import kz.azan.askimam.chat.app.usecase.AddAudioMessage
import kz.azan.askimam.chat.app.usecase.AddTextMessage
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.chat.web.dto.AddAudioMessageDto
import kz.azan.askimam.chat.web.dto.AddTextMessageDto
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.common.web.meta.RestApi
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@RestApi("messages")
class MessageController(
    private val addTextMessage: AddTextMessage,
    private val addAudioMessage: AddAudioMessage,
) {

    @PostMapping
    fun addText(@RequestBody dto: AddTextMessageDto): ResponseDto =
        addTextMessage(Chat.Id(dto.chatId), NonBlankString.of(dto.text), FcmToken.from(dto.fcmToken)).fold(
            { ResponseDto.ok() },
            { ResponseDto.error(it) }
        )

    @PostMapping("audio")
    fun addAudio(@RequestBody dto: AddAudioMessageDto): ResponseDto =
        addAudioMessage(Chat.Id(dto.chatId), NonBlankString.of(dto.audio), FcmToken.from(dto.fcmToken)).fold(
            { ResponseDto.ok() },
            { ResponseDto.error(it) }
        )
}