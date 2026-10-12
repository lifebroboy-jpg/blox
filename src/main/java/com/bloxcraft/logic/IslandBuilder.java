package com.bloxcraft.logic;

import com.bloxcraft.data.Fruit;
import com.bloxcraft.data.Island;
import com.bloxcraft.entity.QuestNpcEntity;
import com.bloxcraft.registry.ModEntities;
import com.bloxcraft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/** Пошаговая постройка острова (чтобы не вешать сервер). */
public final class IslandBuilder {
    private IslandBuilder() {}

    public static final class Job {
        public final Island island;
        public final BlockPos center;
        private final List<int[]> cols = new ArrayList<>();
        private int idx = 0;
        private final long seed;
        private final double p1, p2;
        private final int baseY, topY;

        public Job(Island island, BlockPos center) {
            this.island = island;
            this.center = center;
            this.baseY = center.getY();
            this.topY = baseY + island.hill;
            this.seed = center.asLong() ^ island.id.hashCode();
            RandomSource r = RandomSource.create(seed);
            this.p1 = r.nextDouble() * Math.PI * 2;
            this.p2 = r.nextDouble() * Math.PI * 2;
            int R = island.radius + 3;
            for (int dx = -R; dx <= R; dx++) {
                for (int dz = -R; dz <= R; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d <= edge(Math.atan2(dz, dx))) cols.add(new int[]{dx, dz});
                }
            }
        }

        private double edge(double ang) {
            return island.radius * (0.82 + 0.10 * Math.sin(3 * ang + p1) + 0.08 * Math.sin(5 * ang + p2));
        }

        /** @return true, когда остров полностью построен. */
        public boolean step(ServerLevel l, int budget) {
            for (int n = 0; n < budget && idx < cols.size(); n++) {
                int[] c = cols.get(idx++);
                column(l, c[0], c[1]);
            }
            if (idx >= cols.size()) {
                decorate(l);
                return true;
            }
            return false;
        }

        private void column(ServerLevel l, int dx, int dz) {
            int x = center.getX() + dx, z = center.getZ() + dz;
            double dist = Math.sqrt(dx * dx + dz * dz);
            double e = edge(Math.atan2(dz, dx));
            double t = dist <= 8 ? 1.0 : Math.max(0, 1 - (dist - 8) / (e - 8));
            int surf = baseY + (int) Math.round(t * island.hill);
            if (dist > 8 && ((x * 31 + z * 17) & 3) == 0) surf++;
            int bottom = island.sky ? surf - (int) (t * 18) - 3 : baseY - 24;
            Block top = (!island.sky && dist > e * 0.9) ? island.rim : island.top;

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int y = bottom; y <= surf; y++) {
                Block b = y == surf ? top : (y >= surf - 3 ? island.sub : island.stone);
                l.setBlock(pos.set(x, y, z), b.defaultBlockState(), 2);
            }
            int ceil = topY + 24;
            BlockState air = Blocks.AIR.defaultBlockState();
            for (int y = surf + 1; y <= ceil; y++) {
                pos.set(x, y, z);
                if (!l.getBlockState(pos).isAir()) l.setBlock(pos, air, 2);
            }
        }

        private void decorate(ServerLevel l) {
            int cx = center.getX(), cz = center.getZ();
            AABB area = new AABB(center).inflate(island.radius + 5, 40, island.radius + 5);
            l.getEntitiesOfClass(QuestNpcEntity.class, area).forEach(Entity::discard);

            hut(l, cx, cz);
            scatter(l, cx, cz);

            npc(l, cx + 0.5, topY + 1, cz + 0.5, QuestNpcEntity.Role.QUEST, island.quest.id,
                    "§e" + island.quest.title);
            int fx = cx + 6, fz = cz + 4;
            npc(l, fx + 0.5, l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, fx, fz), fz + 0.5,
                    QuestNpcEntity.Role.FRUIT_DEALER, "", "§dТорговец фруктами");
            int sx = cx - 6, sz = cz + 4;
            npc(l, sx + 0.5, l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sx, sz), sz + 0.5,
                    QuestNpcEntity.Role.SWORD_DEALER, island.sword.id, "§cОружейник");
        }

        private void hut(ServerLevel l, int cx, int cz) {
            BlockState acc = island.accent.defaultBlockState();
            BlockState air = Blocks.AIR.defaultBlockState();
            BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    boolean wall = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                    l.setBlock(new BlockPos(cx + dx, topY, cz + dz), acc, 2);
                    for (int h = 1; h <= 4; h++) {
                        BlockState st = air;
                        if (wall) {
                            boolean door = dz == 3 && dx == 0 && h <= 2;
                            boolean window = h == 2 && ((Math.abs(dx) == 3 && dz == 0) || (Math.abs(dz) == 3 && dx == 0));
                            st = door ? air : (window ? glass : acc);
                        }
                        l.setBlock(new BlockPos(cx + dx, topY + h, cz + dz), st, 2);
                    }
                    boolean lamp = dx == 0 && dz == 0;
                    l.setBlock(new BlockPos(cx + dx, topY + 5, cz + dz),
                            lamp ? Blocks.GLOWSTONE.defaultBlockState() : acc, 2);
                }
            }
            BlockPos cp = new BlockPos(cx - 2, topY + 1, cz - 2);
            l.setBlock(cp, Blocks.CHEST.defaultBlockState(), 3);
            if (l.getBlockEntity(cp) instanceof ChestBlockEntity chest) {
                RandomSource r = RandomSource.create(seed + 7);
                chest.setItem(0, new ItemStack(Items.BREAD, 12));
                chest.setItem(1, new ItemStack(Items.IRON_INGOT, 8));
                chest.setItem(2, new ItemStack(ModItems.fruit(Fruit.random(r, island.ordinal() < 3 ? 0 : 1))));
                chest.setItem(3, new ItemStack(ModItems.LOG_POSE.get()));
            }
        }

        private void scatter(ServerLevel l, int cx, int cz) {
            RandomSource r = RandomSource.create(seed + 1);
            int placed = 0, attempts = 0;
            while (placed < island.decorCount && attempts++ < 500) {
                double ang = r.nextDouble() * Math.PI * 2;
                double e = edge(ang);
                double d = 12 + r.nextDouble() * Math.max(1, e - 17);
                int x = cx + (int) (Math.cos(ang) * d), z = cz + (int) (Math.sin(ang) * d);
                int gy = l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                BlockPos g = new BlockPos(x, gy, z);
                if (!l.getBlockState(g).is(island.top)) continue;
                switch (island.decor) {
                    case TREE -> tree(l, g, r);
                    case CACTUS -> {
                        int h = 1 + r.nextInt(3);
                        for (int i = 1; i <= h; i++) l.setBlock(g.above(i), Blocks.CACTUS.defaultBlockState(), 2);
                    }
                    case PILLAR -> {
                        int h = 2 + r.nextInt(4);
                        for (int i = 1; i <= h; i++) l.setBlock(g.above(i), island.log.defaultBlockState(), 2);
                        l.setBlock(g.above(h + 1), island.leaves.defaultBlockState(), 2);
                    }
                }
                placed++;
            }
        }

        private void tree(ServerLevel l, BlockPos g, RandomSource r) {
            int h = 4 + r.nextInt(3);
            BlockState log = island.log.defaultBlockState();
            BlockState leaf = island.leaves.defaultBlockState();
            for (int i = 1; i <= h; i++) l.setBlock(g.above(i), log, 2);
            for (int dy = h - 2; dy <= h + 1; dy++) {
                int rad = dy >= h ? 1 : 2;
                for (int dx = -rad; dx <= rad; dx++) {
                    for (int dz = -rad; dz <= rad; dz++) {
                        if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                        BlockPos bp = g.offset(dx, dy, dz);
                        if (l.getBlockState(bp).isAir()) l.setBlock(bp, leaf, 2);
                    }
                }
            }
        }

        private void npc(ServerLevel l, double x, double y, double z, QuestNpcEntity.Role role, String data, String name) {
            QuestNpcEntity n = ModEntities.NPC.get().create(l);
            if (n == null) return;
            n.moveTo(x, y, z, 180f, 0f);
            n.setup(role, data, name);
            l.addFreshEntity(n);
        }
    }
}
