package com.bloxcraft.client;

import com.bloxcraft.BloxCraft;
import com.bloxcraft.net.AbilityPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = BloxCraft.MODID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        for (int i = 0; i < ClientModEvents.KEYS.length; i++) {
            while (ClientModEvents.KEYS[i].consumeClick()) {
                PacketDistributor.sendToServer(new AbilityPayload(i));
            }
        }
    }
}
