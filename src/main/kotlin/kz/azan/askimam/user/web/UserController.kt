package kz.azan.askimam.user.web

import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.common.web.meta.RestApi
import kz.azan.askimam.user.app.usecase.UpdateFcmToken
import kz.azan.askimam.user.web.dto.UpdateFcmTokenDto
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody

@RestApi("user")
class UserController(
    private val updateFcmToken: UpdateFcmToken,
) {
    @PatchMapping("update-token")
    fun updateToken(@RequestBody dto: UpdateFcmTokenDto): ResponseDto = updateFcmToken
        .process(FcmToken.from(dto.old), FcmToken.from(dto.new)).fold(
            { ResponseDto.ok() },
            { ResponseDto.error(it) }
        )
}