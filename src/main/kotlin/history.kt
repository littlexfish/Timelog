
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import dto.BoardData
import dto.HistoryResponse
import dto.LogItem
import dto.Profile
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.component.styling.ButtonColors
import org.jetbrains.jewel.ui.component.styling.ButtonStyle
import org.jetbrains.jewel.ui.theme.defaultButtonStyle
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryPage(profileState: MutableState<Profile?>, recordPageState: MutableState<Page>, record: MutableState<String?>) {
	val historyResponse = remember { mutableStateOf(HistoryResponse(emptyList())) }
	val profile = profileState.value ?: return
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
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				@Composable
				fun Title(name: String, tooltip: String, modifier: Modifier = Modifier.weight(1f), color: Color = Color.Black) {
					Tooltip({ Text(tooltip) }, modifier) {
						Text(name, color = color, fontWeight = FontWeight.Bold)
					}
				}
				Title("Action", "Log Action(Disabled)", Modifier.width(100.dp), Color.Gray)
				Title("Title", "Log Title")
				Title("Activity", "Activity Type")
				Title("Start", "Start Time")
				Title("End", "End Time")
				if(scrollEnabled) Spacer(Modifier.width(8.dp))
			}
			Divider(Orientation.Horizontal, Modifier.fillMaxWidth())
			Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				Column(Modifier.verticalScroll(scroll).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
					historyResponse.value.logItemList.sortedByDescending { it.startTime }.forEach {
						HistoryItem(it, recordPageState, record)
					}
				}
				// FIXME: scrollbar is not showing
				if(scrollEnabled) VerticalScrollbar(rememberScrollbarAdapter(scroll), Modifier.width(8.dp).fillMaxHeight())
			}
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				Tooltip({ Text("Add single log") }) {
					DefaultButton({
						record.value = null
						recordPageState.value = Page.Record
					}) {
						Text("Add Log")
					}
				}
				Tooltip({ Text("Add log weekly with template") }) {
					OutlinedButton({
						record.value = null
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
	}
}

@Composable
private fun HistoryItem(item: LogItem, recordPageState: MutableState<Page>, record: MutableState<String?>) {
	Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
		Row(Modifier.width(100.dp)) {
//			Icon(AllIconsKeys.Actions.Edit, "Edit", Modifier.size(16.dp).clickable {
//				record.value = item.id
//				recordPageState.value = Page.Record
//			})
//			Icon(AllIconsKeys.Diff.Remove, "Delete", Modifier.size(16.dp).clickable {
//				// TODO
//			})
		}
		Text(item.title, Modifier.weight(1f), color = Color.Black)
		Text(item.activityTypeName, Modifier.weight(1f), color = Color.Black)
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
			Text("Total Time: ${boardData.value!!.totalTime}", Modifier.align(Alignment.CenterHorizontally),
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
							Text("${(v.hour to v.minute).toCustomDateTime()}", Modifier.fillMaxWidth())
						}
					}
				}

			}
		}
	}
}
