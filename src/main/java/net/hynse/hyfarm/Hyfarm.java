package net.hynse.hyfarm;

import net.hynse.hyfarm.listeners.CropBreakListener;

import me.nahu.scheduler.wrapper.FoliaWrappedJavaPlugin;
import me.nahu.scheduler.wrapper.WrappedScheduler;

public final class Hyfarm extends FoliaWrappedJavaPlugin {

    public static Hyfarm instance;
    public WrappedScheduler scheduler;
    
    @Override
    public void onEnable() {
        instance = this;
        scheduler = getScheduler();
        getServer().getPluginManager().registerEvents(new CropBreakListener(), this);
        getLogger().info("HyFarm has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("HyFarm has been disabled!");
    }
}
