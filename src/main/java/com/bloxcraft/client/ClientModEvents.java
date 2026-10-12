package com.bloxcraft.client;

import com.bloxcraft.BloxCraft;
import com.bloxcraft.registry.ModEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = BloxCraft.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final KeyMapping[] KEYS = {
            new KeyMapping("key.bloxcraft.z", InputConstants.KEY_Z, "key.categories.bloxcraft"),
            new KeyMapping("key.bloxcraft.x", InputConstants.KEY_X, "key.categories.bloxcraft"),
            new KeyMapping("key.bloxcraft.c", InputConstants.KEY_C, "key.categories.bloxcraft")
    };

    private ClientModEvents() {}

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        for (KeyMapping k : KEYS) e.register(k);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.BOSS.get(), BossRenderer::new);
        e.registerEntityRenderer(ModEntities.NPC.get(), NpcRenderer::new);
    }

    @SubscribeEvent
    public static void gui(RegisterGuiLayersEvent e) {
        e.registerAboveAll(BloxCraft.id("hud"), Hud::render);
    }
}
