package dto

data class Profile(
	val userId: String, // should be UUID
	val username: String,
	val email: String,
	val displayName: String,
)
