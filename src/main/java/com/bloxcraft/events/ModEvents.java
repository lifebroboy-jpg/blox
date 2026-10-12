package com.bloxcraft.events;

import com.bloxcraft.BloxCraft;
import com.bloxcraft.entity.BossEntity;
import com.bloxcraft.entity.QuestNpcEntity;
import com.bloxcraft.registry.ModEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = BloxCraft.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEvents {
    private ModEvents() {}

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent e) {
        e.put(ModEntities.BOSS.get(), BossEntity.createAttributes().build());
        e.put(ModEntities.NPC.get(), QuestNpcEntity.createAttributes().build());
    }
}
