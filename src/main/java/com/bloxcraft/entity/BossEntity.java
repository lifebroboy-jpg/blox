package com.bloxcraft.entity;

import com.bloxcraft.Config;
import com.bloxcraft.data.BossType;
import com.bloxcraft.data.Fruit;
import com.bloxcraft.data.IslandData;
import com.bloxcraft.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BossEntity extends Monster {
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(BossEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bar = new ServerBossEvent(Component.empty(),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private String islandId = "";
    private int abilityTimer = 120;

    public BossEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 8)
                .add(Attributes.FOLLOW_RANGE, 40)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public BossType type() { return BossType.byIndex(entityData.get(VARIANT)); }

    public String islandId() { return islandId; }

    public void setIsland(String id) { this.islandId = id; }

    public void setType(BossType t) {
        entityData.set(VARIANT, t.ordinal());
        applyType(t);
        setHealth(getMaxHealth());
    }

    private void applyType(BossType t) {
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(t.hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(t.attack);
        setCustomName(Component.literal(t.display));
        bar.setName(Component.literal(t.display));
        bar.setColor(t.color);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.sword(t.weapon)));
        setDropChance(EquipmentSlot.MAINHAND, 0f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", entityData.get(VARIANT));
        tag.putString("Island", islandId);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(VARIANT, tag.getInt("Variant"));
        islandId = tag.getString("Island");
        applyType(type());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer p) { super.startSeenByPlayer(p); bar.addPlayer(p); }

    @Override
    public void stopSeenByPlayer(ServerPlayer p) { super.stopSeenByPlayer(p); bar.removePlayer(p); }

    @Override
    public boolean removeWhenFarAway(double dist) { return false; }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bar.setProgress(getHealth() / getMaxHealth());
        LivingEntity t = getTarget();
        if (t != null && --abilityTimer <= 0 && level() instanceof ServerLevel sl) {
            abilityTimer = 100 + random.nextInt(80);
            switch (random.nextInt(3)) {
                case 0 -> slam(sl);
                case 1 -> summon(sl, t);
                default -> charge(t);
            }
        }
    }

    private void slam(ServerLevel sl) {
        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 0.5, getZ(), 1, 0, 0, 0, 0);
        double dmg = getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.2;
        for (Player p : sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(7),
                pl -> !pl.isCreative() && !pl.isSpectator())) {
            p.hurt(damageSources().mobAttack(this), (float) dmg);
            Vec3 d = p.position().subtract(position()).normalize();
            p.setDeltaMovement(d.x * 1.2, 0.6, d.z * 1.2);
            p.hurtMarked = true;
        }
    }

    private void summon(ServerLevel sl, LivingEntity target) {
        EntityType<? extends Mob> mt = type().minion;
        int existing = sl.getEntitiesOfClass(Mob.class, getBoundingBox().inflate(20), m -> m.getType() == mt).size();
        if (existing >= 6) return;
        for (int i = 0; i < 2; i++) {
            Mob m = mt.create(sl);
            if (m == null) continue;
            m.moveTo(getX() + random.nextInt(5) - 2, getY(), getZ() + random.nextInt(5) - 2, random.nextFloat() * 360f, 0f);
            m.setTarget(target);
            sl.addFreshEntity(m);
        }
    }

    private void charge(LivingEntity t) {
        Vec3 d = t.position().subtract(position()).normalize().scale(1.4);
        setDeltaMovement(d.x, 0.3, d.z);
        hasImpulse = true;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource src, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, src, recentlyHit);
        BossType t = type();
        Fruit f = Fruit.random(random, t.maxRarity);
        spawnAtLocation(new ItemStack(ModItems.fruit(f)));
        if (random.nextFloat() < 0.35f) spawnAtLocation(new ItemStack(ModItems.sword(t.drop)));
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel sl && !islandId.isEmpty()) {
            IslandData d = IslandData.get(sl.getServer().overworld());
            d.respawn.put(islandId, sl.getGameTime() + Config.BOSS_RESPAWN_TICKS.get());
            d.setDirty();
        }
    }
}
