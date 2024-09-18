
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import component.ActivitySelect
import component.TeamSelect
import dto.BoardData
import dto.HistoryResponse
import dto.LogItem
import dto.Profile
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.component.VerticalScrollbar
import org.jetbrains.jewel.ui.component.styling.ButtonColors
import org.jetbrains.jewel.ui.component.styling.ButtonStyle
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.defaultButtonStyle
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryPage(profileState: MutableState<Profile?>, recordPageState: MutableState<Page>) {
	val historyResponse = remember { mutableStateOf(HistoryResponse(emptyList())) }
	val profile = profileState.value ?: return
	val editRecord = remember { mutableStateOf<LogItem?>(null) }
	val deleteRecord = remember { mutableStateOf<LogItem?>(null) }
	Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
		val now = CustomDateTime.now().toOnlyDate()
		val beginDate = remember { mutableStateOf(now - 7.days) }
		val endDate = remember { mutableStateOf(now) }
		val reRequest = remember { mutableStateOf(false) }
		Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
			DatePicker(beginDate.value.toDateTriple(), { beginDate.value = it.toCustomDateTime() }, Modifier.weight(1f),
				yearMin = (now.year ?: 2000) - 10, yearMax = (now.year ?: 2000) + 1)
			Text("to")
			DatePicker(endDate.value.toDateTriple(), { endDate.value = it.toCustomDateTime() }, Modifier.weight(1f),
				yearMin = (now.year ?: 2000) - 10, yearMax = (now.year ?: 2000) + 1)
			DefaultButton({
				reRequest.value = !reRequest.value
			}) {
				Text("Search")
			}
			val boardDialog = remember { mutableStateOf(false) }
			OutlinedButton({
				boardDialog.value = true
			}) {
				Text("Board")
			}
			if(boardDialog.value) {
				val dialogState = rememberDialogState(size = DpSize(800.dp, 600.dp))
				DialogWindow(onCloseRequest = { boardDialog.value = false }, title = "Board", state = dialogState) {
					Board(beginDate.value, endDate.value, profile)
				}
			}
		}
		LaunchedEffect(reRequest.value) {
			getHistory(profile, beginDate.value, endDate.value)?.let {
				historyResponse.value = it
			}
		}
		Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			val scroll = rememberScrollState()
			val scrollEnabled by derivedStateOf { scroll.maxValue > 0 }
			Row {
				@Composable
				fun Title(name: String, tooltip: String, modifier: Modifier = Modifier.weight(1f), color: Color = Color.Black) {
					Tooltip({ Text(tooltip) }, modifier) {
						Text(name, color = color, fontWeight = FontWeight.Bold)
					}
				}
				Title("Action", "Log Action", Modifier.width(100.dp))
				Title("Title", "Log Title")
				Title("Team(Activity)", "Team & Activity Type")
				Title("Start", "Start Time")
				Title("End", "End Time")
				if(scrollEnabled) Spacer(Modifier.width(8.dp))
			}
			Divider(Orientation.Horizontal, Modifier.fillMaxWidth())
			Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				Column(Modifier.verticalScroll(scroll).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
					historyResponse.value.logItemList.sortedByDescending { it.startTime }.forEach {
						HistoryItem(it, editRecord, deleteRecord)
					}
				}
				// FIXME: scrollbar is not showing
				if(scrollEnabled) VerticalScrollbar(rememberScrollbarAdapter(scroll), Modifier.width(8.dp).fillMaxHeight())
			}
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				Tooltip({ Text("Add single log") }) {
					DefaultButton({
						recordPageState.value = Page.Record
					}) {
						Text("Add Log")
					}
				}
				Tooltip({ Text("Add log weekly with template") }) {
					OutlinedButton({
						recordPageState.value = Page.WeeklyRecord
					}) {
						Text("Weekly Add")
					}
				}
				Spacer(Modifier.weight(1f))
				DefaultButton({
					removeBearer()
					profileState.value = null
				}, style = ButtonStyle(
					ButtonColors(
						background = SolidColor(JewelTheme.globalColors.text.error),
						backgroundDisabled = JewelTheme.defaultButtonStyle.colors.backgroundDisabled,
						backgroundFocused = JewelTheme.defaultButtonStyle.colors.backgroundFocused,
						backgroundPressed = JewelTheme.defaultButtonStyle.colors.backgroundPressed,
						backgroundHovered = JewelTheme.defaultButtonStyle.colors.backgroundHovered,
						content = JewelTheme.defaultButtonStyle.colors.content,
						contentDisabled = JewelTheme.defaultButtonStyle.colors.contentDisabled,
						contentFocused = JewelTheme.defaultButtonStyle.colors.contentFocused,
						contentPressed = JewelTheme.defaultButtonStyle.colors.contentPressed,
						contentHovered = JewelTheme.defaultButtonStyle.colors.contentHovered,
						border = SolidColor(JewelTheme.globalColors.text.error),
						borderDisabled = JewelTheme.defaultButtonStyle.colors.borderDisabled,
						borderFocused = JewelTheme.defaultButtonStyle.colors.borderFocused,
						borderPressed = JewelTheme.defaultButtonStyle.colors.borderPressed,
						borderHovered = JewelTheme.defaultButtonStyle.colors.borderHovered,
					),
					JewelTheme.defaultButtonStyle.metrics,
					JewelTheme.defaultButtonStyle.focusOutlineAlignment
				)) {
					Text("Logout(${profile.displayName})")
				}
			}
		}
		EditDialog(editRecord, profile, reRequest)
		ConfirmAndDelete(deleteRecord, profile, reRequest)
	}
}

@Composable
private fun HistoryItem(item: LogItem, editRecord: MutableState<LogItem?>, deleteRecord: MutableState<LogItem?>) {
	Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
		Row(Modifier.width(100.dp)) {
			component.IconButton(AllIconsKeys.Actions.Edit, "Edit", modifier = Modifier.size(24.dp)) {
				editRecord.value = item
			}
			component.IconButton(AllIconsKeys.Diff.Remove, "Delete", modifier = Modifier.size(24.dp)) {
				deleteRecord.value = item
			}
		}
		Text(item.title, Modifier.weight(1f), color = Color.Black)
		Text("${item.teamName ?: "Personal"}(${item.activityTypeName ?: ""})", Modifier.weight(1f), color = Color.Black)
		Text(item.startTime.toString(), Modifier.weight(1f), color = Color.Black)
		Text(item.endTime.toString(), Modifier.weight(1f), color = Color.Black)
	}
}

@Composable
private fun Board(begin: CustomDateTime, end: CustomDateTime, profile: Profile) {
	val error = remember { mutableStateOf(false) }
	val boardData = remember { mutableStateOf<BoardData?>(null) }
	LaunchedEffect(Unit) {
		val data = getBoard(begin, end, profile)
		if(data == null) error.value = true
		else boardData.value = data
	}
	if(boardData.value == null) {
		if(error.value) Text("Error on loading board data.")
		else Text("Loading...")
	}
	else {
		Column(Modifier.padding(8.dp)) {
			Text("From ${begin.toOnlyDate()} to ${end.toOnlyDate()}", Modifier.align(Alignment.CenterHorizontally))
			val tt = boardData.value!!.totalTime
			Text("Total Time: ${tt.hour.toString().padStart(2, '0')}:${tt.minute.toString().padStart(2, '0')}",
				Modifier.align(Alignment.CenterHorizontally),
				fontWeight = FontWeight.Bold, fontSize = 20.sp)
			Divider(Orientation.Horizontal)
			Column(Modifier.verticalScroll(rememberScrollState()).padding(8.dp)) {
				Row {
					val keys = boardData.value!!.dataMap.keys.toList()
					Column(Modifier.width(IntrinsicSize.Max)) {
						keys.forEach {
							Text(it, Modifier.fillMaxWidth())
						}
					}
					Column {
						repeat(keys.size) {
							Text(" -> ")
						}
					}
					Column {
						keys.forEach {
							val v = boardData.value!!.dataMap[it]!!
							Text("${v.hour.toString().padStart(2, '0')}:${v.minute.toString().padStart(2, '0')}", Modifier.fillMaxWidth())
						}
					}
				}

			}
		}
	}
}

@Composable
private fun EditDialog(recordState: MutableState<LogItem?>, profile: Profile, reRequest: MutableState<Boolean>) {
	if(recordState.value != null) {
		DialogWindow(onCloseRequest = { recordState.value = null }, title = "Edit Record", state = rememberDialogState(size = DpSize(550.dp, 250.dp))) {
			val editRecord = remember { mutableStateOf(recordState.value!!) }
			@Composable
			fun row(name: String, content: @Composable () -> Unit) = Row(verticalAlignment = Alignment.CenterVertically) {
				Text("$name ")
				content()
			}
			Column(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
				row("Title") {
					TextField(editRecord.value.title, { editRecord.value = editRecord.value.copy(title = it) },
						placeholder = { Text("Title") })
				}
				row("Team") {
					TeamSelect(current = Lookup.findTeamId(editRecord.value.teamName)) {
						editRecord.value = editRecord.value.copy(teamName = Lookup.findTeamName(it))
					}
				}
				row("Activity") {
					ActivitySelect(current = editRecord.value.activityTypeName) {
						editRecord.value = editRecord.value.copy(activityTypeName = it)
					}
				}
				row("Begin") {
					val start = editRecord.value.startTime
					HDateTimePicker(editRecord.value.startTime, {
						editRecord.value = editRecord.value.copy(startTime = it)
					}, minuteStep = 10, yearMin = (start.year ?: 2000) - 10, yearMax = (start.year ?: 2000) + 10)
				}
				row("End") {
					val end = editRecord.value.endTime
					HDateTimePicker(editRecord.value.endTime, {
						editRecord.value = editRecord.value.copy(endTime = it)
					}, minuteStep = 10, yearMin = (end.year ?: 2000) - 10, yearMax = (end.year ?: 2000) + 10)
				}
				Spacer(Modifier.weight(1f))
				val coroutine = rememberCoroutineScope()
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
					OutlinedButton({
						recordState.value = null
					}) {
						Text("Cancel")
					}
					OutlinedButton({
						editRecord.value = recordState.value!!
					}) {
						Text("Reset")
					}
					DefaultButton({
						coroutine.launch {
							editRecord(editRecord.value, profile)
							reRequest.value = !reRequest.value
							recordState.value = null
						}
					}) {
						Text("Save")
					}
				}
			}
		}
	}
}

@Composable
private fun ConfirmAndDelete(deleteRecord: MutableState<LogItem?>, profile: Profile, reRequest: MutableState<Boolean>) {
	if(deleteRecord.value != null) {
		DialogWindow(onCloseRequest = { deleteRecord.value = null }, title = "Confirm to Delete Record",
			state = rememberDialogState(size = DpSize(300.dp, 150.dp))) {
			Column(Modifier.fillMaxSize().padding(8.dp)) {
				Box(Modifier.weight(1f)) {
					Text("Are you sure to delete this record?", Modifier.align(Alignment.Center), textAlign = TextAlign.Center)
				}
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
					val coroutine = rememberCoroutineScope()
					OutlinedButton({
						deleteRecord.value = null
					}) {
						Text("Cancel")
					}
					DefaultButton({
						coroutine.launch {
							deleteRecord(deleteRecord.value!!, profile)
							reRequest.value = !reRequest.value
							deleteRecord.value = null
						}
					}) {
						Text("Delete")
					}
				}
			}
		}
	}
}
