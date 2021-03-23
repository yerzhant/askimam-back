package kz.azan.askimam.common.domain

import kz.azan.askimam.common.type.NonBlankString

data class Declination(val reason: NonBlankString) {
    companion object {
        fun withReason(reason: String?) = Declination(
            NonBlankString.of(
                if (reason.isNullOrEmpty()) "Unknown" else reason
            )
        )
    }
}
