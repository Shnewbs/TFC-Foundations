import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    id("net.neoforged.moddev") version "2.0.148"
    id("net.neoforged.licenser") version "0.7.5"
}


// Verified against the NeoForge 26.3 MDK; integration versions must target 26.3.
val minecraftVersion: String = providers.gradleProperty("minecraftVersion").get()
val neoForgeVersion: String = providers.gradleProperty("neoForgeVersion").get()
val jeiVersion: String = providers.gradleProperty("jeiVersion").get()
val patchouliVersion = providers.gradleProperty("patchouliVersion")
val emiVersion = providers.gradleProperty("emiVersion")
val jadeVersion = providers.gradleProperty("jadeVersion")
val theOneProbeVersion = providers.gradleProperty("theOneProbeVersion")

val modId: String = "tfc"
val modVersion: String = providers.gradleProperty("modVersion").get()
val modJavaVersion: String = "25"
val modIsInCI: Boolean = providers.environmentVariable("CI").map { it == "true" }.getOrElse(false)
val modDataOutput: String = "src/generated/resources"


val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val modReplacementProperties = mapOf(
        "modId" to modId,
        "modVersion" to modVersion,
        "minecraftVersionRange" to "[$minecraftVersion]",
        "neoForgeVersionRange" to "[$neoForgeVersion,)",
        "patchouliVersionRange" to patchouliVersion.map { "[$it,)" }.getOrElse("[0,)"),
        "jeiVersionRange" to "[$jeiVersion,)"
    )
    inputs.properties(modReplacementProperties)
    expand(modReplacementProperties)
    from("src/main/templates")
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}

neoForge {
    version = neoForgeVersion // this is here because declaring a neoForge version enables 'additionalRuntimeClasspath'
}

base {
    archivesName.set("TFC-Foundations-$minecraftVersion")
    group = "net.dries007.tfc"
    version = modVersion
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
}

repositories {
    mavenCentral()
    mavenLocal()
    exclusiveContent {
        forRepository { maven("https://maven.terraformersmc.com/") }
        filter { includeGroup("dev.emi") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.blamejared.com/") }
        filter {
            includeGroup("mezz.jei")
            includeGroup("net.mezzdev.config")
        }
    }
    exclusiveContent {
        forRepository { maven("https://maven.k-4u.nl/") }
        filter { includeGroup("mcjty.theoneprobe") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.blamejared.com") }
        filter { includeGroup("vazkii.patchouli") }
    }
    exclusiveContent {
        forRepository { maven("https://www.cursemaven.com") }
        filter { includeGroup("curse.maven") }
    }
}

sourceSets {
    main {
        resources {
            srcDir(modDataOutput)
            srcDir(generateModMetadata)
        }
    }
    create("data")
}

neoForge {
    addModdingDependenciesTo(sourceSets["data"])
    validateAccessTransformers = true

    runs {
        configureEach {
            // Only JBR allows enhanced class redefinition, so ignore the option for any other JDKs
            jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition", "-ea")
            systemProperty("tfc.enableDebugSelfTests", "true")
        }
        register("client") {
            client()
            gameDirectory = file("run/client")
        }
        register("server") {
            server()
            gameDirectory = file("run/server")
            programArgument("--nogui")
        }
        register("data") {
            clientData()
            sourceSet = sourceSets["data"]
            programArguments.addAll("--all", "--mod", modId, "--output", file(modDataOutput).absolutePath, "--existing",  file("src/main/resources").absolutePath)
        }
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["data"])
        }
    }

    unitTest {
        enable()
        testedMod = mods[modId];
    }

    ideSyncTask(generateModMetadata)
}

dependencies {
    // Integration artifacts must match the Minecraft target of this branch.
    // Unported integrations remain in source until replacement adapters are ready.
    if (emiVersion.isPresent) {
        compileOnly("dev.emi:emi-neoforge:${emiVersion.get()}:api")
    }
    compileOnly("mezz.jei:jei-${minecraftVersion}-common-api:${jeiVersion}")
    compileOnly("mezz.jei:jei-${minecraftVersion}-neoforge-api:${jeiVersion}")
    runtimeOnly("mezz.jei:jei-${minecraftVersion}-neoforge:${jeiVersion}")
    if (patchouliVersion.isPresent) {
        implementation("vazkii.patchouli:patchouli-neoforge:${patchouliVersion.get()}")
        "dataImplementation"("vazkii.patchouli:patchouli-neoforge:${patchouliVersion.get()}")
    }
    if (jadeVersion.isPresent) {
        implementation("curse.maven:jade-324717:${jadeVersion.get()}")
    }
    if (theOneProbeVersion.isPresent) {
        compileOnly("mcjty.theoneprobe:theoneprobe:${theOneProbeVersion.get()}")
    }

    // Data
    "dataImplementation"(sourceSets["main"].output)

    // Test
    // Use JUnit at runtime, plus depend on data to allow us to mock certain data without having to load a server
    testImplementation(sourceSets["data"].output)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.3")
}

// Fail before producing a misleading artifact while legacy adapters are still linked.
val requiredIntegrationVersions = listOf("patchouliVersion", "emiVersion", "jadeVersion", "theOneProbeVersion")
val missingIntegrationVersions = requiredIntegrationVersions.filter { !providers.gradleProperty(it).isPresent }
val verifyPortDependencies = tasks.register("verifyPortDependencies") {
    inputs.property("missingIntegrationVersions", missingIntegrationVersions)
    doLast {
        val missing = inputs.properties.getValue("missingIntegrationVersions") as List<*>
        check(missing.isEmpty()) {
            "Port is incomplete: verified target-version dependencies are missing for " +
                missing.joinToString() +
                ". Port/isolate these adapters or configure verified 26.3 artifacts; do not use 1.21.1 jars."
        }
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
tasks.named("jar") { dependsOn(verifyPortDependencies) }

// Automatically apply a license header when running checkLicense / updateLicense
license {
    header(project.file("HEADER.txt"))

    include("**/*.java")
    exclude("net/dries007/tfc/world/noise/FastNoiseLite.java") // Fast Noise
}

abstract class MinifyJsonTask : DefaultTask() {

    @get:InputDirectory
    abstract val dir: DirectoryProperty

    @TaskAction
    fun minify() {
        val jsonSlurper = JsonSlurper()
        var jsonMinified = 0
        var jsonBytesBefore = 0L
        var jsonBytesAfter = 0L
        val start = System.currentTimeMillis()

        dir.get().asFileTree.matching {
            include("**/*.json")
        }.forEach { file ->
            jsonMinified++
            jsonBytesBefore += file.length()
            try {
                val parsed = jsonSlurper.parse(file)
                val minified = JsonOutput.toJson(parsed)
                    .replace("\"__comment__\":\"This file was automatically created by mcresources\",", "")
                file.writeText(minified)
            } catch (e: Exception) {
                logger.error("JSON Error in ${file.path}", e)
                throw e
            }
            jsonBytesAfter += file.length()
        }

        logger.lifecycle(
            "Minified $jsonMinified JSON files. Reduced ${jsonBytesBefore / 1024} kB → ${(jsonBytesAfter / 1024)} kB. Took ${System.currentTimeMillis() - start} ms"
        )
    }
}

if (modIsInCI) {
    tasks.register<MinifyJsonTask>("minifyJson") {
        // `processResources` writes into build/resources/<sourceSet>
        dir.set(layout.buildDirectory.dir("resources/main"))
    }

    tasks.named<ProcessResources>("processResources") {
        finalizedBy("minifyJson") // run AFTER processResources
    }
}


tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("failed", "standardError")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showCauses = true
            showExceptions = true
            showStackTraces = true
        }
    }

    jar {
        manifest {
            attributes["Implementation-Version"] = project.version
        }
    }

    named("neoForgeIdeSync") {
        dependsOn(generateModMetadata)
    }
}

