import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val ktor_version: String by project
val platform_version: String by project
val jewel_version: String by project

plugins {
	kotlin("jvm")
	id("org.jetbrains.compose")
	id("org.jetbrains.kotlin.plugin.compose")
}

group = "dev.littlexfish"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
	maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	maven("https://packages.jetbrains.team/maven/p/kpm/public/")
	maven("https://www.jetbrains.com/intellij-repository/releases/")
	google()
}

dependencies {
	implementation("org.jetbrains.jewel:jewel-int-ui-standalone-$platform_version:$jewel_version")
	implementation("org.jetbrains.jewel:jewel-int-ui-decorated-window-$platform_version:$jewel_version")
	implementation("com.jetbrains.intellij.platform:icons:241.18034.62")

	// Do not bring in Material (we use Jewel)
	implementation(compose.desktop.currentOs) {
		exclude(group = "org.jetbrains.compose.material")
	}
	implementation("io.ktor:ktor-client-core:$ktor_version")
	implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
	implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
	implementation("io.ktor:ktor-client-okhttp:$ktor_version")
	// change file picker to filekit because mpfilepicker is unmaintained & it not supports save mode
//	implementation("com.darkrockstudios:mpfilepicker:3.1.0")
	implementation("io.github.vinceglb:filekit-compose:0.8.2")
}

compose.desktop {
	application {
		mainClass = "MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.AppImage)
			packageName = "Timelog"
			packageVersion = "1.0.3"
		}
	}
}
