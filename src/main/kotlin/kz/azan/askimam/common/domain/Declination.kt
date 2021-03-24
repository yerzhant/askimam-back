package kz.azan.askimam.common.domain

import kz.azan.askimam.common.type.NonBlankString

data class Declination(val reason: NonBlankString) {
    companion object {
        fun withReason(reason: String) = Declination(
            NonBlankString.of(reason)
        )

        fun from(throwable: Throwable) = Declination(
            NonBlankString.of(
                if (throwable.message.isNullOrEmpty()) "Unknown" else throwable.message!!
            )
        )
    }
}
