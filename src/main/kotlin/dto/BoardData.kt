package dto

import CustomDateTime

data class BoardData(
	val totalTime: CustomDateTime,
	val dataMap: Map<String, BoardTime>,
)

data class BoardTime(
	val hour: Int,
	val minute: Int,
	/**
	 * The total minute of the time, a.k.a. `hour * 60 + minute`
	 */
	val timeLength: Int,
)
