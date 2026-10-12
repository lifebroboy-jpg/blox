package com.bloxcraft.logic;

import com.bloxcraft.BloxCraft;
import com.bloxcraft.data.Fruit;
import com.bloxcraft.data.PlayerStats;
import com.bloxcraft.net.Sync;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class Progression {
    private Progression() {}

    public static void addXp(ServerPlayer p, long amount) {
        if (amount <= 0) return;
        PlayerStats s = PlayerStats.get(p);
        if (s.level >= PlayerStats.MAX_LEVEL) { Sync.sync(p); return; }
        s.xp += amount;
        boolean up = false;
        while (s.level < PlayerStats.MAX_LEVEL && s.xp >= PlayerStats.xpFor(s.level)) {
            s.xp -= PlayerStats.xpFor(s.level);
            s.level++;
            up = true;
        }
        if (up) {
            applyAttributes(p);
            s.energy = s.maxEnergy();
            p.sendSystemMessage(Component.literal("§6★ Новый уровень! §eУр. " + s.level));
            p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f);
        }
        Sync.sync(p);
    }

    public static void addBeli(ServerPlayer p, long amount) {
        PlayerStats s = PlayerStats.get(p);
        s.beli += amount;
        Sync.sync(p);
    }

    /** Бонус к максимальному здоровью от уровня. */
    public static void applyAttributes(ServerPlayer p) {
        PlayerStats s = PlayerStats.get(p);
        AttributeInstance a = p.getAttribute(Attributes.MAX_HEALTH);
        if (a != null) {
            a.addOrReplacePermanentModifier(new AttributeModifier(
                    BloxCraft.id("level_health"), s.level * 0.05, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void eat(ServerPlayer p, Fruit f) {
        PlayerStats s = PlayerStats.get(p);
        s.fruit = f.id;
        s.mastery = 0;
        for (int i = 0; i < s.ready.length; i++) s.ready[i] = 0;
        Sync.sync(p);
    }
}
