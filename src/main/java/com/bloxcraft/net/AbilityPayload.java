package com.bloxcraft.net;

import com.bloxcraft.BloxCraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AbilityPayload(int slot) implements CustomPacketPayload {
    public static final Type<AbilityPayload> TYPE = new Type<>(BloxCraft.id("ability"));
    public static final StreamCodec<ByteBuf, AbilityPayload> CODEC =
            ByteBufCodecs.VAR_INT.map(AbilityPayload::new, AbilityPayload::slot);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
