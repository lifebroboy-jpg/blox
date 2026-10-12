package com.bloxcraft.client;

import com.bloxcraft.BloxCraft;
import com.bloxcraft.entity.QuestNpcEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class NpcRenderer extends HumanoidMobRenderer<QuestNpcEntity, HumanoidModel<QuestNpcEntity>> {
    public NpcRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(QuestNpcEntity e) {
        String n = switch (e.role()) {
            case QUEST -> "npc_quest";
            case FRUIT_DEALER -> "npc_fruit";
            case SWORD_DEALER -> "npc_sword";
        };
        return BloxCraft.id("textures/entity/" + n + ".png");
    }
}
