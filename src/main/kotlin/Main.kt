import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.application
import dto.ActivityTypeListJson
import dto.Profile
import dto.TeamList
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle
import org.jetbrains.jewel.window.styling.TitleBarStyle

const val version = "0.1.2"

enum class Page {
	History,
	Record,
	WeeklyRecord
}

@Composable
@Preview
fun App() {
	val profile = remember { mutableStateOf<Profile?>(null) }
	val pageState = remember { mutableStateOf(Page.History) }
	val recordState = remember { mutableStateOf<String?>(null) }
	val activitiesState = remember { mutableStateOf<ActivityTypeListJson?>(null) }
	val teamsState = remember { mutableStateOf<TeamList?>(null) }
	if(profile.value == null) {
		LoginPage(profile)
	}
	else {
		ensureActivities(profile.value!!, activitiesState)
		ensureTeam(profile.value!!, teamsState)
		when(pageState.value) {
			Page.History -> HistoryPage(profile, pageState, recordState)
			Page.Record -> RecordPage(profile.value!!, teamsState, activitiesState, pageState, recordState)
			Page.WeeklyRecord -> WeeklyRecordPage(profile.value!!, teamsState, activitiesState, pageState)
		}
	}
}

fun main() = application {
	IntUiTheme(
		theme = JewelTheme.lightThemeDefinition(),
		styling = ComponentStyling.default().decoratedWindow(
			DecoratedWindowStyle.light(),
			titleBarStyle = TitleBarStyle.light()
		),
		swingCompatMode = true) {
		val needShowSetup = remember { mutableStateOf(true) }
		LaunchedEffect(Unit) {
			if(configFileExists) {
				needShowSetup.value = !initConfig()
			}
		}
		if(needShowSetup.value) {
			Setup(needShowSetup)
		}
		else {
			val title = "Time Log(v$version)"
			DecoratedWindow(
				onCloseRequest = ::exitApplication,
				title = title) {
				TitleBar {
					Text(title)
				}
				App()
			}
		}
	}
}

@Composable
fun ApplicationScope.Setup(showSetup: MutableState<Boolean>) {
	DialogWindow(onCloseRequest = ::exitApplication, title = "Enter api url") {
		Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)) {
			val auth = remember { mutableStateOf("") }
			val timelog = remember { mutableStateOf("") }
			val defaultFocus = remember { FocusRequester() }
			LaunchedEffect(Unit) {
				defaultFocus.requestFocus()
			}
			TextField(auth.value, { auth.value = it }, Modifier.focusRequester(defaultFocus), placeholder = { Text("Auth server url") })
			TextField(timelog.value, { timelog.value = it }, placeholder = { Text("Time log server url") })
			val coroutine = rememberCoroutineScope()
			val error = remember { mutableStateOf<String?>(null) }
			val button = {
				coroutine.launch {
					if(auth.value.isBlank() || timelog.value.isBlank()) {
						error.value = "Url cannot be empty"
					}
					else {
						initConfig()
						config.authServer = auth.value
						config.timelogServer = timelog.value
						saveConfig()
						showSetup.value = false
					}
				}
				Unit
			}
			Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
				OutlinedButton(::exitApplication) {
					Text("Cancel")
				}
				DefaultButton(button) {
					Text("Setup")
				}
			}
		}
	}
}
