package kz.azan.askimam.common.domain

import kz.azan.askimam.common.type.NotBlankString

data class Declination(val reason: NotBlankString) {
    companion object {
        fun withReason(reason: String) = Declination(NotBlankString.of(reason))
    }
}
