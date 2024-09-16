package dto

import CustomDateTime

data class HistoryRequest(
	val userID: String,
	val startDate: CustomDateTime,
	val endDate: CustomDateTime
)
