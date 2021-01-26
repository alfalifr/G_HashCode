import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    kotlin("jvm") version "1.4.21"
    application
}

group = "sidev.aktif.lomba"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = URI("https://dl.bintray.com/alfalifr/SidevLib") }
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("sidev.lib.jvm:JvmStdLib:0.0.1xx")
    implementation("sidev.lib.kotlin:KtStdLib-jvm:0.0.1x.11112020")
    implementation("sidev.lib.kotlin:KtMath-jvm:0.0.1x")
    //implementation("sidev.lib.kotlin:StdLibKt:0.0.1x.11112020")
    //implementation("sidev.lib.kotlin:StdLibKt-jvm:0.0.1x.11112020")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "MainKt"
}