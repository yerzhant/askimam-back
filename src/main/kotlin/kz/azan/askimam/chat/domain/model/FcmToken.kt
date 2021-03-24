package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.common.type.NonBlankString

data class FcmToken(val value: NonBlankString) {

    companion object {
        fun from(string: String) = FcmToken(NonBlankString.of(string))
    }

    fun string() = value.value
}
