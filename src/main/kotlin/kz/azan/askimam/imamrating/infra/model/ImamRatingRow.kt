package kz.azan.askimam.imamrating.infra.model

import jakarta.validation.constraints.PositiveOrZero
import kz.azan.askimam.imamrating.domain.model.ImamRating
import kz.azan.askimam.user.domain.model.User
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("imam_ratings")
data class ImamRatingRow(
    @Id
    val imamId: Long,

    @get:PositiveOrZero
    val rating: Int,
) {
    fun toDomain() = ImamRating(User.Id(imamId), rating)

    companion object {
        fun from(rating: ImamRating) = ImamRatingRow(rating.imamId.value, rating.rating())
    }
}
