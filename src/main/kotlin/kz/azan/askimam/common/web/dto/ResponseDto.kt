package kz.azan.askimam.common.web.dto

import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.web.dto.ResponseDto.Status.Error
import kz.azan.askimam.common.web.dto.ResponseDto.Status.Ok

data class ResponseDto(
    val status: Status,
    val data: Any? = null,
    val error: String? = null,
) {
    companion object {
        fun ok() = ResponseDto(Ok)
        fun ok(data: Any) = ResponseDto(Ok, data)
        fun error(declination: Declination) = ResponseDto(Error, error = declination.reason.value)
    }

    enum class Status { Ok, Error }
}
