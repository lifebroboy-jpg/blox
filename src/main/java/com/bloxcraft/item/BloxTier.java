package com.bloxcraft.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public record BloxTier(int uses, float speed, float damage, int ench) implements Tier {
    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return damage; }
    @Override public TagKey<Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_NETHERITE_TOOL; }
    @Override public int getEnchantmentValue() { return ench; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
}
