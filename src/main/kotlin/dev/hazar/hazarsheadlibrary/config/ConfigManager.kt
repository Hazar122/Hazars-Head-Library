package dev.hazar.hazarsheadlibrary.config

import com.google.gson.GsonBuilder
import java.io.File

internal object ConfigManager {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val configDir= File("config/hazar/head_library")
    private val configFile = File(configDir, "config.json")

    internal lateinit var config: HeadLibraryConfig
        private set

    internal fun loadOrCreateConfig() {
        if (!configDir.exists()) configDir.mkdirs()

        config = if (configFile.exists()) {
            try {
                gson.fromJson(configFile.readText(), HeadLibraryConfig::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                createDefaultConfig()
            }
        } else {
            createDefaultConfig()
        }
    }

    internal fun reloadConfig() {
        if (configFile.exists()) {
            try {
                config = gson.fromJson(configFile.readText(), HeadLibraryConfig::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createDefaultConfig(): HeadLibraryConfig {
        val defaultConfig = HeadLibraryConfig()
        configFile.writeText(gson.toJson(defaultConfig))
        return defaultConfig
    }
}