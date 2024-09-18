package component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.Text

@Composable
fun TeamSelect(modifier: Modifier = Modifier, current: String? = null, onValueChange: (String?) -> Unit) {
	Dropdown(modifier, menuContent = {
		selectableItem(current == null, onClick = {
			onValueChange(null)
		}) {
			Text("(Personal)")
		}
		Lookup.teams.teamList.forEach {
			selectableItem(it.teamID == current, onClick = {
				onValueChange(it.teamID)
			}) {
				Text(it.teamName)
			}
		}
	}) {
		Text(Lookup.teams.teamList.find { it.teamID == current }?.teamName ?: "(Personal)")
	}
}

@Composable
fun ActivitySelect(modifier: Modifier = Modifier, current: String? = null, onValueChange: (String?) -> Unit) {
	Dropdown(modifier, menuContent = {
		selectableItem(current == null, onClick = {
			onValueChange(null)
		}) {
			Text("")
		}
		Lookup.activities.activityTypeList.forEach {
			selectableItem(it.name == current, onClick = {
				onValueChange(it.name)
			}) {
				Text(it.name)
			}
		}
	}) {
		Text(current ?: "")
	}
}

