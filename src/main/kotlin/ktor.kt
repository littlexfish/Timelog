import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

const val authUrl = "http://140.124.181.95:30100"
const val timeLogUrl = "http://140.124.181.95:30200"

val client = HttpClient(OkHttp) {
	engine {
		config {
			followRedirects(true)
		}
	}
	install(HttpCache) {}
	install(ContentNegotiation) {
		jackson(streamRequestBody = false) {
			registerKotlinModule()
		}
	}
}

@OptIn(ExperimentalEncodingApi::class)
suspend fun login(username: String, password: String): String? {
	val basic = Base64.encode("$username:$password".toByteArray())
	val res = client.post("$authUrl/auth/login") {
		header("Authorization", "Basic $basic")
	}
	return if(res.status.isSuccess()) res.body()
	else {
		System.err.println("Error: ${res.status}\ndata: $username:$password\nresponse: ${res.body<String>()}")
		null
	}
}

suspend fun getProfile(token: String): Profile? {
	val res = client.get("$authUrl/profile") {
		header("Authorization", "Bearer $token")
	}
	return if(res.status.isSuccess()) res.body()
	else {
		System.err.println("Error: ${res.status}\ndata: $token\nresponse: ${res.body<String>()}")
		null
	}
}

suspend fun getTeams(username: String): TeamList? {
	val res = client.post("$timeLogUrl/api/belong") {
		contentType(ContentType.Application.Json)
		setBody(mapOf("username" to username))
	}
	return if(res.status.isSuccess()) res.body()
	else {
		System.err.println("Error: ${res.status}\ndata: $username\nresponse: ${res.body<String>()}")
		null
	}
}

@Composable
fun ensureTeam(profile: Profile, teamList: MutableState<TeamList?>) {
	val coroutine = rememberCoroutineScope()
	if(teamList.value == null) {
		SideEffect {
			coroutine.launch {
				teamList.value = getTeams(profile.username)
				if(teamList.value == null) {
					teamList.value = TeamList(emptyList())
				}
			}
		}
	}
}

suspend fun getActivities(userId: String): ActivityTypeListJson? {
	val res = client.post("$timeLogUrl/api/login") {
		contentType(ContentType.Application.Json)
		setBody(mapOf("userID" to userId))
	}
	return if(res.status.isSuccess()) res.body()
	else {
		System.err.println("Error: ${res.status}\ndata: $userId\nresponse: ${res.body<String>()}")
		null
	}
}

@Composable
fun ensureActivities(profile: Profile, activities: MutableState<ActivityTypeListJson?>) {
	val coroutine = rememberCoroutineScope()
	if(activities.value == null) {
		SideEffect {
			coroutine.launch {
				activities.value = getActivities(profile.userId)
				if(activities.value == null) {
					activities.value = ActivityTypeListJson(emptyList(), null)
				}
			}
		}
	}
}

suspend fun getHistory(profile: Profile, startDate: CustomDateTime, endDate: CustomDateTime): HistoryResponse? {
	val req = HistoryRequest(profile.userId, startDate.toOnlyDate(), endDate.toOnlyDate())
	val res = client.post("$timeLogUrl/api/log/history") {
		contentType(ContentType.Application.Json)
		setBody(req)
	}
	return if(res.status.isSuccess()) res.body()
	else {
		System.err.println("Error: ${res.status}\ndata: $req\nresponse: ${res.body<String>()}")
		null
	}
}

suspend fun postRecord(record: Record): RecordResponse? {
	val res = client.post("$timeLogUrl/api/log/record") {
		contentType(ContentType.Application.Json)
		setBody(record)
	}
	return if(res.status.isSuccess()) res.body()
	else {
		System.err.println("Error: ${res.status}\ndata: $record\nresponse: ${res.body<String>()}")
		null
	}
}

suspend fun getBoard(begin: CustomDateTime, end: CustomDateTime, profile: Profile): BoardData? {
	val res = client.post("$timeLogUrl/api/dash-board/spent-time") {
		contentType(ContentType.Application.Json)
		setBody(mapOf(
			"userID" to profile.userId,
			"startDate" to begin.toOnlyDate(),
			"endDate" to end.toOnlyDate(),
		))
	}
	return if(res.status.isSuccess()) res.body()
	else {
		System.err.println("Error: ${res.status}\ndata: ${begin.toOnlyDate()} ~ ${end.toOnlyDate()}\nresponse: ${res.body<String>()}")
		null
	}
}
