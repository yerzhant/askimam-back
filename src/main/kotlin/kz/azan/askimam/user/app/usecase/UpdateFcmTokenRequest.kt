package kz.azan.askimam.user.app.usecase

import kz.azan.askimam.chat.domain.model.FcmToken

data class UpdateFcmTokenRequest(
    val oldToken: FcmToken,
    val newToken: FcmToken,
)
