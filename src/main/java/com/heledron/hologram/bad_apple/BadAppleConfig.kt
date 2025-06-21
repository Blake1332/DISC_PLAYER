package com.heledron.hologram.bad_apple

import com.heledron.hologram.utilities.currentPlugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object MusicDiscConfig {
    private var config: YamlConfiguration? = null
    private var configFile: File? = null
    
    fun load() {
        configFile = File(currentPlugin.dataFolder, "music_disc_config.yml")
        
        // Create default config if it doesn't exist
        if (!configFile!!.exists()) {
            currentPlugin.saveResource("music_disc_config.yml", false)
        }
        
        config = YamlConfiguration.loadConfiguration(configFile!!)
        
        // Set defaults
        setDefaults()
    }
    
    private fun setDefaults() {
        val cfg = config ?: return
        
        // Video settings
        if (!cfg.contains("video.frame_rate")) cfg.set("video.frame_rate", 30)
        if (!cfg.contains("video.width")) cfg.set("video.width", 64)
        if (!cfg.contains("video.height")) cfg.set("video.height", 48)
        if (!cfg.contains("video.scale")) cfg.set("video.scale", 1.0)
        if (!cfg.contains("video.loop")) cfg.set("video.loop", false)
        
        // Display settings
        if (!cfg.contains("display.billboard")) cfg.set("display.billboard", false)
        if (!cfg.contains("display.double_sided")) cfg.set("display.double_sided", false)
        if (!cfg.contains("display.brightness")) cfg.set("display.brightness", 15)
        if (!cfg.contains("display.teleport_duration")) cfg.set("display.teleport_duration", 1)
        if (!cfg.contains("display.interpolation_duration")) cfg.set("display.interpolation_duration", 1)
        
        // Position settings
        if (!cfg.contains("position.height_above_jukebox")) cfg.set("position.height_above_jukebox", 1.5)
        if (!cfg.contains("position.x_offset")) cfg.set("position.x_offset", 0.5)
        if (!cfg.contains("position.z_offset")) cfg.set("position.z_offset", 0.5)
        
        // File settings
        if (!cfg.contains("files.frames_directory")) cfg.set("files.frames_directory", "plugins/Hologram/music_disc_frames")
        if (!cfg.contains("files.supported_formats")) cfg.set("files.supported_formats", listOf("png", "jpg", "jpeg"))
        
        // Audio settings
        if (!cfg.contains("audio.play_music")) cfg.set("audio.play_music", true)
        if (!cfg.contains("audio.volume")) cfg.set("audio.volume", 1.0)
        if (!cfg.contains("audio.use_custom_audio")) cfg.set("audio.use_custom_audio", true)
        if (!cfg.contains("audio.custom_audio_file")) cfg.set("audio.custom_audio_file", "custom_disc.ogg")
        
        // Custom disc settings
        if (!cfg.contains("disc.use_custom_disc")) cfg.set("disc.use_custom_disc", true)
        if (!cfg.contains("disc.custom_disc_name")) cfg.set("disc.custom_disc_name", "§6Custom Music Disc")
        if (!cfg.contains("disc.custom_disc_lore")) cfg.set("disc.custom_disc_lore", listOf(
            "§7Plays a custom video above jukeboxes",
            "",
            "§eRight-click on a jukebox to play!"
        ))
        
        // Dropper settings
        if (!cfg.contains("dropper.detect_dropper_insertion")) cfg.set("dropper.detect_dropper_insertion", true)
        if (!cfg.contains("dropper.accept_regular_discs")) cfg.set("dropper.accept_regular_discs", true)
        
        save()
    }
    
    fun save() {
        config?.save(configFile!!)
    }
    
    fun reload() {
        load()
    }
    
    // Video settings
    val frameRate: Int get() = config?.getInt("video.frame_rate", 30) ?: 30
    val videoWidth: Int get() = config?.getInt("video.width", 64) ?: 64
    val videoHeight: Int get() = config?.getInt("video.height", 48) ?: 48
    val scale: Float get() = config?.getDouble("video.scale", 1.0)?.toFloat() ?: 1.0f
    val loop: Boolean get() = config?.getBoolean("video.loop", false) ?: false
    
    // Display settings
    val billboard: Boolean get() = config?.getBoolean("display.billboard", false) ?: false
    val doubleSided: Boolean get() = config?.getBoolean("display.double_sided", false) ?: false
    val brightness: Int get() = config?.getInt("display.brightness", 15) ?: 15
    val teleportDuration: Int get() = config?.getInt("display.teleport_duration", 1) ?: 1
    val interpolationDuration: Int get() = config?.getInt("display.interpolation_duration", 1) ?: 1
    
    // Audio settings
    val playMusic: Boolean get() = config?.getBoolean("audio.play_music", true) ?: true
    val musicVolume: Float get() = config?.getDouble("audio.volume", 1.0)?.toFloat() ?: 1.0f
    val useCustomAudio: Boolean get() = config?.getBoolean("audio.use_custom_audio", true) ?: true
    val customAudioFile: String get() = config?.getString("audio.custom_audio_file", "custom_disc.ogg") ?: "custom_disc.ogg"
    
    // Custom disc settings
    val useCustomDisc: Boolean get() = config?.getBoolean("disc.use_custom_disc", true) ?: true
    val customDiscName: String get() = config?.getString("disc.custom_disc_name", "§6Custom Music Disc") ?: "§6Custom Music Disc"
    val customDiscLore: List<String> get() = config?.getStringList("disc.custom_disc_lore") ?: listOf(
        "§7Plays a custom video above jukeboxes",
        "",
        "§eRight-click on a jukebox to play!"
    )
    
    // Dropper settings
    val detectDropperInsertion: Boolean get() = config?.getBoolean("dropper.detect_dropper_insertion", true) ?: true
    val acceptRegularDiscs: Boolean get() = config?.getBoolean("dropper.accept_regular_discs", true) ?: true
    
    // Position settings
    val heightAboveJukebox: Double get() = config?.getDouble("position.height_above_jukebox", 1.5) ?: 1.5
    val xOffset: Double get() = config?.getDouble("position.x_offset", 0.5) ?: 0.5
    val zOffset: Double get() = config?.getDouble("position.z_offset", 0.5) ?: 0.5
    
    // File settings
    val framesDirectory: String get() = config?.getString("files.frames_directory", "plugins/Hologram/music_disc_frames") ?: "plugins/Hologram/music_disc_frames"
    val supportedFormats: List<String> get() = config?.getStringList("files.supported_formats") ?: listOf("png", "jpg", "jpeg")
} 