package com.heledron.hologram.bad_apple

import com.heledron.hologram.utilities.namespacedID
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

object Disc {
    private val discKey = namespacedID("music_disc")
    
    fun createDisc(): ItemStack {
        val disc = ItemStack(Material.MUSIC_DISC_13) // Use MUSIC_DISC_13 as base
        val meta = disc.itemMeta ?: return disc
        
        // Set custom name and lore
        meta.setDisplayName(MusicDiscConfig.customDiscName)
        meta.lore = MusicDiscConfig.customDiscLore
        
        // Add custom identifier
        meta.persistentDataContainer.set(discKey, PersistentDataType.BOOLEAN, true)
        
        disc.itemMeta = meta
        return disc
    }
    
    fun isDisc(item: ItemStack): Boolean {
        if (item.type != Material.MUSIC_DISC_13) return false
        
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.get(discKey, PersistentDataType.BOOLEAN) == true
    }
    
    fun getDiscKey(): NamespacedKey = discKey
    // (audio logic removed)
} 