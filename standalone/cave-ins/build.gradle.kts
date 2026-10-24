import groovy.json.JsonOutput
import net.neoforged.moddevgradle.internal.RunGameTask

plugins { id("net.neoforged.moddev") version "2.0.148" }
val minecraftVersion: String by project
val neoForgeVersion: String by project
val modVersion: String by project
group = "dev.shnewbs.foundations.caveins"
version = modVersion
base { archivesName.set("Foundations-Cave-Ins-$minecraftVersion") }
java { toolchain.languageVersion.set(JavaLanguageVersion.of(25)) }
repositories { mavenCentral() }
neoForge {
    version = neoForgeVersion
    runs {
        register("server") { server(); gameDirectory = file("run/server"); programArgument("--nogui") }
        register("client") { client(); gameDirectory = file("run/client") }
    }
    mods { create("foundations_caveins") { sourceSet(sourceSets.main.get()) } }
    unitTest { enable(); testedMod = mods["foundations_caveins"] }
}
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.3")
}
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8"; options.isIncremental = false }
tasks.test { useJUnitPlatform() }
tasks.processResources {
    val replacements = mapOf("modVersion" to modVersion, "minecraftVersion" to minecraftVersion, "neoForgeVersion" to neoForgeVersion)
    inputs.properties(replacements)
    filesMatching("META-INF/neoforge.mods.toml") { expand(replacements) }
}
tasks.jar { from("../../LICENSE.txt"); from("NOTICE.md") }

// Test infrastructure only; no validation classes or helpers enter the mod JAR.
tasks.register("exportValidationServerLaunch") {
    dependsOn("prepareServerRun")
    doLast {
        val run = tasks.named<RunGameTask>("runServer").get()
        val result = mapOf(
            "classpath" to run.classpathProvider.files.map { it.absolutePath },
            "jvmArgs" to run.allJvmArgs,
            "mainClass" to run.mainClass.get(),
            "args" to run.args,
            "environment" to run.environmentProperty.get()
        )
        val output = layout.buildDirectory.file("validation/server-launch.json").get().asFile
        output.parentFile.mkdirs()
        output.writeText(JsonOutput.toJson(result))
    }
}
