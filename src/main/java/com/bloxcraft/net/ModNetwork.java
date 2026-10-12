package com.bloxcraft.net;

import com.bloxcraft.BloxCraft;
import com.bloxcraft.client.ClientStats;
import com.bloxcraft.logic.Abilities;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = BloxCraft.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetwork {
    private ModNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("1");
        r.playToServer(AbilityPayload.TYPE, AbilityPayload.CODEC, (payload, ctx) -> {
            if (ctx.player() instanceof ServerPlayer sp) Abilities.cast(sp, payload.slot());
        });
        r.playToClient(StatsPayload.TYPE, StatsPayload.CODEC,
                (payload, ctx) -> ClientStats.set(payload.tag()));
    }
}
