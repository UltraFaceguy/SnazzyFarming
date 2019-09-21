package com.faceguy.snazzy.farming.data;

import org.bukkit.Material;

public class CropDropData {

  private Material material;
  private double chance;
  private int min;
  private int max;
  private double qualityChance;
  private double qualityChancePerLevel;
  private double bonusMin;
  private double bonusMax;

  public Material getMaterial() {
    return material;
  }

  public void setMaterial(Material material) {
    this.material = material;
  }

  public double getChance() {
    return chance;
  }

  public void setChance(double chance) {
    this.chance = chance;
  }

  public int getMin() {
    return min;
  }

  public void setMin(int min) {
    this.min = min;
  }

  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public double getQualityChance() {
    return qualityChance;
  }

  public void setQualityChance(double qualityChance) {
    this.qualityChance = qualityChance;
  }

  public double getQualityChancePerLevel() {
    return qualityChancePerLevel;
  }

  public void setQualityChancePerLevel(double qualityChancePerLevel) {
    this.qualityChancePerLevel = qualityChancePerLevel;
  }

  public double getBonusMin() {
    return bonusMin;
  }

  public void setBonusMin(double bonusMin) {
    this.bonusMin = bonusMin;
  }

  public double getBonusMax() {
    return bonusMax;
  }

  public void setBonusMax(double bonusMax) {
    this.bonusMax = bonusMax;
  }
}
