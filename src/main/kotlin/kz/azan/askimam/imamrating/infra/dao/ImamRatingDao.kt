package kz.azan.askimam.imamrating.infra.dao

import kz.azan.askimam.imamrating.infra.model.ImamRatingRow
import org.springframework.data.repository.CrudRepository

interface ImamRatingDao : CrudRepository<ImamRatingRow, Long> {
    fun findAllByOrderByRating(): List<ImamRatingRow>
}
