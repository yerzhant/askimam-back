package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.common.type.NotBlankString

data class Message(
    val text: NotBlankString,
)
