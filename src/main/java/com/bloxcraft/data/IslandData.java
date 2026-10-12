package com.bloxcraft.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Хранит координаты островов, флаг «построен» и время респауна боссов. */
public class IslandData extends SavedData {
    public final Map<String, BlockPos> centers = new HashMap<>();
    public final Set<String> built = new HashSet<>();
    public final Map<String, Long> respawn = new HashMap<>();

    public static IslandData get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(IslandData::new, IslandData::load, null), "bloxcraft_islands");
    }

    public static IslandData load(CompoundTag tag, HolderLookup.Provider provider) {
        IslandData d = new IslandData();
        ListTag list = tag.getList("islands", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            String id = c.getString("id");
            d.centers.put(id, BlockPos.of(c.getLong("pos")));
            if (c.getBoolean("built")) d.built.add(id);
            d.respawn.put(id, c.getLong("respawn"));
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<String, BlockPos> e : centers.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.putString("id", e.getKey());
            c.putLong("pos", e.getValue().asLong());
            c.putBoolean("built", built.contains(e.getKey()));
            c.putLong("respawn", respawn.getOrDefault(e.getKey(), 0L));
            list.add(c);
        }
        tag.put("islands", list);
        return tag;
    }
}
