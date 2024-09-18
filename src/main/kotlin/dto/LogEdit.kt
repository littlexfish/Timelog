package dto

import CustomDateTime

data class LogEdit(
	val userID: String,
	val logID: String,
	val title: String,
	val activityTypeName: String?,
	val activityUnitID: String?,
	val startTime: CustomDateTime,
	val endTime: CustomDateTime,
	val description: String? = null,
)
