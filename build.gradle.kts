import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    id("net.neoforged.moddev") version "2.0.148"
    id("net.neoforged.licenser") version "0.7.5"
}


// Integration versions must match the Minecraft target of this branch.
val minecraftVersion: String = providers.gradleProperty("minecraftVersion").get()
val neoForgeVersion: String = providers.gradleProperty("neoForgeVersion").get()
val jeiVersion: String = providers.gradleProperty("jeiVersion").get()
val patchouliVersion = providers.gradleProperty("patchouliVersion")
val emiVersion = providers.gradleProperty("emiVersion")
val jadeVersion = providers.gradleProperty("jadeVersion")
val theOneProbeVersion = providers.gradleProperty("theOneProbeVersion")
val optionalIntegrationVersions = mapOf(
    "emi" to emiVersion,
    "jade" to jadeVersion,
    "theoneprobe" to theOneProbeVersion
)

val modId: String = "tfc"
val modVersion: String = providers.gradleProperty("modVersion").get()
val modJavaVersion: String = "25"
val modIsInCI: Boolean = providers.environmentVariable("CI").map { it == "true" }.getOrElse(false)
val modDataOutput: String = "src/generated/resources"

// MC 26.x requires assets/tfc/items/<id>.json selectors in addition to
// models/item/<id>.json. Generate only static model selectors: custom loaders,
// legacy override predicates and tinted geometry need target-specific adapters.
val generatedItemSelectorsRoot = layout.buildDirectory.dir("generated/tfcItemSelectors")
val generateTfcItemSelectors = tasks.register<Exec>("generateTfcItemSelectors") {
    inputs.dir("src/main/resources/assets/tfc/models/item")
    inputs.file("tools/porting/generate_item_selectors.py")
    outputs.dir(generatedItemSelectorsRoot)
    outputs.file(layout.buildDirectory.file("reports/tfcItemSelectors.json"))
    commandLine(
        "python3", "tools/porting/generate_item_selectors.py",
        "--output", generatedItemSelectorsRoot.get().asFile.absolutePath,
        "--report", layout.buildDirectory.file("reports/tfcItemSelectors.json").get().asFile.absolutePath
    )
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(generateTfcItemSelectors)
    from(generatedItemSelectorsRoot)
}


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
        java {
            // Optional adapters compile independently, only with a verified dependency pin.
            optionalIntegrationVersions.keys.forEach { exclude("net/dries007/tfc/compat/$it/**") }
            exclude("net/dries007/tfc/mixin/client/compat/jade/**")
        }
        resources {
            srcDir(modDataOutput)
            srcDir(generateModMetadata)
        }
    }
    create("data")
}

val optionalIntegrationSources = optionalIntegrationVersions.filterValues { it.isPresent }.mapValues { (name, _) ->
    sourceSets.create("${name}Integration") {
        java.srcDir("src/main/java")
        java.include("net/dries007/tfc/compat/$name/**")
        if (name == "jade") java.include("net/dries007/tfc/mixin/client/compat/jade/**")
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }.also {
        configurations[it.implementationConfigurationName].extendsFrom(configurations.implementation.get())
        configurations[it.compileOnlyConfigurationName].extendsFrom(configurations.compileOnly.get())
        configurations[it.runtimeOnlyConfigurationName].extendsFrom(configurations.runtimeOnly.get())
    }
}

neoForge {
    addModdingDependenciesTo(sourceSets["data"])
    optionalIntegrationSources.values.forEach { addModdingDependenciesTo(it) }
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
            optionalIntegrationSources.values.forEach { sourceSet(it) }
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
    // Unpinned optional adapters stay in source but are absent from the compiled mod.
    optionalIntegrationSources["emi"]?.let {
        add(it.compileOnlyConfigurationName, "dev.emi:emi-neoforge:${emiVersion.get()}:api")
    }
    compileOnly("mezz.jei:jei-${minecraftVersion}-common-api:${jeiVersion}")
    compileOnly("mezz.jei:jei-${minecraftVersion}-neoforge-api:${jeiVersion}")
    runtimeOnly("mezz.jei:jei-${minecraftVersion}-neoforge:${jeiVersion}")
    if (patchouliVersion.isPresent) {
        implementation("vazkii.patchouli:patchouli-neoforge:${patchouliVersion.get()}")
        "dataImplementation"("vazkii.patchouli:patchouli-neoforge:${patchouliVersion.get()}")
    }
    optionalIntegrationSources["jade"]?.let {
        add(it.implementationConfigurationName, "curse.maven:jade-324717:${jadeVersion.get()}")
    }
    optionalIntegrationSources["theoneprobe"]?.let {
        add(it.compileOnlyConfigurationName, "mcjty.theoneprobe:theoneprobe:${theOneProbeVersion.get()}")
    }

    // Data
    "dataImplementation"(sourceSets["main"].output)

    // Test
    // Use JUnit at runtime, plus depend on data to allow us to mock certain data without having to load a server
    testImplementation(sourceSets["data"].output)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.3")
}

// The field guide is required gameplay content. Optional adapters no longer gate packaging:
// their source sets are compiled and bundled only when their dependency is pinned.
val requiredIntegrationVersions = listOf("patchouliVersion")
val missingIntegrationVersions = requiredIntegrationVersions.filter { !providers.gradleProperty(it).isPresent }
val verifyPortDependencies = tasks.register("verifyPortDependencies") {
    inputs.property("missingIntegrationVersions", missingIntegrationVersions)
    doLast {
        val missing = inputs.properties.getValue("missingIntegrationVersions") as List<*>
        check(missing.isEmpty()) {
            "Port is incomplete: verified target-version dependencies are missing for " +
                missing.joinToString() +
                ". Configure a verified artifact for this branch; do not use legacy runtime jars."
        }
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // Expose the migration backlog in one CI pass without suppressing any errors.
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "10000"))
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
        optionalIntegrationSources.values.forEach { from(it.output) }
        manifest {
            attributes["Implementation-Version"] = project.version
        }
    }

    named("neoForgeIdeSync") {
        dependsOn(generateModMetadata)
    }
}

