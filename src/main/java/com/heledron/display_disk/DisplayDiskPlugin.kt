package com.heledron.hologram

import com.heledron.hologram.bad_apple.setupBadApplePlayer
import com.heledron.hologram.utilities.*
import com.heledron.hologram.utilities.custom_items.setupCustomItemCommand
import org.bukkit.plugin.java.JavaPlugin


@Suppress("unused")
class HologramPlugin : JavaPlugin() {
    override fun onDisable() {
        shutdownCoreUtils()
    }

    override fun onEnable() {
        setupCoreUtils()
        setupCustomItemCommand()
        setupBadApplePlayer()
    }
}