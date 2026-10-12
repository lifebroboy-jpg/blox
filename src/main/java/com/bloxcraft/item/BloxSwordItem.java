package com.bloxcraft.item;

import com.bloxcraft.data.PlayerStats;
import com.bloxcraft.data.SwordType;
import com.bloxcraft.logic.Fx;
import com.bloxcraft.net.Sync;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BloxSwordItem extends SwordItem {
    public final SwordType type;

    public BloxSwordItem(SwordType t) {
        super(t.tier, new Item.Properties()
                .attributes(SwordItem.createAttributes(t.tier, 3, t.speed))
                .rarity(t.minLevel >= 400 ? net.minecraft.world.item.Rarity.EPIC
                        : t.minLevel >= 90 ? net.minecraft.world.item.Rarity.RARE
                        : net.minecraft.world.item.Rarity.UNCOMMON));
        this.type = t;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean r = super.hurtEnemy(stack, target, attacker);
        if (target.level() instanceof ServerLevel sl) {
            switch (type.effect) {
                case SLOW -> Fx.effect(target, MobEffects.MOVEMENT_SLOWDOWN, 60, 1);
                case FROST -> {
                    Fx.effect(target, MobEffects.MOVEMENT_SLOWDOWN, 80, 2);
                    target.setTicksFrozen(160);
                }
                case LIGHTNING -> {
                    if (attacker.getRandom().nextFloat() < 0.25f) {
                        LightningBolt b = EntityType.LIGHTNING_BOLT.create(sl);
                        if (b != null) {
                            b.moveTo(target.position());
                            sl.addFreshEntity(b);
                        }
                    }
                }
                case FIRE -> target.setRemainingFireTicks(100);
                case WITHER -> Fx.effect(target, MobEffects.WITHER, 100, 1);
                case LIFESTEAL -> {
                    Fx.effect(target, MobEffects.WITHER, 80, 1);
                    attacker.heal(2.0f);
                }
                default -> { }
            }
        }
        return r;
    }

    /** ПКМ — волна рубящего удара за 30 энергии. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack st = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            PlayerStats s = PlayerStats.get(sp);
            if (s.level < type.minLevel) {
                sp.displayClientMessage(Component.literal("§cТребуется уровень " + type.minLevel), true);
                return InteractionResultHolder.fail(st);
            }
            if (s.energy < 30) {
                sp.displayClientMessage(Component.literal("§9Не хватает энергии (30)"), true);
                return InteractionResultHolder.fail(st);
            }
            s.energy -= 30;
            sp.getCooldowns().addCooldown(this, 50);
            Fx.bolt(sp, 9, 1.6, type.slash * s.power(), ParticleTypes.SWEEP_ATTACK, 0, 0.8);
            level.playSound(null, sp.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1f, 0.8f);
            st.hurtAndBreak(2, sp, EquipmentSlot.MAINHAND);
            Sync.sync(sp);
        }
        return InteractionResultHolder.sidedSuccess(st, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§7Требуется уровень: §f" + type.minLevel));
        tip.add(Component.literal("§7Эффект удара: §f" + type.effect.ru));
        tip.add(Component.literal("§9ПКМ: §fволна клинка (30 энергии)"));
    }
}
