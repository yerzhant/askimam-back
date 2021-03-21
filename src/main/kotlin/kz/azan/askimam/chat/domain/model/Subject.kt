package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.common.type.NotBlankString

data class Subject(val value: NotBlankString) {
    companion object {
        fun from(string: String) = Subject(NotBlankString.of(string))
    }
}
