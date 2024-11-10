package net.hynse.hyfarm.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.event.block.Action;

import net.hynse.hyfarm.Hyfarm;
import java.util.concurrent.ThreadLocalRandom;
import java.util.EnumMap;
import java.util.EnumSet;

public class CropBreakListener implements Listener {
    
    private static final String PLACED_META = "hyfarm_placed";
    private static final double OFFSET_XY = 0.5;
    private static final double OFFSET_Y = 0.3;
    
    private static final EnumSet<Material> TRACK_PLACED = EnumSet.of(
        Material.MELON,
        Material.PUMPKIN,
        Material.GLOW_BERRIES,
        Material.SWEET_BERRY_BUSH
    );
    
    private static final EnumSet<Material> HARVESTABLE_BERRIES = EnumSet.of(
        Material.SWEET_BERRY_BUSH,
        Material.GLOW_BERRIES
    );
    
    private static final class XPRange {
        final int min;
        final int max;
        
        XPRange(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }
    
    private static final EnumMap<Material, XPRange> CROP_XP_RANGES = new EnumMap<>(Material.class);
    
    static {
        // Basic crops - lower XP due to easy farming
        CROP_XP_RANGES.put(Material.WHEAT, new XPRange(1, 3));
        CROP_XP_RANGES.put(Material.CARROTS, new XPRange(1, 3));
        CROP_XP_RANGES.put(Material.POTATOES, new XPRange(1, 3));
        
        // Medium tier crops - slightly more XP
        CROP_XP_RANGES.put(Material.BEETROOTS, new XPRange(2, 4));
        CROP_XP_RANGES.put(Material.SWEET_BERRY_BUSH, new XPRange(1, 2));
        CROP_XP_RANGES.put(Material.GLOW_BERRIES, new XPRange(1, 2));
        
        // Special crops - more XP due to unique mechanics
        CROP_XP_RANGES.put(Material.MELON, new XPRange(1, 3));
        CROP_XP_RANGES.put(Material.PUMPKIN, new XPRange(1, 3));
        
        // Nether crops - highest XP due to nether difficulty
        CROP_XP_RANGES.put(Material.NETHER_WART, new XPRange(2, 5));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Material type = event.getBlock().getType();
        if (TRACK_PLACED.contains(type)) {
            event.getBlock().setMetadata(PLACED_META, new FixedMetadataValue(Hyfarm.instance, true));
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(event.getPlayer() instanceof Player)) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        if (!HARVESTABLE_BERRIES.contains(block.getType())) return;
        
        Ageable berries = (Ageable) block.getBlockData();
        if (berries.getAge() != berries.getMaximumAge()) return;
        
        XPRange xpRange = CROP_XP_RANGES.get(block.getType());
        if (xpRange == null) return;
        
        // Schedule XP spawn after the berries are harvested
        Hyfarm.instance.scheduler.runTaskLater(() -> {
            spawnXPOrb(block, xpRange);
        }, 1L);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCropBreak(BlockBreakEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Block block = event.getBlock();
        
        XPRange xpRange = CROP_XP_RANGES.get(block.getType());
        if (xpRange == null) return;
        
        if (block.hasMetadata(PLACED_META)) return;
        
        if (block.getBlockData() instanceof Ageable) {
            Ageable crop = (Ageable) block.getBlockData();
            if (crop.getAge() != crop.getMaximumAge()) return;
        }
        
        spawnXPOrb(block, xpRange);
    }
    
    private void spawnXPOrb(Block block, XPRange xpRange) {
        Hyfarm.instance.scheduler.runTaskLater(() -> {
            int xpAmount = ThreadLocalRandom.current().nextInt(xpRange.min, xpRange.max + 1);
            block.getWorld().spawn(
                block.getLocation().add(OFFSET_XY, OFFSET_Y, OFFSET_XY),
                ExperienceOrb.class,
                orb -> orb.setExperience(xpAmount)
            );
        }, 1L);
    }
}