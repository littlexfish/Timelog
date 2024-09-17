import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.Text
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

typealias DateTriple = Triple<Int?, Int?, Int?>
typealias TimePair = Pair<Int?, Int?>

@JsonSerialize(using = DateTimeSerializer::class)
@JsonDeserialize(using = DateTimeDeserializer::class)
data class CustomDateTime(
	val year: Int? = null,
	val month: Int? = null,
	val day: Int? = null,
	val hour: Int? = null,
	val minute: Int? = null,
): Comparable<CustomDateTime> {

	companion object {
//		private val format = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
//		private val formatOnlyDate = DateTimeFormatter.ofPattern("yyyy/MM/dd")
//		private val formatOnlyTime = DateTimeFormatter.ofPattern("HH:mm")
		fun now(): CustomDateTime {
			return fromCalendar(Calendar.getInstance())
		}
		fun fromCalendar(c: Calendar): CustomDateTime {
			return CustomDateTime(
				c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)
			)
		}
		fun fromDuration(d: Duration): CustomDateTime {
			return CustomDateTime(minute = d.toInt(DurationUnit.MINUTES)).abs()
		}
		fun parse(from: String): CustomDateTime {
			val parts = from.split(' ')
			if(parts.size == 2) {
				val dateParts = parts[0].split('/')
				val timeParts = parts[1].split(':')
				return CustomDateTime(
					dateParts[0].toIntOrNull(),
					dateParts[1].toIntOrNull(),
					dateParts[2].toIntOrNull(),
					timeParts[0].toIntOrNull(),
					timeParts[1].toIntOrNull()
				)
			}
			else {
				val dateParts = parts[0].split('/')
				if(dateParts.size == 3) {
					return CustomDateTime(
						dateParts[0].toIntOrNull(),
						dateParts[1].toIntOrNull(),
						dateParts[2].toIntOrNull()
					)
				}
				else {
					val timeParts = parts[0].split(':')
					return CustomDateTime(
						hour = timeParts[0].toIntOrNull(),
						minute = timeParts[1].toIntOrNull()
					)
				}
			}
		}
	}

	fun toOnlyDate() = CustomDateTime(year, month, day)
	fun toOnlyTime() = CustomDateTime(hour = hour, minute = minute)
	fun toDateTriple() = Triple(year, month, day)
	fun toTimePair() = hour to minute
	fun toResetMinute() = copy(minute = 0)

	private fun getMonthMaxDay() = when(month) {
		1, 3, 5, 7, 8, 10, 12 -> 31
		4, 6, 9, 11 -> 30
		2 -> if((year ?: 0) % 4 == 0) 29 else 28
		else -> 0
	}

	fun isValid(): Boolean {
		val vDay = day ?: 1
		if(vDay <= 0 || vDay > getMonthMaxDay()) return false
		val vHour = hour ?: 0
		if(vHour < 0 || vHour > 23) return false
		val vMinute = minute ?: 0
		return vMinute <= 59
	}

	fun abs(): CustomDateTime {
		return fromCalendar(toCalender())
	}

	fun toCalender(): Calendar {
		return Calendar.Builder().setDate(year ?: 1, (month ?: 1) - 1, day ?: 0)
			.setTimeOfDay(hour ?: 0, minute ?: 0, 0).build()
	}

	override fun compareTo(other: CustomDateTime): Int {
		return toCalender().compareTo(other.toCalender())
	}

	fun getDayOfWeek(): Int {
		return (toCalender().get(Calendar.DAY_OF_WEEK) - 1).let { if(it == 0) 7 else it }
	}

	/**
	 * Without year and month
	 */
	fun toMilliSecond(): Long {
		return (((day ?: 0) * 24L + (hour ?: 0)) * 60 + (minute ?: 0)) * 60 * 1000
	}

	override fun toString(): String {
		// currently disable because formatter will auto add 6 on the minute(WHY?)
//		val inst = toCalender().toInstant().atZone(ZoneId.systemDefault())
//		if(year == null || month == null || day == null) return formatOnlyTime.format(inst)
//		if(hour == null || minute == null) return formatOnlyDate.format(inst)
//		return format.format(inst)
		val abs = abs()
		if(year == null || month == null || day == null) return "${abs.hour.padString(2)}:${abs.minute.padString(2)}"
		if(hour == null || minute == null) return "${abs.year}/${abs.month.padString(2)}/${abs.day.padString(2)}"
		return "${abs.year}/${abs.month.padString(2)}/${abs.day.padString(2)} ${abs.hour.padString(2)}:${abs.minute.padString(2)}"
	}

	private fun Int?.padString(size: Int) = this?.toString()?.padStart(size, '0') ?: ""

	operator fun plus(customDateTime: CustomDateTime): Duration {
		return (toCalender().time.time + customDateTime.toCalender().time.time).milliseconds
	}

	operator fun plus(duration: Duration): CustomDateTime {
		return fromCalendar(toCalender() + duration)
	}

	operator fun minus(customDateTime: CustomDateTime): Duration {
		return (toCalender().time.time - customDateTime.toCalender().time.time).milliseconds
	}

	operator fun minus(duration: Duration): CustomDateTime {
		return fromCalendar(toCalender() - duration)
	}

//	private fun Int?.padString(size: Int) = this?.toString()?.padStart(size, '0') ?: ""

}

private operator fun Calendar.plus(duration: Duration): Calendar {
	return Calendar.Builder().setInstant(time.time + duration.toLong(DurationUnit.MILLISECONDS)).build()
}

private operator fun Calendar.minus(duration: Duration): Calendar {
	return Calendar.Builder().setInstant(time.time - duration.toLong(DurationUnit.MILLISECONDS)).build()
}

fun Duration.toTimePair(): TimePair {
	var minute = toInt(DurationUnit.MINUTES)
	val hour = minute / 60
	minute %= 60
	return hour to minute
}

fun TimePair.toCustomDateTime(): CustomDateTime {
	return CustomDateTime(hour = first, minute = second)
}

fun TimePair.toDuration(): Duration {
	return (first ?: 0).hours + (second ?: 0).minutes
}

fun DateTriple.toCustomDateTime(): CustomDateTime {
	return CustomDateTime(first, second, third)
}

class DateTimeDeserializer : StdDeserializer<CustomDateTime>(CustomDateTime::class.java) {

	private val base = CustomDateTime()

	override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): CustomDateTime {
		return CustomDateTime.parse(p?.valueAsString ?: return base)
	}

}

class DateTimeSerializer : StdSerializer<CustomDateTime>(CustomDateTime::class.java) {

	override fun serialize(value: CustomDateTime?, gen: JsonGenerator?, provider: SerializerProvider?) {
		if(value == null) gen?.writeNull()
		else gen?.writeString(value.toString())
	}

}

enum class DateTimeState {
	DATE, TIME, DATETIME
}

@Composable
fun DateTimePicker(value: CustomDateTime, onValueChange: (CustomDateTime) -> Unit,
                   state: DateTimeState = DateTimeState.DATETIME, modifier: Modifier = Modifier,
				   minuteStep: Int = 1, yearMin: Int = 2000, yearMax: Int = yearMin + 50) {
	Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
		InnerDateTimePicker(value, onValueChange, state, minuteStep, yearMin, yearMax)
	}
}

@Composable
fun HDateTimePicker(value: CustomDateTime, onValueChange: (CustomDateTime) -> Unit,
                    state: DateTimeState = DateTimeState.DATETIME, modifier: Modifier = Modifier,
                    minuteStep: Int = 1, yearMin: Int = 2000, yearMax: Int = yearMin + 50) {
	Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
		InnerDateTimePicker(value, onValueChange, state, minuteStep, yearMin, yearMax)
	}
}

@Composable
private fun InnerDateTimePicker(value: CustomDateTime, onValueChange: (CustomDateTime) -> Unit,
                                state: DateTimeState = DateTimeState.DATETIME, minuteStep: Int = 1,
								yearMin: Int, yearMax: Int) {
	if(state != DateTimeState.TIME) DatePicker(value.toDateTriple(), { onValueChange(value.copy(year = it.first, month = it.second, day = it.third)) },
		yearMin = yearMin, yearMax = yearMax)
	if(state != DateTimeState.DATE) TimePicker(value.toTimePair(), { onValueChange(value.copy(hour = it.first, minute = it.second)) }, minuteStep = minuteStep)
}

@Composable
fun DatePicker(value: DateTriple,
                       onValueChange: (DateTriple) -> Unit,
                       modifier: Modifier = Modifier,
					   yearMin: Int = 2000, yearMax: Int = yearMin + 50) {
	Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
		Dropdown(menuContent = {
			for(i in yearMin..yearMax) {
				selectableItem(i == value.first, onClick = { onValueChange(value.copy(first = i)) }) {
					Text(i.toString())
				}
			}
		}) {
			Text(value.first?.toString() ?: "")
		}
		Text("/")
		Dropdown(menuContent = {
			for(i in 1..12) {
				selectableItem(i == value.second, onClick = { onValueChange(value.copy(second = i)) }) {
					Text(i.toString())
				}
			}
		}) {
			Text(value.second?.toString() ?: "")
		}
		Text("/")
		val maxDay by derivedStateOf {
			when(value.second) {
				1, 3, 5, 7, 8, 10, 12 -> 31
				4, 6, 9, 11 -> 30
				2 -> if((value.third ?: 0) % 4 == 0) 29 else 28
				else -> 0
			}
		}
		Dropdown(menuContent = {
			for(i in 1..maxDay) {
				selectableItem(i == value.third, onClick = { onValueChange(value.copy(third = i)) }) {
					Text(i.toString())
				}
			}
		}) {
			Text(value.third?.toString() ?: "")
		}
	}
}

@Composable
fun TimePicker(value: TimePair, onValueChange: (TimePair) -> Unit, modifier: Modifier = Modifier, minuteStep: Int = 1) {
	Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
		Dropdown(menuContent = {
			for(i in 0..23) {
				selectableItem(i == value.first, onClick = { onValueChange(value.copy(first = i)) }) {
					Text(i.toString())
				}
			}
		}) {
			Text(value.first?.toString() ?: "")
		}
		Text(":")
		Dropdown(menuContent = {
			for(i in 0..59 step minuteStep) {
				selectableItem(i == value.second, onClick = { onValueChange(value.copy(second = i)) }) {
					Text(i.toString())
				}
			}
		}) {
			Text(value.second?.toString() ?: "")
		}
	}
}

