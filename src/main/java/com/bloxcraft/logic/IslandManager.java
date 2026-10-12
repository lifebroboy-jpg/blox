package com.bloxcraft.logic;

import com.bloxcraft.Config;
import com.bloxcraft.data.Island;
import com.bloxcraft.data.IslandData;
import com.bloxcraft.entity.BossEntity;
import com.bloxcraft.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayDeque;
import java.util.List;

public final class IslandManager {
    private static final ArrayDeque<IslandBuilder.Job> QUEUE = new ArrayDeque<>();

    private IslandManager() {}

    public static BlockPos defaultCenter(ServerLevel l, Island i) {
        BlockPos sp = l.getSharedSpawnPos();
        double ang = i.ordinal() * 0.9 + 0.3;
        int x = sp.getX() + (int) (Math.cos(ang) * i.dist);
        int z = sp.getZ() + (int) (Math.sin(ang) * i.dist);
        return new BlockPos(x, i.sky ? Island.SKY_Y : Island.SEA_Y, z);
    }

    /** При старте сервера ставит в очередь все ещё не построенные острова. */
    public static void init(MinecraftServer server) {
        ServerLevel o = server.overworld();
        IslandData d = IslandData.get(o);
        if (!Config.AUTO_ISLANDS.get()) return;
        for (Island i : Island.values()) {
            if (!d.built.contains(i.id)) {
                BlockPos c = d.centers.computeIfAbsent(i.id, k -> defaultCenter(o, i));
                enqueue(i, c);
            }
        }
        d.setDirty();
    }

    public static void enqueue(Island i, BlockPos center) {
        QUEUE.removeIf(j -> j.island == i);
        QUEUE.add(new IslandBuilder.Job(i, center));
    }

    public static void clear() { QUEUE.clear(); }

    public static void tick(MinecraftServer server) {
        ServerLevel o = server.overworld();
        IslandBuilder.Job job = QUEUE.peek();
        if (job != null && job.step(o, 120)) {
            QUEUE.poll();
            IslandData d = IslandData.get(o);
            d.centers.put(job.island.id, job.center);
            d.built.add(job.island.id);
            d.setDirty();
        }
        if (server.getTickCount() % 100 == 0) bossTick(o);
    }

    private static void bossTick(ServerLevel o) {
        IslandData d = IslandData.get(o);
        long now = o.getGameTime();
        for (Island i : Island.values()) {
            if (!d.built.contains(i.id)) continue;
            BlockPos c = d.centers.get(i.id);
            if (c == null || !o.isPositionEntityTicking(c)) continue;
            AABB area = new AABB(c).inflate(i.radius + 25, 60, i.radius + 25);
            List<BossEntity> bosses = o.getEntitiesOfClass(BossEntity.class, area, b -> i.id.equals(b.islandId()));
            if (!bosses.isEmpty()) continue;
            if (now < d.respawn.getOrDefault(i.id, 0L)) continue;
            spawnBoss(o, i, c);
        }
    }

    public static void spawnBoss(ServerLevel o, Island i, BlockPos c) {
        int x = c.getX() + i.radius / 2, z = c.getZ();
        int y = o.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BossEntity b = ModEntities.BOSS.get().create(o);
        if (b == null) return;
        b.moveTo(x + 0.5, y, z + 0.5, o.random.nextFloat() * 360f, 0f);
        b.setIsland(i.id);
        b.setType(i.boss);
        b.setPersistenceRequired();
        o.addFreshEntity(b);
    }
}
