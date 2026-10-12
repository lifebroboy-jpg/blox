package com.bloxcraft;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue AUTO_ISLANDS;
    public static final ModConfigSpec.IntValue BOSS_RESPAWN_TICKS;
    public static final ModConfigSpec.DoubleValue FRUIT_DROP_CHANCE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        AUTO_ISLANDS = b.comment("Автоматически строить острова при первом запуске мира")
                .define("autoGenerateIslands", true);
        BOSS_RESPAWN_TICKS = b.comment("Через сколько тиков возрождается босс острова (20 тиков = 1 сек)")
                .defineInRange("bossRespawnTicks", 9600, 600, 10_000_000);
        FRUIT_DROP_CHANCE = b.comment("Шанс выпадения фрукта с обычного враждебного моба (0.004 = 0.4%)")
                .defineInRange("fruitDropChance", 0.004, 0.0, 1.0);
        SPEC = b.build();
    }

    private Config() {}
}
