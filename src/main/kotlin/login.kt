import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import dto.Profile
import kotlinx.coroutines.launch
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import java.io.File

@Composable
fun LoginPage(profileState: MutableState<Profile?>) {
	LaunchedEffect(Unit) {
		val lastBearer = getLastBearer()
		if(lastBearer != null) {
			val profile = getProfile(lastBearer)
			if(profile != null) {
				profileState.value = profile
			}
		}
	}
	Box(Modifier.fillMaxSize().padding(8.dp)) {
		Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		val usernameState = remember { mutableStateOf(System.getenv("username") ?: "") }
		val passwordState = remember { mutableStateOf(System.getenv("password") ?: "") }
		val coroutine = rememberCoroutineScope()
		val isLoginState = remember { mutableStateOf(false) }
		val errorPopup = remember { mutableStateOf<String?>(null) }
		val loginAction = {
			isLoginState.value = true
			coroutine.launch {
				val bearer = login(usernameState.value, passwordState.value)
				if(bearer == null) {
					errorPopup.value = "Login failed"
					isLoginState.value = false
					return@launch
				}
				storeBearer(bearer)
				val profile = getProfile(bearer)
				if(profile == null) {
					errorPopup.value = "Failed to get profile"
					isLoginState.value = false
					return@launch
				}
				profileState.value = profile
			}
			Unit
		}
		val defaultFocus = remember { FocusRequester() }
		LaunchedEffect(Unit) {
			defaultFocus.requestFocus()
		}
		TextField(usernameState.value, { usernameState.value = it }, Modifier.focusRequester(defaultFocus).onPreviewKeyEvent {
			if((it.key == Key.Enter || it.key == Key.NumPadEnter) && it.type == KeyEventType.KeyDown) {
				loginAction()
				true
			}
			else false
		}, placeholder = { Text("Username") })
		TextField(passwordState.value, { passwordState.value = it }, Modifier.onPreviewKeyEvent {
			if((it.key == Key.Enter || it.key == Key.NumPadEnter) && it.type == KeyEventType.KeyDown) {
				loginAction()
				true
			}
			else false
		}, placeholder = { Text("Password") },
			visualTransformation = PasswordVisualTransformation())
		if(errorPopup.value != null) {
			DialogWindow(onCloseRequest = { errorPopup.value = null }) {
				Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White).padding(8.dp)) {
					Text(errorPopup.value!!)
				}
			}
		}
		DefaultButton(loginAction, enabled = !isLoginState.value) {
			Text("Login")
		}
	}
		Text("Version: $version", Modifier.align(Alignment.BottomEnd), color = Color.Gray)
	}
}

private val file = File("bearer.txt")

fun getLastBearer(): String? {
	if(file.exists()) {
		return file.readText().ifBlank { null }
	}
	return null
}

fun storeBearer(bearer: String) {
	file.writeText(bearer)
}

fun removeBearer() {
	file.delete()
}
