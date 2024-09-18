package dto

import CustomDateTime

data class Record(
	val userID: String,
	val title: String,
	val activityTypeName: String,
	val activityUnitID: String,
	val startTime: CustomDateTime,
	val endTime: CustomDateTime,
	val description: String? = null,
)

/**
 * RecordLocal is use for store as template for record, it doesn't have userID
 */
data class RecordTemplate(
	val title: String,
	val activityTypeName: String,
	val activityUnitID: String,
	val startTime: CustomDateTime,
	val endTime: CustomDateTime,
	val description: String? = null,
)

fun Record.toRecordTemplate() = RecordTemplate(title, activityTypeName, activityUnitID, startTime.toOnlyTime(), endTime.toOnlyTime(), description)

fun RecordTemplate.toRecord(userID: String) = Record(userID, title, activityTypeName, activityUnitID, startTime, endTime, description)
