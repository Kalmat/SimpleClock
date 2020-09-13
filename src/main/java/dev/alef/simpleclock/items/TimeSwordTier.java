package dev.alef.simpleclock.items;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;

public enum TimeSwordTier implements IItemTier {

    time_sword(7.0f, 9.0f, 100000000, 0, 25, null);

    private float attackDamage, efficiency;
    private int maxUses, harvestLevel, enchantability;
    private Item repairMaterial;
    private static double knockback = 1.5;

    private TimeSwordTier(float attackDamage, float efficiency, int maxUses, int harvestLevel, int enchantability, Item repairMaterial) {
        this.attackDamage = attackDamage;
        this.efficiency = efficiency;
        this.maxUses = maxUses;
        this.harvestLevel = harvestLevel;
        this.enchantability = enchantability;
        this.repairMaterial = repairMaterial;
    }

    @Override
    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public float getEfficiency() {
        return this.efficiency;
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public int getHarvestLevel() {
        return this.harvestLevel;
    }

    @Override
    public int getMaxUses() {
        return this.maxUses;
    }

    @Override
    public Ingredient getRepairMaterial() {
        return Ingredient.fromItems(this.repairMaterial);
    }
    
    public static double getKnockback() {
    	return TimeSwordTier.knockback;
    }
}

