package com.faceguy.snazzy.farming.listener;

import com.faceguy.snazzy.farming.SnazzyFarmingPlugin;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.data.champion.LifeSkillType;
import info.faceland.strife.util.PlayerDataUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CropListener implements Listener {

  private SnazzyFarmingPlugin plugin;
  private String farmingInfo;
  private String farmingNoPlace;

  private static final Map<UUID, Long> FARMING_NO_SPAM = new HashMap<>();
  private static final int FARMING_SPAM_CD = 10000;

  private double perPillarExp;
  private double perStageExp;
  private double perBlockExp;
  private double fertilizeFalloffLevel;
  private double baseFertilizeExp;
  private double fertilizeFailMult;

  public CropListener(SnazzyFarmingPlugin plugin) {
    this.plugin = plugin;
    this.farmingInfo = "&eBreak fully grown crops to harvest them, and auto-replant. "
        + "To remove crops, break them with a hoe!";
    this.farmingNoPlace = "&eSorry, you have to wait for it to grow naturally!";
    perPillarExp = plugin.getBaseConfig().getDouble("experience.per-pillar-height", 1);
    perStageExp = plugin.getBaseConfig().getDouble("experience.per-growth-stage", 1);
    perBlockExp = plugin.getBaseConfig().getDouble("experience.per-block-crop", 1);
    fertilizeFalloffLevel = plugin.getBaseConfig().getDouble("experience.fertilize-falloff-level", 100);
    baseFertilizeExp = plugin.getBaseConfig().getDouble("experience.base-fertilize-exp", 1);
    fertilizeFailMult = plugin.getBaseConfig().getDouble("experience.fertilize-failure-multiplier", 0.5);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCropPlace(BlockPlaceEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.getItemInHand().getType() == Material.BAMBOO) {
      if (event.getBlockPlaced().getRelative(BlockFace.DOWN).getType() == Material.BAMBOO ||
          event.getBlockPlaced().getRelative(BlockFace.DOWN).getType() == Material.BAMBOO_SAPLING) {
        event.setCancelled(true);
      }
    } else if (event.getItemInHand().getType() == Material.SUGAR_CANE) {
      if (event.getBlockPlaced().getRelative(BlockFace.DOWN).getType() == Material.SUGAR_CANE) {
        event.setCancelled(true);
      }
    } else if (event.getItemInHand().getType() == Material.CACTUS) {
      if (event.getBlockPlaced().getRelative(BlockFace.DOWN).getType() == Material.CACTUS) {
        event.setCancelled(true);
      }
    }
    if (event.isCancelled()) {
      event.getBlockPlaced().getWorld().spawnParticle(Particle.SMOKE_NORMAL,
          event.getBlockPlaced().getLocation(), 8, 0.25, 0.25, 0.25, 0);
      sendFarmingSpam(event.getPlayer(), farmingNoPlace);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCropFertilize(BlockFertilizeEvent event) {
    if (!plugin.getCropManager().isFarmingHandled(event.getBlock().getType()) || event.getBlocks()
        .isEmpty()) {
      return;
    }
    double level = PlayerDataUtil.getLifeSkillLevel(event.getPlayer(), LifeSkillType.FARMING);
    double effectiveLevel = PlayerDataUtil.getEffectiveLifeSkill(event.getPlayer(),
        LifeSkillType.FARMING, true);
    double exp = baseFertilizeExp;
    exp *= 1 - (level / fertilizeFalloffLevel);
    if (Math.random() > 0.4 + 0.015 * effectiveLevel) {
      event.getBlocks().clear();
      exp *= fertilizeFailMult;
    }
    if (exp > 0) {
      plugin.getStrifePlugin().getSkillExperienceManager().addExperience(
          event.getPlayer(), LifeSkillType.FARMING, exp, false);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBerryHarvest(PlayerInteractEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.getClickedBlock() != null
        && event.getClickedBlock().getType() != Material.SWEET_BERRY_BUSH) {
      return;
    }
    Ageable cropData = (Ageable) event.getClickedBlock().getBlockData();
    if (cropData.getAge() > 1) {
      if (cropData.getAge() == 3 || cropData.getAge() == 2 && Math.random() > 0.5) {
        plugin.getStrifePlugin().getSkillExperienceManager().addExperience(
            event.getPlayer(), LifeSkillType.FARMING, cropData.getAge() * perStageExp, false);
        plugin.getCropManager().spawnCropDrops(event.getPlayer(), event.getClickedBlock());
      }
      event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(),
          Sound.ITEM_SWEET_BERRIES_PICK_FROM_BUSH, 1, 1);
      cropData.setAge(1);
      event.getClickedBlock().setBlockData(cropData);
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCropBreak(BlockBreakEvent event) {

    BlockData data = event.getBlock().getBlockData();
    if (!plugin.getCropManager().isFarmingHandled(data.getMaterial())) {
      return;
    }
    if (!plugin.getCropManager().isConfiguredForFarming(data.getMaterial())) {
      return;
    }

    event.setCancelled(true);
    event.setDropItems(false);

    if (data.getMaterial() == Material.SUGAR_CANE || data.getMaterial() == Material.BAMBOO ||
        data.getMaterial() == Material.CACTUS) {
      Set<Block> pillarBlocks = new HashSet<>();
      Block currblock = event.getBlock();
      if (event.getBlock().getRelative(BlockFace.DOWN).getType() != data.getMaterial()) {
        currblock = currblock.getRelative(BlockFace.UP);
      }
      while (currblock.getType() == data.getMaterial() && pillarBlocks.size() < 10) {
        pillarBlocks.add(currblock);
        currblock = currblock.getRelative(BlockFace.UP);
      }
      if (pillarBlocks.isEmpty()) {
        event.setCancelled(false);
        event.setDropItems(true);
        return;
      }
      for (Block b : pillarBlocks) {
        plugin.getCropManager().spawnCropDrops(event.getPlayer(), b);
        b.getWorld().spawnParticle(Particle.BLOCK_CRACK,
            b.getLocation().add(0.5, 0.5, 0.5), 10, 0, 0, 0, b.getBlockData());
        b.setType(Material.AIR);
      }
      plugin.getStrifePlugin().getSkillExperienceManager().addExperience(event.getPlayer(),
          LifeSkillType.FARMING, pillarBlocks.size() * perPillarExp, false);
      return;
    }

    if (data.getMaterial() == Material.PUMPKIN || data.getMaterial() == Material.MELON) {
      if (!isStemAttached(event.getBlock())) {
        event.setCancelled(false);
        event.setDropItems(true);
        return;
      }
      plugin.getStrifePlugin().getSkillExperienceManager()
          .addExperience(event.getPlayer(), LifeSkillType.FARMING, perBlockExp, false);
      plugin.getCropManager().spawnCropDrops(event.getPlayer(), event.getBlock());
      event.getBlock().getWorld().spawnParticle(Particle.BLOCK_CRACK,
          event.getBlock().getLocation().add(0.5, 0.5, 0.5), 10, 0, 0, 0, data);
      event.getBlock().setType(Material.AIR);
      return;
    }

    Ageable cropData = (Ageable) data;
    if (cropData.getAge() != cropData.getMaximumAge()) {
      cropData.setAge(0);
      event.getBlock().setBlockData(cropData);
      if (isHoldingHoe(event.getPlayer())) {
        event.setCancelled(false);
        event.setDropItems(true);
      } else {
        sendFarmingSpam(event.getPlayer(), farmingInfo);
      }
      return;
    }

    cropData.setAge(0);
    event.getBlock().setBlockData(cropData);

    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(
        event.getPlayer(), LifeSkillType.FARMING, cropData.getMaximumAge() * perStageExp, false);
    plugin.getCropManager().spawnCropDrops(event.getPlayer(), event.getBlock());
  }

  private boolean isStemAttached(Block block) {
    Set<Block> cardinalBlocks = new HashSet<>();
    cardinalBlocks.add(block.getRelative(BlockFace.NORTH));
    cardinalBlocks.add(block.getRelative(BlockFace.EAST));
    cardinalBlocks.add(block.getRelative(BlockFace.WEST));
    cardinalBlocks.add(block.getRelative(BlockFace.SOUTH));
    for (Block b : cardinalBlocks) {
      if (block.getType() == Material.PUMPKIN && b.getType() != Material.ATTACHED_PUMPKIN_STEM) {
        continue;
      }
      if (block.getType() == Material.MELON && b.getType() != Material.ATTACHED_MELON_STEM) {
        continue;
      }
      Directional directionData = (Directional) b.getBlockData();
      if (b.getRelative(directionData.getFacing()).equals(block)) {
        return true;
      }
    }
    return false;
  }

  private boolean isHoldingHoe(Player p) {
    if (p.getEquipment() == null) {
      return false;
    }
    return isHoe(p.getEquipment().getItemInMainHand()) || isHoe(
        p.getEquipment().getItemInOffHand());
  }

  private boolean isHoe(ItemStack stack) {
    switch (stack.getType()) {
      case WOODEN_HOE:
      case STONE_HOE:
      case IRON_HOE:
      case GOLDEN_HOE:
      case DIAMOND_HOE:
        return true;
      default:
        return false;
    }
  }

  private void sendFarmingSpam(Player player, String message) {
    if (FARMING_NO_SPAM.containsKey(player.getUniqueId())
        && System.currentTimeMillis() - FARMING_NO_SPAM.get(player.getUniqueId())
        < FARMING_SPAM_CD) {
      return;
    }
    FARMING_NO_SPAM.put(player.getUniqueId(), System.currentTimeMillis());
    MessageUtils.sendMessage(player, message);
  }
}
