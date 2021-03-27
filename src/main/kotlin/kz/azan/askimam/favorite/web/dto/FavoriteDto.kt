package kz.azan.askimam.favorite.web.dto

import kz.azan.askimam.favorite.app.projection.FavoriteProjection

data class FavoriteDto(
    val id: Long,
    val chatId: Long,
    val subject: String,
) {
    companion object {
        fun from(projection: FavoriteProjection) = FavoriteDto(
            projection.id.value,
            projection.chatId.value,
            projection.subject.string(),
        )
    }
}