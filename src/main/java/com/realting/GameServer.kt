package com.realting

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.realting.GameConfiguration
import com.realting.util.ShutdownHook
import sun.misc.Unsafe
import java.io.File
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * The starting point of Ruse.
 * @author Gabriel
 * @author Samy
 */
object GameServer {
    @JvmStatic
    val logger = Logger.getLogger("Ruse")

    @JvmStatic
    var loader: GameLoader? = null
        private set

    @JvmStatic
    var isUpdating = false

    /**
     * Gets the [GameConfiguration]
     * @return the [GameConfiguration]
     */
    @JvmStatic
    var configuration: GameConfiguration? = null
        private set

    @JvmStatic
    fun main(params: Array<String>) {
        Runtime.getRuntime().addShutdownHook(ShutdownHook())
        try {
            disableWarning()
            loadConfiguration()
            loader = GameLoader(configuration!!.port)
            logger.info("Initializing the loader...")
            loader!!.init()
            loader!!.finish()
            logger.info("The loader has finished loading utility tasks.")
            logger.info(GameSettings.RSPS_NAME + " is now online on port " + configuration!!.port + "!")
        } catch (ex: Exception) {
            logger.log(Level.SEVERE, "Could not start " + GameSettings.RSPS_NAME + "! Program terminated.", ex)
            System.exit(1)
        }
    }

    /**
     * Load yaml configuration.
     * @throws IOException if an error occurs while reading the file.
     */
    @Throws(IOException::class)
    private fun loadConfiguration() {
        val configFile = File(GameSettings.GAME_CONFIGURATION_FILE)
        val mapper = ObjectMapper(YAMLFactory())
        if (!configFile.exists()) {
            mapper.writeValue(configFile, GameConfiguration.default)
            logger.info("Created default configuration file.")
        }
        mapper.findAndRegisterModules()
        configuration = mapper.readValue(configFile, GameConfiguration::class.java)
        logger.info("Loaded configuration.")
    }

    /**
     * Disables the "WARNING: An illegal reflective access operation has occurred"
     */
    fun disableWarning() {
        try {
            val theUnsafe = Unsafe::class.java.getDeclaredField("theUnsafe")
            theUnsafe.isAccessible = true
            val u = theUnsafe[null] as Unsafe
            val cls = Class.forName("jdk.internal.module.IllegalAccessLogger")
            val logger = cls.getDeclaredField("logger")
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null)
        } catch (e: Exception) {
        }
    }
}