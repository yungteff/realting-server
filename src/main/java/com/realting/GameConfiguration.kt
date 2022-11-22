package com.realting

/**
 * Defines runtime configuration.
 * @author Michael Sasse (https://github.com/mikeysasse/)
 */
class GameConfiguration private constructor() {
    var port = 0
    var isDiscordBotEnabled = false
    var isEncryptPasswords = false
    var isDebug = false

    companion object {
        /**
         * Gets the default game configuration.
         * @return the default configuration.
         */
        val default: GameConfiguration
            get() {
                val configuration = GameConfiguration()
                configuration.port = 13377
                configuration.isDiscordBotEnabled = false
                configuration.isEncryptPasswords = false
                configuration.isDebug = true
                return configuration
            }
    }
}