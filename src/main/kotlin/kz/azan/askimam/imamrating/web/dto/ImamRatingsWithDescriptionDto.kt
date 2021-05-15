package kz.azan.askimam.imamrating.web.dto

data class ImamRatingsWithDescriptionDto(
    val description: String,
    val ratings: List<ImamRatingDto>,
)
