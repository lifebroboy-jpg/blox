package com.bloxcraft.net;

import com.bloxcraft.BloxCraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StatsPayload(CompoundTag tag) implements CustomPacketPayload {
    public static final Type<StatsPayload> TYPE = new Type<>(BloxCraft.id("stats"));
    public static final StreamCodec<ByteBuf, StatsPayload> CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(StatsPayload::new, StatsPayload::tag);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
