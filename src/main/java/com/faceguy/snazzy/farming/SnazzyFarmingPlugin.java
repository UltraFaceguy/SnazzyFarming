package com.faceguy.snazzy.farming;

import com.faceguy.snazzy.farming.listener.CropListener;
import com.faceguy.snazzy.farming.manager.CropLootManager;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import info.faceland.strife.StrifePlugin;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;

public class SnazzyFarmingPlugin extends FacePlugin {

  private StrifePlugin strifePlugin;
  private MasterConfiguration settings;
  private VersionedSmartYamlConfiguration configYAML;
  private CropLootManager cropLootManager;

  @Override
  public void enable() {
    strifePlugin = (StrifePlugin) getServer().getPluginManager().getPlugin("Strife");

    configYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "config.yml"),
        getResource("config.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);

    if (configYAML.update()) {
      Bukkit.getLogger().info("Updating " + configYAML.getFileName());
    }

    settings = MasterConfiguration.loadFromFiles(configYAML);

    cropLootManager = new CropLootManager();
    cropLootManager.setGenericName(settings.getString("config.generic-crop-format.name"));
    cropLootManager.setGenericList(settings.getStringList("config.generic-crop-format.lore"));

    ConfigurationSection croptions = configYAML.getConfigurationSection("croptions");
    cropLootManager.loadCroptions(croptions);

    Bukkit.getPluginManager().registerEvents(new CropListener(this), this);
  }

  @Override
  public void disable() {
    HandlerList.unregisterAll(this);
  }

  public VersionedSmartYamlConfiguration getBaseConfig() {
    return configYAML;
  }

  public StrifePlugin getStrifePlugin() {
    return strifePlugin;
  }

  public CropLootManager getCropManager() {
    return cropLootManager;
  }
}
