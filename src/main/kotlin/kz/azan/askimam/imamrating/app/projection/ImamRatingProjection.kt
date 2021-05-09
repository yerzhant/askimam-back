package kz.azan.askimam.imamrating.app.projection

import kz.azan.askimam.user.domain.model.User

data class ImamRatingProjection(val imam: User, val rating: Int)
