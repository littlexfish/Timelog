package dto

data class TeamList(
	val teamList: List<TeamItem>
)

data class TeamItem(
	val teamID: String,
	val teamName: String,
)
