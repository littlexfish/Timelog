package dto

import CustomDateTime

data class HistoryResponse(
	val logItemList: List<LogItem>
)

data class LogItem(
	val id: String,
	val title: String,
	val teamName: String?,
	val activityTypeName: String,
	val startTime: CustomDateTime,
	val endTime: CustomDateTime,
)
