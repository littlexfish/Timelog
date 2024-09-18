package component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.icon.IconKey
import org.jetbrains.jewel.ui.painter.PainterHint

@Composable
fun IconButton(key: IconKey, contentDescription: String? = null, modifier: Modifier = Modifier,
               iconClass: Class<*> = key::class.java,
               tint: Color = Color.Unspecified,
               vararg hints: PainterHint, onClick: () -> Unit) {
	InnerIconButton(modifier, onClick) {
		Icon(key, contentDescription, Modifier, iconClass, tint, *hints)
	}
}

@Composable
fun IconButton(key: IconKey, contentDescription: String? = null, modifier: Modifier = Modifier,
               iconClass: Class<*> = key::class.java,
               colorFilter: ColorFilter,
               vararg hints: PainterHint, onClick: () -> Unit) {
	InnerIconButton(modifier, onClick) {
		Icon(key, contentDescription, Modifier, iconClass, colorFilter, *hints)
	}
}

@Composable
fun IconButton(bitmap: ImageBitmap, contentDescription: String?, tint: Color = Color.Unspecified,
               modifier: Modifier = Modifier, onClick: () -> Unit) {
	InnerIconButton(modifier, onClick) {
		Icon(bitmap, contentDescription, tint = tint)
	}
}

@Composable
fun IconButton(resource: String, contentDescription: String? = null, iconClass: Class<*>,
               modifier: Modifier = Modifier,
               tint: Color = Color.Unspecified,
               vararg hints: PainterHint, onClick: () -> Unit) {
	InnerIconButton(modifier, onClick) {
		Icon(resource, contentDescription, iconClass, Modifier, tint, *hints)
	}
}

@Composable
fun IconButton(resource: String, contentDescription: String? = null, iconClass: Class<*>,
               modifier: Modifier = Modifier,
               colorFilter: ColorFilter,
               vararg hints: PainterHint, onClick: () -> Unit) {
	InnerIconButton(modifier, onClick) {
		Icon(resource, contentDescription, iconClass, colorFilter, Modifier, *hints)
	}
}

@Composable
fun IconButton(painter: Painter, contentDescription: String? = null,
               modifier: Modifier = Modifier,
               colorFilter: ColorFilter, onClick: () -> Unit) {
	InnerIconButton(modifier, onClick) {
		Icon(painter, contentDescription, colorFilter, Modifier)
	}
}

@Composable
fun IconButton(painter: Painter, contentDescription: String? = null,
               modifier: Modifier = Modifier,
               tint: Color = Color.Unspecified, onClick: () -> Unit) {
	InnerIconButton(modifier, onClick) {
		Icon(painter, contentDescription, Modifier, tint)
	}
}

@Composable
fun IconButton(imageVector: ImageVector, contentDescription: String? = null,
               modifier: Modifier = Modifier,
               tint: Color = Color.Unspecified, onClick: () -> Unit) {
	InnerIconButton(modifier, onClick) {
		Icon(imageVector, contentDescription, Modifier, tint)
	}
}

@Composable
private fun InnerIconButton(modifier: Modifier, onClick: () -> Unit, icon: @Composable () -> Unit) {
	Box(modifier.clip(CircleShape).clickable(onClick = onClick).padding(4.dp)) {
		icon()
	}
}
