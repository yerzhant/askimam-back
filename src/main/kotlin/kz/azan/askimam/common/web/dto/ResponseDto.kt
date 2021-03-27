package kz.azan.askimam.common.web.dto

import kz.azan.askimam.common.web.dto.ResponseDto.Status.Error
import kz.azan.askimam.common.web.dto.ResponseDto.Status.Ok

data class ResponseDto(
    val status: Status,
    val data: Any? = null,
    val error: String? = null,
) {
    companion object {
        fun ok(data: Any) = ResponseDto(Ok, data)
        fun error(description: String) = ResponseDto(Error, error = description)
    }

    enum class Status { Ok, Error }
}
