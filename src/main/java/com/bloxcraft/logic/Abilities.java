package com.bloxcraft.logic;

import com.bloxcraft.data.Ability;
import com.bloxcraft.data.Fruit;
import com.bloxcraft.data.PlayerStats;
import com.bloxcraft.net.Sync;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class Abilities {
    private Abilities() {}

    public static void cast(ServerPlayer p, int slot) {
        if (p.isSpectator() || !p.isAlive()) return;
        PlayerStats s = PlayerStats.get(p);
        Fruit f = Fruit.byId(s.fruit);
        if (f == null) {
            p.displayClientMessage(Component.literal("§cУ вас нет силы фрукта. Присядьте и нажмите ПКМ с фруктом в руке."), true);
            return;
        }
        if (slot < 0 || slot >= f.abilities.length) return;
        Ability a = f.abilities[slot];
        if (s.mastery < a.minMastery()) {
            p.displayClientMessage(Component.literal("§cНужно мастерство фрукта: " + a.minMastery() + " (у вас " + s.mastery + ")"), true);
            return;
        }
        long now = p.level().getGameTime();
        if (now < s.ready[slot]) return;
        if (s.energy < a.cost()) {
            p.displayClientMessage(Component.literal("§9Не хватает энергии (" + a.cost() + ")"), true);
            return;
        }
        s.energy -= a.cost();
        s.ready[slot] = now + a.cooldown();
        a.cast().cast(p, s.power());
        s.mastery = Math.min(1000, s.mastery + 1);
        Sync.sync(p);
    }
}
