package com.faceguy.snazzy.farming.manager;

import info.faceland.strife.data.champion.LifeSkillType;
import info.faceland.strife.util.PlayerDataUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class CropLootManager {

  public void spawnCropDrops(Player player, Block block) {
    double level = PlayerDataUtil.getEffectiveLifeSkill(player, LifeSkillType.FARMING, true);
    switch (block.getType()) {
      case WHEAT:
      case COCOA:
      case BEETROOTS:
      case POTATOES:
      case CARROTS:
      case NETHER_WART_BLOCK:
      case SWEET_BERRY_BUSH:
      case CACTUS:
        return;
      default:
        return;
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
}
