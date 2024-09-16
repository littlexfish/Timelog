package dto

data class ActivityTypeListJson(
	val activityTypeList: List<ActivityTypeList>,
	val logList: Any?
)
data class ActivityTypeList(
	val id: String,
	val name: String,
	val enable: Boolean,
	val private: Boolean
)