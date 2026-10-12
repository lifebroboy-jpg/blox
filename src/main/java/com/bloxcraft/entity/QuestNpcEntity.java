package com.bloxcraft.entity;

import com.bloxcraft.data.Fruit;
import com.bloxcraft.data.PlayerStats;
import com.bloxcraft.data.Quest;
import com.bloxcraft.data.SwordType;
import com.bloxcraft.logic.Progression;
import com.bloxcraft.net.Sync;
import com.bloxcraft.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Неубиваемый NPC: даёт квесты, продаёт фрукты и мечи. */
public class QuestNpcEntity extends PathfinderMob {
    public enum Role { QUEST, FRUIT_DEALER, SWORD_DEALER }

    private static final EntityDataAccessor<Integer> ROLE =
            SynchedEntityData.defineId(QuestNpcEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA =
            SynchedEntityData.defineId(QuestNpcEntity.class, EntityDataSerializers.STRING);

    public QuestNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    public void setup(Role role, String data, String name) {
        entityData.set(ROLE, role.ordinal());
        entityData.set(DATA, data);
        setCustomName(Component.literal(name));
        setCustomNameVisible(true);
        setPersistenceRequired();
    }

    public Role role() { return Role.values()[Math.floorMod(entityData.get(ROLE), Role.values().length)]; }

    public String data() { return entityData.get(DATA); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(ROLE, 0);
        b.define(DATA, "");
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8f));
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean hurt(DamageSource src, float amount) {
        return src.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && super.hurt(src, amount);
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Role", entityData.get(ROLE));
        tag.putString("Data", entityData.get(DATA));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(ROLE, tag.getInt("Role"));
        entityData.set(DATA, tag.getString("Data"));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer sp) handle(sp);
        return InteractionResult.CONSUME;
    }

    private Fruit fruitStock() {
        long day = level().getDayTime() / 24000L;
        int n = Fruit.values().length;
        return Fruit.values()[(int) Math.floorMod(day * 7 + getUUID().hashCode(), (long) n)];
    }

    private void handle(ServerPlayer p) {
        PlayerStats s = PlayerStats.get(p);
        switch (role()) {
            case QUEST -> quest(p, s);
            case FRUIT_DEALER -> {
                Fruit f = fruitStock();
                shop(p, s, ModItems.fruit(f), f.price, f.nameRu + " (" + f.tier() + ")");
            }
            case SWORD_DEALER -> {
                SwordType t = SwordType.byId(data());
                if (t != null) shop(p, s, ModItems.sword(t), t.price, t.nameRu + " (треб. ур. " + t.minLevel + ")");
            }
        }
    }

    private void quest(ServerPlayer p, PlayerStats s) {
        Quest q = Quest.byId(data());
        if (q == null) return;
        if (s.quest.equals(q.id)) {
            if (s.questProgress >= q.count) {
                s.quest = "";
                s.questProgress = 0;
                p.sendSystemMessage(Component.literal("§a✔ Задание «" + q.title + "» выполнено! §e+" + q.xp + " опыта, §6+" + q.beli + " белли"));
                Progression.addBeli(p, q.beli);
                Progression.addXp(p, q.xp);
            } else {
                p.sendSystemMessage(Component.literal("§e[" + q.title + "] §fПрогресс: " + s.questProgress + "/" + q.count));
            }
            return;
        }
        if (!s.quest.isEmpty()) {
            p.sendSystemMessage(Component.literal("§cСначала завершите текущее задание."));
            return;
        }
        if (s.level < q.minLevel) {
            p.sendSystemMessage(Component.literal("§cЭто задание доступно с уровня " + q.minLevel + " (у вас " + s.level + ")"));
            return;
        }
        s.quest = q.id;
        s.questProgress = 0;
        p.sendSystemMessage(Component.literal("§e[" + q.title + "] §fУбейте §b" + q.count + " §f— " + q.targetRu
                + ". Награда: §e" + q.xp + " опыта§f, §6" + q.beli + " белли§f. Вернитесь ко мне после."));
        Sync.sync(p);
    }

    private void shop(ServerPlayer p, PlayerStats s, Item item, long price, String name) {
        if (price <= 0) {
            p.sendSystemMessage(Component.literal("§7Этот товар не продаётся."));
            return;
        }
        if (!p.isShiftKeyDown()) {
            p.sendSystemMessage(Component.literal("§e[Торговец] §fПродаю: §b" + name + " §fза §6" + price
                    + " белли§f. Присядьте и нажмите ПКМ, чтобы купить. §7(у вас " + s.beli + ")"));
            return;
        }
        if (s.beli < price) {
            p.sendSystemMessage(Component.literal("§cНе хватает белли: нужно " + price + ", у вас " + s.beli));
            return;
        }
        s.beli -= price;
        ItemStack st = new ItemStack(item);
        if (!p.getInventory().add(st)) p.drop(st, false);
        Sync.sync(p);
        p.sendSystemMessage(Component.literal("§aКуплено: §b" + name));
    }
}
