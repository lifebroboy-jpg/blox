package com.bloxcraft.data;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum Island {
    PIRATE_VILLAGE("pirate_village", "Деревня пиратов", 34, 4, false, 220,
            Blocks.GRASS_BLOCK, Blocks.SAND, Blocks.DIRT, Blocks.STONE, Blocks.SPRUCE_PLANKS,
            Decor.TREE, Blocks.OAK_LOG, Blocks.OAK_LEAVES, 12,
            BossType.BOBBY, Quest.BANDITS, SwordType.KATANA),
    JUNGLE("jungle", "Остров джунглей", 40, 8, false, 520,
            Blocks.GRASS_BLOCK, Blocks.SAND, Blocks.DIRT, Blocks.STONE, Blocks.JUNGLE_PLANKS,
            Decor.TREE, Blocks.JUNGLE_LOG, Blocks.JUNGLE_LEAVES, 26,
            BossType.GORILLA_KING, Quest.MONKEYS, SwordType.CUTLASS),
    DESERT("desert", "Пустынный остров", 40, 6, false, 820,
            Blocks.SAND, Blocks.SAND, Blocks.SAND, Blocks.SANDSTONE, Blocks.CUT_SANDSTONE,
            Decor.CACTUS, Blocks.CACTUS, Blocks.CACTUS, 18,
            BossType.SABER_EXPERT, Quest.DESERT_BANDITS, SwordType.BISENTO),
    FROZEN_VILLAGE("frozen_village", "Ледяная деревня", 38, 9, false, 1120,
            Blocks.SNOW_BLOCK, Blocks.SNOW_BLOCK, Blocks.SNOW_BLOCK, Blocks.PACKED_ICE, Blocks.SPRUCE_PLANKS,
            Decor.TREE, Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES, 14,
            BossType.ICE_ADMIRAL, Quest.SNOW_TROOPERS, SwordType.SHISUI),
    MAGMA_VILLAGE("magma_village", "Магмовая деревня", 38, 10, false, 1420,
            Blocks.SMOOTH_BASALT, Blocks.BASALT, Blocks.BLACKSTONE, Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS,
            Decor.PILLAR, Blocks.BLACKSTONE, Blocks.MAGMA_BLOCK, 16,
            BossType.MAGMA_ADMIRAL, Quest.LAVA_BOMBERS, SwordType.SADDI),
    MARINE_FORTRESS("marine_fortress", "Крепость Морского дозора", 44, 3, false, 1720,
            Blocks.STONE_BRICKS, Blocks.STONE_BRICKS, Blocks.COBBLESTONE, Blocks.STONE, Blocks.STONE_BRICKS,
            Decor.PILLAR, Blocks.STONE_BRICK_WALL, Blocks.LANTERN, 14,
            BossType.GREYBEARD, Quest.GREYBEARD_HUNT, SwordType.YAMA),
    SKY_ISLAND("sky_island", "Небесный остров", 38, 6, true, 2000,
            Blocks.GRASS_BLOCK, Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.DIORITE, Blocks.QUARTZ_BRICKS,
            Decor.TREE, Blocks.CHERRY_LOG, Blocks.CHERRY_LEAVES, 12,
            BossType.DARKBEARD, Quest.SKY_ENDERS, SwordType.DARK_BLADE);

    public enum Decor { TREE, CACTUS, PILLAR }

    public static final int SKY_Y = 170;
    public static final int SEA_Y = 64;

    public final String id, name;
    public final int radius, hill, dist, decorCount;
    public final boolean sky;
    public final Block top, rim, sub, stone, accent, log, leaves;
    public final Decor decor;
    public final BossType boss;
    public final Quest quest;
    public final SwordType sword;

    Island(String id, String name, int radius, int hill, boolean sky, int dist,
           Block top, Block rim, Block sub, Block stone, Block accent,
           Decor decor, Block log, Block leaves, int decorCount,
           BossType boss, Quest quest, SwordType sword) {
        this.id = id; this.name = name; this.radius = radius; this.hill = hill; this.sky = sky; this.dist = dist;
        this.top = top; this.rim = rim; this.sub = sub; this.stone = stone; this.accent = accent;
        this.decor = decor; this.log = log; this.leaves = leaves; this.decorCount = decorCount;
        this.boss = boss; this.quest = quest; this.sword = sword;
    }

    public static Island byId(String id) {
        for (Island i : values()) if (i.id.equals(id)) return i;
        return null;
    }
}
