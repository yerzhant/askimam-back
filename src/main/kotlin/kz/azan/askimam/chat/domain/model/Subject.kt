package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.common.type.NonBlankString

data class Subject(val value: NonBlankString) {
    companion object {
        fun from(string: String) = Subject(NonBlankString.of(string))
    }
}
