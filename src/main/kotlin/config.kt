import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import kotlin.system.exitProcess

private val configFile = File("config.json")
private var internalConfig: Config? = null

val configFileExists get() = configFile.exists()
val config: Config get() = internalConfig!!

fun initConfig(): Boolean {
	return if(!configFileExists) {
		defaultConfig()
		saveConfig()
		true
	}
	else {
		try {
			jacksonObjectMapper().readValue<Config>(configFile).also {
				internalConfig = it
			}
			normalizeConfig()
			true
		}
		catch(e: Exception) {
			processErrorConfig()
			false
		}
	}
}

private fun normalizeConfig() {
	config.authServer = config.authServer.removeSuffix("/")
	config.timelogServer = config.timelogServer.removeSuffix("/")
}

private fun processErrorConfig() {
	val tmpConfigFile = File("config.json~")
	try {
		configFile.copyTo(tmpConfigFile, true)
		configFile.delete()
	}
	catch(e: Exception) {
		System.err.println("Failed to backup config file")
		e.printStackTrace()
		exitProcess(1)
	}
}

private fun defaultConfig() {
	internalConfig = Config("http://localhost:8080", "http://localhost:8081")
}

/**
 * Save config as pretty json let user can edit it directly
 */
fun saveConfig() {
	normalizeConfig()
	jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValue(configFile, internalConfig)
}

data class Config(var authServer: String, var timelogServer: String)
