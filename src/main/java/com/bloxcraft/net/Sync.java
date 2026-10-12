package com.bloxcraft.net;

import com.bloxcraft.data.PlayerStats;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class Sync {
    private Sync() {}

    public static void sync(ServerPlayer p) {
        PlayerStats s = PlayerStats.get(p);
        long now = p.level().getGameTime();
        CompoundTag t = new CompoundTag();
        t.putInt("level", s.level);
        t.putLong("xp", s.xp);
        t.putLong("need", PlayerStats.xpFor(s.level));
        t.putLong("beli", s.beli);
        t.putString("fruit", s.fruit);
        t.putInt("mastery", s.mastery);
        t.putDouble("energy", s.energy);
        t.putDouble("max", s.maxEnergy());
        t.putString("quest", s.quest);
        t.putInt("qp", s.questProgress);
        for (int i = 0; i < 3; i++) t.putInt("cd" + i, (int) Math.max(0, s.ready[i] - now));
        PacketDistributor.sendToPlayer(p, new StatsPayload(t));
    }
}
