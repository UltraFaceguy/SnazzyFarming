package com.faceguy.snazzy.farming.manager;

import com.faceguy.snazzy.farming.data.CropDropData;
import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.data.champion.LifeSkillType;
import info.faceland.strife.util.PlayerDataUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CropLootManager {

  private Map<Material, List<CropDropData>> dropsMap = new HashMap<>();
  private String genericName;
  private List<String> genericLore;

  public void addDrops(Material material, List<CropDropData> dropList) {
    dropsMap.put(material, dropList);
  }

  public void spawnCropDrops(Player player, Block block) {
    double level = PlayerDataUtil.getEffectiveLifeSkill(player, LifeSkillType.FARMING, true);
    if (!dropsMap.containsKey(block.getType())) {
      return;
    }
    for (CropDropData data : dropsMap.get(block.getType())) {
      if (Math.random() > data.getChance()) {
        continue;
      }
      double min = data.getMin() + level * data.getBonusMin();
      double max = data.getMax() + level * data.getBonusMax();
      if (min % 1 > Math.random()) {
        min++;
      }
      if (max % 1 > Math.random()) {
        max++;
      }
      min = Math.floor(min);
      max = Math.max(Math.floor(max), min);
      int amount = (int) (min + Math.random() * (max - min));
      int quality = 1;
      while (Math.random() < data.getQualityChance() + level * data.getQualityChancePerLevel()
          && quality < 5) {
        quality++;
      }
      ItemStack stack = new ItemStack(data.getMaterial(), amount);
      if (quality > 1) {
        String name = genericName.replace("{quality-color}", "" + getQualityColor(quality));
        name = name.replace("{name}",
            WordUtils.capitalize(data.getMaterial().toString().toLowerCase().replaceAll("_", " ")));

        List<String> lore = new ArrayList<>();
        for (String s : genericLore) {
          s = s.replace("{quality-color}", "" + getQualityColor(quality));
          s = s.replace("{quality-stars}", getQualityStars(quality));
          lore.add(s);
        }
        ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color(name));
        ItemStackExtensionsKt.setLore(stack, TextUtils.color(lore));
      }
      block.getWorld().dropItemNaturally(block.getLocation(), stack);
    }
  }

  public boolean isFarmingHandled(Material material) {
    switch (material) {
      case WHEAT:
      case BAMBOO:
      case COCOA:
      case BEETROOTS:
      case POTATOES:
      case CARROTS:
      case NETHER_WART_BLOCK:
      case SWEET_BERRY_BUSH:
      case CACTUS:
      case MELON:
      case PUMPKIN:
      case SUGAR_CANE:
        return true;
      default:
        return false;
    }
  }

  private String getQualityStars(int quality) {
    return IntStream.range(0, quality).mapToObj(i -> "✪").collect(Collectors.joining(""));
  }

  private ChatColor getQualityColor(int i) {
    switch (i) {
      case 1:
        return ChatColor.WHITE;
      case 2:
        return ChatColor.BLUE;
      case 3:
        return ChatColor.DARK_PURPLE;
      case 4:
        return ChatColor.RED;
      case 5:
        return ChatColor.GOLD;
      default:
        return ChatColor.BLACK;
    }
  }

  public boolean isConfiguredForFarming(Material material) {
    return dropsMap.containsKey(material);
  }

  public void setGenericName(String genericName) {
    this.genericName = genericName;
  }

  public void setGenericList(List<String> genericLore) {
    this.genericLore = genericLore;
  }

  public void loadCroptions(ConfigurationSection croptions) {
    if (croptions == null) {
      Bukkit.getLogger().warning("Croptions is null or empty! Failed to load drops!");
      return;
    }
    for (String blockMaterial : croptions.getKeys(false)) {
      Material material = Material.getMaterial(blockMaterial);
      if (!isFarmingHandled(material)) {
        System.out.println("BlockType " + material + " isn't handled by farming.");
        continue;
      }
      ConfigurationSection dropNameSection = croptions.getConfigurationSection(blockMaterial);
      List<CropDropData> dataList = new ArrayList<>();
      for (String dropKey : dropNameSection.getKeys(false)) {
        ConfigurationSection dropSection = dropNameSection.getConfigurationSection(dropKey);
        if (dropSection == null) {
          continue;
        }
        CropDropData data = new CropDropData();
        Material dataMaterial = Material.valueOf(dropSection.getString("material", ""));
        data.setMaterial(dataMaterial);
        data.setChance(dropSection.getDouble("chance", 0.5));
        data.setMin(dropSection.getInt("min-amount", 1));
        data.setMax(dropSection.getInt("max-amount", 2));
        data.setBonusMin(dropSection.getInt("min-bonus-per-level", 0));
        data.setBonusMax(dropSection.getInt("max-bonus-per-level", 0));
        data.setQualityChance(dropSection.getDouble("quality-increase-chance", 0.1));
        data.setQualityChancePerLevel(
            dropSection.getDouble("quality-increase-chance-per-level", 0.1));
        Bukkit.getLogger().info("Loaded item for " + material + ": " + dropKey);
        dataList.add(data);
      }
      addDrops(material, dataList);
    }
  }
}
