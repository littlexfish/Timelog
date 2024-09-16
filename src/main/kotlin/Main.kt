import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dto.ActivityTypeListJson
import dto.Profile
import dto.TeamList
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle
import org.jetbrains.jewel.window.styling.TitleBarStyle

const val version = "0.1.1"

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
