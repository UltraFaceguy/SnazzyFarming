package com.faceguy.snazzy.farming;

import com.faceguy.snazzy.farming.listener.CropBreakListener;
import com.faceguy.snazzy.farming.manager.CropLootManager;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import info.faceland.strife.StrifePlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class SnazzyFarmingPlugin extends FacePlugin {

  private StrifePlugin strifePlugin;
  private CropLootManager cropLootManager;

  @Override
  public void enable() {
    strifePlugin = (StrifePlugin) getServer().getPluginManager().getPlugin("Strife");
    cropLootManager = new CropLootManager();
    Bukkit.getPluginManager().registerEvents(new CropBreakListener(this), this);
  }

  @Override
  public void disable() {
    HandlerList.unregisterAll(this);
  }

  public StrifePlugin getStrifePlugin() {
    return strifePlugin;
  }

  public CropLootManager getCropManager() {
    return cropLootManager;
  }
}
