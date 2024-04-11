import com.pswidersk.gradle.python.PythonPluginExtension
import com.pswidersk.gradle.python.VenvTask
import java.io.ByteArrayOutputStream
import java.util.*


plugins {
    id("com.pswidersk.python-plugin")
}

version = "1.0"

pythonPlugin {
    pythonVersion.set("3.9")
    condaVersion.set("py39_23.10.0-1")
}


tasks {
    // Define a custom task that watches for changes in the build.gradle.kts file
    register("killPython") {
        group = "clean"
    }

    register("killPortProcess") {
        group = "clean"
    }

    register("generateBuildConfig") {
        description = "Generates BuildConfig.kt file"
    }

    val pipInstall by registering(VenvTask::class) {
        venvExec = "pip"
        dependsOn("generateBuildConfig")
        if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("mac")) {
            //if it is mac system
            dependsOn("filterRequirements")
            args = listOf("install", "-r", "requirements-filtered.txt")
        } else {
            args = listOf("install", "-r", "requirements.txt")
        }
//        args = listOf("install", "--upgrade", "--force", "jep")
//        args = listOf("install", "--isolated", "-r", "requirements.txt")
    }


    register("filterRequirements") {
        doLast {
            val requirements = file("requirements.txt").readLines()
            val filteredRequirements = requirements.filter { !it.startsWith("gm") }
            file("requirements-filtered.txt").writeText(filteredRequirements.joinToString("\n"))
        }
    }

    register<VenvTask>("condaListEnv") {
        venvExec = "conda"
        args = listOf("env", "list")
    }

    register<VenvTask>("runAPIApp") {
        group = "server"
        workingDir = file("python/src")
        venvExec = "uvicorn"
        args = listOf(
            "api:app",
            "--host",
            "0.0.0.0",
            "--port",
            "7701",
            "--reload"
        )
//        environment = mapOf("FLASK_APP" to "flask_app")
    }

    register<VenvTask>("runNlpApp") {
        group = "server"
        workingDir = file("python/src")
        venvExec = "uvicorn"
        args = listOf("nlp:app", "--host", "0.0.0.0", "--port", "7702", "--reload")
//        environment = mapOf("FLASK_APP" to "flask_app")
    }

    register<VenvTask>("runProxyApp") {
        group = "server"
        workingDir = file("python/src")
        venvExec = "uvicorn"
        args = listOf("proxy:app", "--host", "0.0.0.0", "--port", "7703", "--reload")
//        environment = mapOf("FLASK_APP" to "flask_app")
    }
}

tasks.named("killPython") {
    doLast {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        if (os.contains("win")) {
            exec {
                commandLine("cmd", "/c", "taskkill", "/F", "/IM", "python.exe")
            }
        } else {
            exec {
                commandLine("pkill", "python")
            }
        }
    }
}

tasks.named("killPortProcess") {
    val port = "7701"

    doLast {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        val outputs = ByteArrayOutputStream()
        if (os.contains("win")) {
            exec {
                commandLine("cmd", "/c", "netstat", "-ano", "|", "findstr", ":$port")
                standardOutput = outputs
            }

            val pids = outputs.toString().lines()
                .mapNotNull { it.trim().split(" ").getOrNull(4)?.toInt() }

            if (pids.isNotEmpty()) {
                pids.forEach { pid ->
                    exec {
                        commandLine("cmd", "/c", "taskkill", "/F", "/PID", "$pid")
                    }
                }
            } else {
                println("No processes found listening on port $port")
            }
        } else {
            exec {
                commandLine("lsof", "-i", ":$port")
                standardOutput = outputs
            }

            val pids = outputs.toString().lines()
                .mapNotNull { it.trim().split(" ").getOrNull(1)?.toInt() }

            if (pids.isNotEmpty()) {
                pids.forEach { pid ->
                    exec {
                        commandLine("kill", "-9", "$pid")  // Use kill -9 for forceful termination
                    }
                }
            } else {
                println("No processes found listening on port $port")
            }
        }
    }
}

// Define the @TaskAction outside of the tasks block
tasks.named("generateBuildConfig") {
    doLast {
        // generate build for for jep python interpreter
        val buildConfigFile = rootProject.file("base/src/main/kotlin/BuildConfig.kt")

        // Create the directory if it doesn't exist
        buildConfigFile.parentFile.mkdirs()

        rootProject.file("output/data").mkdirs()
        rootProject.file("output/pdata").mkdirs()
        // Get the value from the plugin extension
        val pythonPluginExtension = project.extensions.getByType(PythonPluginExtension::class)
        var pythonEnvDir = pythonPluginExtension.pythonEnvDir.get().asFile.canonicalPath
        var srcRelativePath = project.name + File.separator + "python" + File.separator + "src"
        var sourceDir = project.file("python/src").canonicalPath

        //handle backslash
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        if (os.contains("win")) {
            pythonEnvDir = pythonEnvDir.replace("\\", "\\\\")
            srcRelativePath = srcRelativePath.replace("\\", "\\\\")
            sourceDir = sourceDir.replace("\\", "\\\\")
        }
        val buildConfigContent = """
            object BuildConfig {
                const val PYTHON_ENV_DIR = "$pythonEnvDir"
                const val SRC_RELATIVE_PATH = "$srcRelativePath"
                const val SOURCE_DIR = "$sourceDir"
            }
        """.trimIndent()
        // Write the content to BuildConfig.kt
        println("Writing to file: $buildConfigFile")
        buildConfigFile.writeText(buildConfigContent)
    }
}

