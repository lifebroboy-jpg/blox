package com.bloxcraft.registry;

import com.bloxcraft.BloxCraft;
import com.bloxcraft.entity.BossEntity;
import com.bloxcraft.entity.QuestNpcEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, BloxCraft.MODID);

    public static final Supplier<EntityType<BossEntity>> BOSS = ENTITIES.register("boss",
            () -> EntityType.Builder.of(BossEntity::new, MobCategory.MONSTER)
                    .sized(0.8f, 2.4f).clientTrackingRange(10).build("boss"));

    public static final Supplier<EntityType<QuestNpcEntity>> NPC = ENTITIES.register("quest_npc",
            () -> EntityType.Builder.of(QuestNpcEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f).clientTrackingRange(8).build("quest_npc"));

    private ModEntities() {}
}
