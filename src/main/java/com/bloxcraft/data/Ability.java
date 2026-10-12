package com.bloxcraft.data;

import net.minecraft.server.level.ServerPlayer;

public record Ability(String name, int cost, int cooldown, int minMastery, Cast cast) {
    @FunctionalInterface
    public interface Cast {
        void cast(ServerPlayer player, double power);
    }
}
