package com.heledron.display_disk.music_disc

import com.heledron.hologram.utilities.namespacedID
import com.heledron.hologram.bad_apple.MusicDiscConfig
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

object Disc {
    private val discKey = namespacedID("music_disc")
    
    fun createDisc(): ItemStack {
        val disc = ItemStack(Material.MUSIC_DISC_BLOCKS) // Use MUSIC_DISC_BLOCKS as base
        val meta = disc.itemMeta
        
        if (meta != null) {
            meta.setDisplayName("§6Bad Apple Music Disc")
            meta.lore = listOf(
                "§7A mysterious music disc",
                "§7that plays a special video",
                "§7when inserted into a jukebox."
            )
            meta.persistentDataContainer.set(discKey, PersistentDataType.BOOLEAN, true)
            disc.itemMeta = meta
        }
        
        return disc
    }
    
    fun isDisc(item: ItemStack): Boolean {
        if (item.type != Material.MUSIC_DISC_BLOCKS) return false
        
        // If acceptRegularDiscs is enabled, accept any MUSIC_DISC_BLOCKS
        if (MusicDiscConfig.acceptRegularDiscs) return true
        
        // Otherwise, only accept custom discs with our identifier
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.get(discKey, PersistentDataType.BOOLEAN) == true
    }
    
    fun getDiscKey(): NamespacedKey = discKey
    // (audio logic removed)
} 