package com.bloxcraft.client;

import com.bloxcraft.BloxCraft;
import com.bloxcraft.entity.BossEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BossRenderer extends HumanoidMobRenderer<BossEntity, HumanoidModel<BossEntity>> {
    public BossRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(BossEntity e) {
        return BloxCraft.id("textures/entity/boss_" + e.type().id + ".png");
    }

    @Override
    protected void scale(BossEntity e, PoseStack ps, float partialTick) {
        float s = (float) e.type().scale;
        ps.scale(s, s, s);
    }
}
