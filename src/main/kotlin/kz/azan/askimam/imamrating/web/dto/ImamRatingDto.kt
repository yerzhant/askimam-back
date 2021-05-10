package kz.azan.askimam.imamrating.web.dto

import kz.azan.askimam.imamrating.app.projection.ImamRatingProjection

data class ImamRatingDto(
    val name: String,
    val rating: Int,
) {
    companion object {
        fun from(projection: ImamRatingProjection) = ImamRatingDto(
            projection.imam.name.value,
            projection.rating,
        )
    }
}
