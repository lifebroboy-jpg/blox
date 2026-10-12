package com.bloxcraft.data;

import com.bloxcraft.registry.ModAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** Прогресс игрока: уровень, опыт, белли, фрукт, мастерство, энергия, квест. */
public class PlayerStats implements INBTSerializable<CompoundTag> {
    public static final int MAX_LEVEL = 700;

    public int level = 1;
    public long xp = 0;
    public long beli = 0;
    public String fruit = "";
    public int mastery = 0;
    public double energy = 100;
    public String quest = "";
    public int questProgress = 0;
    /** Время (gameTime), когда способность будет готова. Не сохраняется. */
    public final long[] ready = new long[3];

    public static PlayerStats get(Player p) {
        return p.getData(ModAttachments.STATS);
    }

    public static long xpFor(int level) {
        return (long) (50 + 3 * Math.pow(level, 1.6));
    }

    public double maxEnergy() { return 100 + level * 1.5; }

    public double power() { return 1.0 + level / 100.0; }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag t = new CompoundTag();
        t.putInt("level", level);
        t.putLong("xp", xp);
        t.putLong("beli", beli);
        t.putString("fruit", fruit);
        t.putInt("mastery", mastery);
        t.putDouble("energy", energy);
        t.putString("quest", quest);
        t.putInt("qp", questProgress);
        return t;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag t) {
        level = Math.max(1, t.getInt("level"));
        xp = t.getLong("xp");
        beli = t.getLong("beli");
        fruit = t.getString("fruit");
        mastery = t.getInt("mastery");
        energy = t.contains("energy") ? t.getDouble("energy") : 100;
        quest = t.getString("quest");
        questProgress = t.getInt("qp");
    }
}
