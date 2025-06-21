package com.heledron.hologram.bad_apple

import com.heledron.hologram.utilities.currentTick
import com.heledron.hologram.utilities.events.onTick
import com.heledron.hologram.utilities.events.addEventListener
import com.heledron.hologram.utilities.images.sampleColor
import com.heledron.hologram.utilities.images.resize
import com.heledron.hologram.utilities.rendering.RenderGroup
import com.heledron.hologram.utilities.rendering.interpolateTransform
import com.heledron.hologram.utilities.rendering.renderText
import com.heledron.hologram.utilities.rendering.textDisplayUnitSquare
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Jukebox
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.inventory.ItemStack
import org.joml.Matrix4f
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class BadApplePlayer {
    private var videoFrames = mutableListOf<BufferedImage>()
    private var frameRate = MusicDiscConfig.frameRate
    private var videoWidth = MusicDiscConfig.videoWidth
    private var videoHeight = MusicDiscConfig.videoHeight
    private var scale = MusicDiscConfig.scale
    
    // Track jukeboxes playing Bad Apple
    private val playingJukeboxes = mutableMapOf<Vector, JukeboxData>()
    
    data class JukeboxData(
        val world: World,
        val jukebox: Jukebox,
        val startTick: Long,
        val isCustomDisc: Boolean,
        var displayGroup: RenderGroup = RenderGroup()
    )
    
    fun loadVideo(videoPath: String) {
        val videoDir = File(videoPath)
        if (!videoDir.exists() || !videoDir.isDirectory) {
            println("Video directory not found: $videoPath")
            return
        }
        
        videoFrames.clear()
        val frameFiles = videoDir.listFiles { file -> 
            file.isFile && file.extension.lowercase() in MusicDiscConfig.supportedFormats
        }?.sortedBy { it.name } ?: emptyList()
        
        for (frameFile in frameFiles) {
            try {
                val frame = ImageIO.read(frameFile)
                val resizedFrame = frame.resize(videoWidth, videoHeight)
                videoFrames.add(resizedFrame)
            } catch (e: Exception) {
                println("Failed to load frame: ${frameFile.name}")
            }
        }
        
        println("Loaded ${videoFrames.size} frames")
    }
    
    fun startJukeboxVideo(jukebox: Jukebox) {
        val position = jukebox.location.toVector()
        val world = jukebox.world
        
        // Check if this jukebox is already playing
        if (playingJukeboxes.containsKey(position)) {
            return
        }
        
        // Load video if not already loaded
        if (videoFrames.isEmpty()) {
            val videoPath = MusicDiscConfig.framesDirectory
            loadVideo(videoPath)
        }
        
        val jukeboxData = JukeboxData(
            world = world,
            jukebox = jukebox,
            startTick = currentTick.toLong(),
            isCustomDisc = true // Since this method is only called for our custom disc
        )
        
        playingJukeboxes[position] = jukeboxData
        
        // Play a special sound to indicate activation
        world.playSound(jukebox.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f)
        
        println("Started Bad Apple video at jukebox: ${jukebox.location}")
    }

    fun stopJukeboxVideo(jukebox: Jukebox) {
        val position = jukebox.location.toVector()
        val jukeboxData = playingJukeboxes.remove(position)
        
        if (jukeboxData != null) {
            // Create a new empty display group to clear the previous one
            jukeboxData.displayGroup = RenderGroup()
            println("Stopped Bad Apple video at jukebox: \\${jukebox.location}")
        }
    }
    
    fun hasFrames(): Boolean {
        return videoFrames.isNotEmpty()
    }
    
    private fun isDisc(item: ItemStack?): Boolean {
        if (item == null) return false
        return if (MusicDiscConfig.useCustomDisc) {
            Disc.isDisc(item)
        } else {
            item.type == Material.MUSIC_DISC_BLOCKS
        }
    }
    
    private fun isDisc(material: Material?): Boolean {
        if (material == null) return false
        return if (MusicDiscConfig.useCustomDisc) {
            material == Material.MUSIC_DISC_BLOCKS
        } else {
            material == Material.MUSIC_DISC_BLOCKS
        }
    }
    
    fun update() {
        if (videoFrames.isEmpty()) return
        
        val currentTime = currentTick.toLong()
        
        // Update each playing jukebox
        val jukeboxesToRemove = mutableListOf<Vector>()
        
        for ((position, jukeboxData) in playingJukeboxes) {
            // Check if jukebox still exists and is still playing the disc
            val block = jukeboxData.world.getBlockAt(position.toLocation(jukeboxData.world))
            if (block.type != Material.JUKEBOX) {
                jukeboxesToRemove.add(position)
                continue
            }
            
            val jukebox = block.state as? Jukebox
            if (jukebox == null || !jukeboxData.isCustomDisc) {
                jukeboxesToRemove.add(position)
                continue
            }
            
            // Calculate current frame based on time since start
            val timeSinceStart = currentTime - jukeboxData.startTick
            val totalFrames = videoFrames.size
            val framesPerSecond = frameRate.toFloat()
            val ticksPerSecond = 20.0f
            val totalVideoDuration = (totalFrames / framesPerSecond * ticksPerSecond).toLong()
            
            // Check if video has finished
            if (!MusicDiscConfig.loop && timeSinceStart >= totalVideoDuration) {
                // Video has finished, stop it
                jukeboxesToRemove.add(position)
                println("Video finished at jukebox: ${jukebox.location}")
                continue
            }
            
            // Calculate current frame (with looping if enabled)
            val currentFrame = if (MusicDiscConfig.loop) {
                ((timeSinceStart * frameRate / 20) % totalFrames).toInt()
            } else {
                ((timeSinceStart * frameRate / 20).toInt()).coerceAtMost(totalFrames - 1)
            }
            
            renderFrameForJukebox(jukeboxData, currentFrame)
        }
        
        // Remove stopped jukeboxes
        for (position in jukeboxesToRemove) {
            val jukeboxData = playingJukeboxes.remove(position)
            jukeboxData?.displayGroup = RenderGroup()
        }
    }
    
    private fun renderFrameForJukebox(jukeboxData: JukeboxData, currentFrame: Int) {
        if (currentFrame >= videoFrames.size) return
        
        val frame = videoFrames[currentFrame]
        val world = jukeboxData.world
        val jukeboxLocation = jukeboxData.jukebox.location
        
        // Position video above the jukebox
        val videoPosition = jukeboxLocation.toVector().add(
            Vector(
                MusicDiscConfig.xOffset, 
                MusicDiscConfig.heightAboveJukebox, 
                MusicDiscConfig.zOffset
            )
        )
        
        // Create a new display group for this frame
        jukeboxData.displayGroup = RenderGroup()
        
        for (y in 0 until videoHeight) {
            for (x in 0 until videoWidth) {
                val color = frame.sampleColor(
                    x.toFloat() / videoWidth,
                    y.toFloat() / videoHeight
                )
                
                // Skip transparent/black pixels
                if (color.alpha == 0 || (color.red == 0 && color.green == 0 && color.blue == 0)) {
                    continue
                }
                
                val offsetX = (x.toFloat() / videoWidth - 0.5f) * scale
                val offsetY = (y.toFloat() / videoHeight - 0.5f) * scale
                
                // Create transform for forward-facing display
                val transformForward = Matrix4f()
                    .translate(offsetX, offsetY, 0f)
                    .scale(scale / videoWidth, scale / videoHeight, 1f)
                    .mul(textDisplayUnitSquare)
                
                // Create forward-facing display
                jukeboxData.displayGroup["forward_$x" to y] = renderText(
                    world = world,
                    position = videoPosition,
                    init = {
                        it.text = " "
                        it.teleportDuration = MusicDiscConfig.teleportDuration
                        it.interpolationDuration = MusicDiscConfig.interpolationDuration
                        it.brightness = Display.Brightness(MusicDiscConfig.brightness, MusicDiscConfig.brightness)
                        if (MusicDiscConfig.billboard) {
                            it.billboard = Display.Billboard.CENTER
                        }
                    },
                    update = {
                        it.interpolateTransform(transformForward)
                        it.backgroundColor = color
                    }
                )
                
                // Create backward-facing display only if double-sided is enabled
                if (MusicDiscConfig.doubleSided) {
                    // Create transform for backward-facing display (rotated 180 degrees)
                    val transformBackward = Matrix4f()
                        .translate(offsetX, offsetY, 0f)
                        .rotateY(Math.PI.toFloat()) // Rotate 180 degrees around Y axis
                        .scale(scale / videoWidth, scale / videoHeight, 1f)
                        .mul(textDisplayUnitSquare)
                    
                    jukeboxData.displayGroup["backward_$x" to y] = renderText(
                        world = world,
                        position = videoPosition,
                        init = {
                            it.text = " "
                            it.teleportDuration = MusicDiscConfig.teleportDuration
                            it.interpolationDuration = MusicDiscConfig.interpolationDuration
                            it.brightness = Display.Brightness(MusicDiscConfig.brightness, MusicDiscConfig.brightness)
                            if (MusicDiscConfig.billboard) {
                                it.billboard = Display.Billboard.CENTER
                            }
                        },
                        update = {
                            it.interpolateTransform(transformBackward)
                            it.backgroundColor = color
                        }
                    )
                }
            }
        }
        
        jukeboxData.displayGroup.submit("bad_apple_video_${jukeboxLocation.x}_${jukeboxLocation.y}_${jukeboxLocation.z}")
    }
}

fun setupBadApplePlayer() {
    // Load configuration
    MusicDiscConfig.load()
    
    val badApplePlayer = BadApplePlayer()
    
    // Add custom music disc to items registry if enabled
    if (MusicDiscConfig.useCustomDisc) {
        val disc = Disc.createDisc()
        com.heledron.hologram.utilities.custom_items.customItemRegistry.add(disc)
        println("Music disc added to items registry")
    }
    
    // Listen for disc insertion into jukebox
    addEventListener(object : org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
        fun onJukeboxInteract(event: PlayerInteractEvent) {
            val player = event.player
            val itemInHand = player.inventory.itemInMainHand
            val clickedBlock = event.clickedBlock
            
            // Check if clicking on a jukebox with music disc
            if (event.action == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK &&
                clickedBlock?.type == Material.JUKEBOX &&
                Disc.isDisc(itemInHand)) {
                
                // Let the vanilla jukebox handle the disc insertion
                // We'll check for the disc in our update loop
                player.sendMessage("§aMusic disc inserted! Video will start playing above the jukebox.")
                
                // Schedule a check after the disc is inserted
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    com.heledron.hologram.utilities.currentPlugin,
                    java.lang.Runnable {
                        val jukebox = clickedBlock.state as? Jukebox
                        if (jukebox != null) {
                            // Check if jukebox is playing our disc (custom or regular if accepted)
                            val isPlayingOurDisc = jukebox.playing == Material.MUSIC_DISC_BLOCKS && 
                                (jukebox.inventory.any { itemStack -> 
                                    itemStack != null && Disc.isDisc(itemStack) 
                                } || MusicDiscConfig.acceptRegularDiscs)
                            
                            if (isPlayingOurDisc) {
                                badApplePlayer.startJukeboxVideo(jukebox)
                            }
                        }
                    },
                    2L // Check after 2 ticks to ensure the disc is inserted
                )
            }
        }
    })
    
    // Listen for jukebox breaking to stop video
    addEventListener(object : org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
        fun onJukeboxBreak(event: BlockBreakEvent) {
            if (event.block.type == Material.JUKEBOX) {
                val jukebox = event.block.state as? Jukebox
                if (jukebox != null) {
                    badApplePlayer.stopJukeboxVideo(jukebox)
                }
            }
        }
    })
    
    // Listen for disc ejection (when jukebox is clicked without a music disc in hand)
    addEventListener(object : org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
        fun onJukeboxEject(event: PlayerInteractEvent) {
            val player = event.player
            val clickedBlock = event.clickedBlock
            
            // Check if clicking on a jukebox without a music disc in hand (ejecting)
            if (event.action == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK &&
                clickedBlock?.type == Material.JUKEBOX &&
                !Disc.isDisc(player.inventory.itemInMainHand)) {
                
                // Schedule a check after the disc is ejected
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    com.heledron.hologram.utilities.currentPlugin,
                    java.lang.Runnable {
                        val jukebox = clickedBlock.state as? Jukebox
                        if (jukebox != null && jukebox.playing != Material.MUSIC_DISC_BLOCKS) {
                            badApplePlayer.stopJukeboxVideo(jukebox)
                        }
                    },
                    2L // Check after 2 ticks to ensure the disc is ejected
                )
            }
        }
    })
    
    // Listen for inventory move events to detect automatic disc insertion by droppers
    addEventListener(object : org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
        fun onInventoryMoveItem(event: org.bukkit.event.inventory.InventoryMoveItemEvent) {
            // Only process if dropper detection is enabled
            if (!MusicDiscConfig.detectDropperInsertion) return
            
            val source = event.source
            val destination = event.destination
            val item = event.item
            
            // Check if an item is being moved from a dropper to a jukebox
            if (source.holder is org.bukkit.block.Dropper && 
                destination.holder is Jukebox &&
                (Disc.isDisc(item) || (MusicDiscConfig.acceptRegularDiscs && item.type == Material.MUSIC_DISC_BLOCKS))) {
                
                println("=== INVENTORY MOVE DEBUG ===")
                println("Item moved from dropper to jukebox: ${item.type}")
                println("Source: ${source.holder}")
                println("Destination: ${destination.holder}")
                
                val jukebox = destination.holder as Jukebox
                println("Jukebox location: ${jukebox.location}")
                println("Jukebox playing: ${jukebox.playing}")
                println("Jukebox has record: ${jukebox.hasRecord()}")
                println("Jukebox is playing: ${jukebox.isPlaying()}")
                println("Jukebox inventory contents:")
                jukebox.inventory.forEachIndexed { index, itemStack ->
                    if (itemStack != null) {
                        println("  Slot $index: ${itemStack.type}")
                    }
                }
                
                // Schedule a check after the item is moved
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    com.heledron.hologram.utilities.currentPlugin,
                    java.lang.Runnable {
                        println("=== CHECKING AFTER MOVE ===")
                        println("Jukebox playing: ${jukebox.playing}")
                        println("Jukebox has record: ${jukebox.hasRecord()}")
                        println("Jukebox is playing: ${jukebox.isPlaying()}")
                        println("Jukebox inventory contents after move:")
                        jukebox.inventory.forEachIndexed { index, itemStack ->
                            if (itemStack != null) {
                                println("  Slot $index: ${itemStack.type}")
                            }
                        }
                        
                        // Check if jukebox inventory contains our disc and is playing
                        val hasOurDisc = jukebox.inventory.any { itemStack ->
                            itemStack != null && (Disc.isDisc(itemStack) || 
                                (MusicDiscConfig.acceptRegularDiscs && itemStack.type == Material.MUSIC_DISC_BLOCKS))
                        }
                        
                        if (hasOurDisc && jukebox.isPlaying()) {
                            badApplePlayer.startJukeboxVideo(jukebox)
                            println("Dropper inserted music disc into jukebox at ${jukebox.location}")
                        } else {
                            println("Jukebox does not have our disc or is not playing. Has disc: $hasOurDisc, is playing: ${jukebox.isPlaying()}")
                        }
                        println("=== CHECKING AFTER MOVE END ===")
                    },
                    2L // Check after 2 ticks to ensure the item is moved
                )
                println("=== INVENTORY MOVE DEBUG END ===")
            }
        }
    })
    
    // Update the video player every tick
    onTick {
        badApplePlayer.update()
    }
} 