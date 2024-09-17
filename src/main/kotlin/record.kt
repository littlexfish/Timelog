import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dto.*
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher
import kotlinx.coroutines.launch
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import java.io.File
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Composable
fun RecordPage(profile: Profile, teamList: MutableState<TeamList?>, activities: MutableState<ActivityTypeListJson?>,
               recordPageState: MutableState<Page>, record: MutableState<String?>) {
	Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
		val default = getDefaultRecord(profile, activities.value, teamList.value)
		val title = remember { mutableStateOf(default.title) }
		val team = remember { mutableStateOf(default.activityUnitID) }
		val project = remember { mutableStateOf(default.activityTypeName) }
		val beginTime = remember { mutableStateOf(default.startTime) }
		val endTime = remember { mutableStateOf(default.endTime) }
		val interval = remember { mutableStateOf(endTime.value - beginTime.value) }
		val hasBreak = remember { mutableStateOf(false) }
		val breakFromTime = remember { mutableStateOf(CustomDateTime(hour = 12, minute = 0)) }
		val breakTime = remember { mutableStateOf(1.hours) }
		TextField(title.value, { title.value = it }, placeholder = { Text("Title") })
		Dropdown(menuContent = {
			teamList.value?.teamList?.forEach {
				selectableItem(it.teamID == team.value, onClick = {
					team.value = it.teamID
				}) {
					Text(it.teamName)
				}
			}
		}) {
			Text(teamList.value?.teamList?.find { it.teamID == team.value }?.teamName ?: "")
		}
		Dropdown(menuContent = {
			activities.value?.activityTypeList?.forEach {
				selectableItem(it.name == project.value, onClick = {
					project.value = it.name
				}) {
					Text(it.name)
				}
			}
		}) {
			Text(project.value)
		}
		Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			Text("Begin")
			HDateTimePicker(beginTime.value,
				{
					beginTime.value = it
					endTime.value = it + interval.value
				}, minuteStep = 10, yearMin = (beginTime.value.year ?: 2000) - 10, yearMax = (beginTime.value.year ?: 2000) + 10)
			Spacer(Modifier.width(8.dp))
			Text("Interval")
			TimePicker(interval.value.toTimePair(),
				{
					interval.value = it.toDuration()
					endTime.value = beginTime.value + interval.value
				}, minuteStep = 30)
		}
		Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				Checkbox(hasBreak.value, { hasBreak.value = it })
				Text("Has Break")
			}
			if(hasBreak.value) {
				Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
					Text("Break from")
					TimePicker(breakFromTime.value.toTimePair(), { breakFromTime.value = it.toCustomDateTime() }, minuteStep = 10)
				}
				Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
					Text("Break Time")
					TimePicker(breakTime.value.toTimePair(), { breakTime.value = it.toDuration() }, minuteStep = 30)
				}
			}
		}
		Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			Text("End")
			HDateTimePicker(endTime.value, {
				endTime.value = it
				interval.value = it - beginTime.value
			}, minuteStep = 10, yearMin = (endTime.value.year ?: 2000) - 10, yearMax = (endTime.value.year ?: 2000) + 10)
		}
		Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			val isSaving = remember { mutableStateOf(false) }
			val coroutine = rememberCoroutineScope()
			val errorPopup = remember { mutableStateOf("") }
			if(errorPopup.value.isNotBlank()) {
				DialogWindow(onCloseRequest = {
					errorPopup.value = ""
				}) {
					Text(errorPopup.value)
				}
			}
			OutlinedButton({
				recordPageState.value = Page.History
			}) {
				Text("back")
			}
			DefaultButton({
				isSaving.value = true
				val timePairList = mutableListOf<Pair<CustomDateTime, CustomDateTime>>()
				if(hasBreak.value) {
					val middle = beginTime.value.copy(hour = breakFromTime.value.hour, minute = breakFromTime.value.minute)
					timePairList.add(Pair(beginTime.value, middle))
					timePairList.add(Pair(middle + breakTime.value, endTime.value))
				}
				else {
					timePairList.add(Pair(beginTime.value, endTime.value))
				}
				coroutine.launch {
					val retList = postMultiRecord(profile.userId, title.value, project.value, team.value, timePairList)
					if(retList.any { it == null }) {
						errorPopup.value = "Some or All failed to save record"
					}
					else {
						errorPopup.value = "Successful"
					}
					isSaving.value = false
				}
			}, enabled = !isSaving.value) {
				Text("Save")
			}
		}
	}
}

fun getDefaultRecord(profile: Profile, activities: ActivityTypeListJson?, teamList: TeamList?): Record {
	val now = CustomDateTime.now().toResetMinute()
	val actName = activities?.activityTypeList?.let {
		(it.find { act -> act.name == "LabProject" } ?: it.getOrNull(0))?.name
	} ?: ""
	val actTeam = teamList?.teamList?.let {
		(it.find { t -> t.teamName.startsWith("Cross") } ?: it.getOrNull(0))?.teamID
	} ?: ""
	return Record(profile.userId, "MOB", actName, actTeam,
		now, now + 1.hours)
}

@Composable
fun WeeklyRecordPage(profile: Profile, teamList: MutableState<TeamList?>, activities: MutableState<ActivityTypeListJson?>,
                   recordPageState: MutableState<Page>) {
	ensureActivities(profile, activities)
	ensureTeam(profile, teamList)
	Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
		val weekDaySelected = remember { mutableStateOf(0) }
		fun td(v: Int, t: String) = TabData.Default(
			weekDaySelected.value == v,
			{ Text(t, Modifier.padding(horizontal = 8.dp)) },
			false
		) { weekDaySelected.value = v }
		val weekTabData = listOf(td(0, "一"), td(1, "二"), td(2, "三"),
			td(3, "四"), td(4, "五"), td(5, "六"), td(6, "日"))
		val monList = remember { mutableStateListOf<Record>() }
		val tueList = remember { mutableStateListOf<Record>() }
		val wedList = remember { mutableStateListOf<Record>() }
		val thuList = remember { mutableStateListOf<Record>() }
		val friList = remember { mutableStateListOf<Record>() }
		val satList = remember { mutableStateListOf<Record>() }
		val sunList = remember { mutableStateListOf<Record>() }
		val weekTimeLog = remember {
			listOf(
				monList, tueList, wedList,
				thuList, friList, satList, sunList
			)
		}
		val coroutine = rememberCoroutineScope()
		val scroll = rememberScrollState()
		val addAction = {
			val fromLastOrDefault = weekTimeLog[weekDaySelected.value].lastOrNull()?.let {
				val start = it.endTime + 1.hours
				val end = start + 1.hours
				it.copy(startTime = start, endTime = end)
			} ?: getDefaultRecord(profile, activities.value, teamList.value)
			weekTimeLog[weekDaySelected.value].add(fromLastOrDefault)
			coroutine.launch {
				scroll.animateScrollTo(scroll.maxValue)
			}
			Unit
		}
		TabStrip(weekTabData)
		val popup = remember { mutableStateOf("") }
		Column(Modifier.weight(1f)) {
			val timeLog = weekTimeLog[weekDaySelected.value]
			if (timeLog.isEmpty()) {
				Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
					Row(verticalAlignment = Alignment.CenterVertically) {
						Text("Click ")
						DefaultButton(addAction) {
							Text("Add")
						}
						Text(" to add record")
					}
				}
			}
			else {
				Column(Modifier.verticalScroll(scroll).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
					timeLog.forEachIndexed { index, it ->
						EditWeekRecord(it, { record ->
							timeLog[index] = record
						}, teamList, activities, {
							timeLog.removeAt(index)
						}) {
							popup.value = it
						}
					}
				}
			}
			DefaultButton(addAction) {
				Text("Add")
			}
		}
		Divider(Orientation.Horizontal)
		val now = CustomDateTime.now().toOnlyDate()
		val fromDate = remember { mutableStateOf(now - 7.days) }
		val toDate = remember { mutableStateOf(now) }
		Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
			Text("From")
			DatePicker(fromDate.value.toDateTriple(), { fromDate.value = it.toCustomDateTime() }, yearMin = (now.year ?: 2000) - 10, yearMax = (now.year ?: 2000) + 10)
			Text("To")
			DatePicker(toDate.value.toDateTriple(), { toDate.value = it.toCustomDateTime() }, yearMin = (now.year ?: 2000) - 10, yearMax = (now.year ?: 2000) + 10)
		}
		if (popup.value.isNotBlank()) {
			DialogWindow(onCloseRequest = {
				popup.value = ""
			}) {
				Column(Modifier.verticalScroll(rememberScrollState())) {
					Text(popup.value)
				}
			}
		}
		Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			OutlinedButton({
				recordPageState.value = Page.History
			}) {
				Text("Back")
			}
			DefaultButton({
				val all = mutableListOf<Record>()
				var currentDay = fromDate.value
				while (currentDay < toDate.value) {
					val dayWeek = currentDay.getDayOfWeek() - 1
					all.addAll(weekTimeLog[dayWeek].map {
						it.copy(
							startTime = it.startTime.copy(
								year = currentDay.year,
								month = currentDay.month,
								day = currentDay.day
							),
							endTime = it.endTime.copy(
								year = currentDay.year,
								month = currentDay.month,
								day = currentDay.day
							)
						)
					})
					currentDay += 1.days
				}
				coroutine.launch {
					val retList = all.map { postRecord(it) }
					if (retList.any { it == null }) {
						popup.value = "Some or All failed to save record"
					}
					else {
						popup.value = "Successful"
					}
				}
			}) {
				Text("Save")
			}
			val saver = rememberFileSaverLauncher { /* do nothing */ }
			val picker = rememberFilePickerLauncher {
				val file = it?.file ?: return@rememberFilePickerLauncher
				importWeekRecord(weekTimeLog, file, profile.userId)
			}
			val importNotice = remember { mutableStateOf(false) }
			Spacer(Modifier.weight(1f))
			OutlinedButton({
				val defaultDir = prepareDefaultTemplateDir()
				val list = weekTimeLog.map { l -> l.map(Record::toRecordTemplate) }
				val buffer = jacksonObjectMapper().writeValueAsBytes(list)
				saver.launch(buffer, "template", "json", defaultDir.absolutePath)
			}) {
				Text("Export")
			}
			OutlinedButton({
				importNotice.value = true
			}) {
				Text("Import")
			}
			if(importNotice.value) {
				DialogWindow(onCloseRequest = {
					importNotice.value = false
				}) {
					Column(Modifier.fillMaxSize().width(IntrinsicSize.Max).padding(8.dp)) {
						Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
							Text("Import will overwrite current data, continue?")
						}
						Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
							OutlinedButton({
								importNotice.value = false
							}) {
								Text("Cancel")
							}
							DefaultButton({
								importNotice.value = false
								picker.launch()
							}) {
								Text("Continue")
							}
						}
					}
				}
			}
		}
	}
}

private fun prepareDefaultTemplateDir(): File {
	val dir = File("template")
	if(!dir.exists()) {
		dir.mkdirs()
	}
	return dir
}

private fun importWeekRecord(records: List<SnapshotStateList<Record>>, file: File, overrideUser: String) {
	records.forEach {
		it.clear()
	}
	val json = jacksonObjectMapper().readValue<List<List<RecordTemplate>>>(file)
	json.forEachIndexed { idx, rc ->
		records[idx].addAll(rc.map { it.toRecord(overrideUser) })
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditWeekRecord(record: Record, onValueChange: (Record) -> Unit,
                           teamList: MutableState<TeamList?>,
                           activities: MutableState<ActivityTypeListJson?>,
						   onRemove: () -> Unit,
						   onWarning: (msg: String) -> Unit) {
	Column(Modifier.border(1.dp, Color.Gray, RoundedCornerShape(8.dp)).padding(8.dp)) {
		fun Modifier.cell() = this.weight(1f).fillMaxHeight().padding(4.dp)
		Row(Modifier.height(IntrinsicSize.Max)) {
			TextField(record.title, { onValueChange(record.copy(title = it)) }, Modifier.cell(), placeholder = { Text("Title") })
			Dropdown(Modifier.cell(), menuContent = {
				teamList.value?.teamList?.forEach {
					selectableItem(it.teamID == record.activityUnitID, onClick = {
						onValueChange(record.copy(activityUnitID = it.teamID))
					}) {
						Text(it.teamName)
					}
				}
			}) {
				Text(teamList.value?.teamList?.find { it.teamID == record.activityUnitID }?.teamName ?: "")
			}
			Dropdown(Modifier.cell(), menuContent = {
				activities.value?.activityTypeList?.forEach {
					selectableItem(it.name == record.activityTypeName, onClick = {
						onValueChange(record.copy(activityTypeName = it.name))
					}) {
						Text(it.name)
					}
				}
			}) {
				Text(record.activityTypeName)
			}
		}
		Row(Modifier.height(IntrinsicSize.Max).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
			Text("Begin")
			TimePicker(record.startTime.toTimePair(),
				{
					if(it.toDuration() > record.endTime.toTimePair().toDuration()) {
						onWarning("Start time should be earlier than end time")
						return@TimePicker
					}
					onValueChange(record.copy(startTime = it.toCustomDateTime()))
				}, Modifier.cell(), minuteStep = 10)
			Text("End")
			TimePicker(record.endTime.toTimePair(),
				{
					if(it.toDuration() < record.startTime.toTimePair().toDuration()) {
						onWarning("End time should be later than start time")
						return@TimePicker
					}
					onValueChange(record.copy(endTime = it.toCustomDateTime()))
				}, Modifier.cell(), minuteStep = 10)
			Tooltip({
				Text("Remove")
			}) {
				Row(Modifier.clip(CircleShape).fillMaxHeight().clickable {
					onRemove()
				}.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
					Icon(AllIconsKeys.Diff.Remove, "Remove", Modifier.size(16.dp))
				}
			}
		}
	}
}

suspend fun postMultiRecord(
	userId: String, title: String, project: String, team: String,
	timePairList: List<Pair<CustomDateTime, CustomDateTime>>
): List<RecordResponse?> {
	val base = Record(userId, title, project, team,
		CustomDateTime(), CustomDateTime())
	val retList = mutableListOf<RecordResponse?>()
	for(pair in timePairList) {
		val rec = base.copy(startTime = pair.first, endTime = pair.second)
		retList.add(postRecord(rec))
	}
	return retList
}
