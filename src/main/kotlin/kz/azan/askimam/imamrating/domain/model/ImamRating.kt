package kz.azan.askimam.imamrating.domain.model

import kz.azan.askimam.user.domain.model.User

data class ImamRating(
    val imamId: User.Id,
    private var rating: Int,
) {
    fun rating() = rating

    fun increment() {
        rating++
    }
}
