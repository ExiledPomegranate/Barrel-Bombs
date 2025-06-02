package com.exiledpomegranate.entities;

import com.exiledpomegranate.BarrelBombExplosion;
import com.exiledpomegranate.ConfigHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;

import static com.exiledpomegranate.BarrelBombExplosion.VecItoD;

public class BarrelBombEntity extends Entity implements Ownable {
    private static final TrackedData<Integer> FUSE = DataTracker.registerData(BarrelBombEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> FACING = DataTracker.registerData(BarrelBombEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final int DEFAULT_FUSE = 80;

    @Nullable
    private LivingEntity causingEntity;
    private boolean kaboomed = false;
    private int extraGunpowder;
    private int extraBlazePowder;

    public BarrelBombEntity(EntityType<? extends BarrelBombEntity> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
    }

    public BarrelBombEntity(World world, double x, double y, double z, Direction facing,
                            @Nullable LivingEntity igniter, int extraGunpowder, int extraBlazePowder) {
        this(EntityInit.BARREL_BOMB_ENTITY.get(), world);
        this.setPosition(x, y, z);
        this.setFuse(DEFAULT_FUSE);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        setFacing(facing);
        this.causingEntity = igniter;
        this.extraGunpowder = extraGunpowder;
        this.extraBlazePowder = extraBlazePowder;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(FUSE, DEFAULT_FUSE);
        this.dataTracker.startTracking(FACING, Direction.NORTH.ordinal());
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return MoveEffect.NONE;
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Environment(EnvType.CLIENT)
    public static void spawnSmokeCloud(Position pos, Random random, World world) {
        for (SmokeParticles particles : ConfigHandler.config().smokeParticles) {
            for (int i = 0; i < particles.amount(); i++) {
                // Random position around the entity
                double xOff = (random.nextDouble() - 0.5) * 2 * particles.range();
                double yOff = (random.nextDouble() - 0.5) * 2 * particles.range();
                double zOff = (random.nextDouble() - 0.5) * 2 * particles.range();

                // Direction vector from center to the random position
                double dx = xOff;
                double dy = yOff;
                double dz = zOff;

                // Normalize and scale by velocity
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (length == 0) length = 1; // Prevent division by zero

                dx = dx / length * particles.velocity();
                dy = dy / length * particles.velocity();
                dz = dz / length * particles.velocity();

                // Spawn particle
                world.addParticle(
                        particles.type(),
                        xOff + pos.getX(), yOff + pos.getY(), zOff + pos.getZ(),
                        dx, dy, dz
                );
            }
        }
    }

    @Override
    public void tick() {
        int fuse = this.getFuse() - 1;
        this.setFuse(fuse);

        if (fuse <= -3) {
            this.discard();
        }

        if (fuse <= 0 && !kaboomed) {
            kaboomed = true;
            if (!this.getWorld().isClient) {
                this.explode();
            } else {
                spawnSmokeCloud(this.getPos(), this.random, this.getWorld());
            }
        } else {
            if (this.getWorld().isClient) {
                this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    public static void playKaboomSound(World world, Position pos, Random random) {
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS,
                4.0F + (random.nextBetween(-10, 10) / 10F), 1.0F + (random.nextBetween(-10, 10) / 10F));
    }

    private void explode() {
        playKaboomSound(this.getWorld(), this.getPos(), this.random);

        BarrelBombExplosion explosion = new BarrelBombExplosion(this.getWorld(), this, this.getX(),
                this.getBodyY(0.0625), this.getZ(), ConfigHandler.config().power, false,
                Explosion.DestructionType.DESTROY, VecItoD(Direction.byId(this.dataTracker.get(FACING)).getVector()),
                this.extraGunpowder, this.extraBlazePowder);
        explosion.kaboom();
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {

    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putShort("Fuse", (short) this.getFuse());
        nbt.putInt("Facing", this.dataTracker.get(FACING));
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.setFuse(nbt.getShort("Fuse"));
        if (nbt.contains("Facing", NbtElement.INT_TYPE)) {
            this.dataTracker.set(FACING, nbt.getInt("Facing"));
        }
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        return this.causingEntity;
    }

    @Override
    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return 0.15F;
    }

    public void setFuse(int fuse) {
        this.dataTracker.set(FUSE, fuse);
    }

    public int getFuse() {
        return this.dataTracker.get(FUSE);
    }

    public void setFacing(Direction facing) {
        this.dataTracker.set(FACING, facing.ordinal());
    }

    public Direction getFacing() {
        return Direction.byId(this.dataTracker.get(FACING));
    }

    public record SmokeParticles(ParticleEffect type, int amount, float range, float velocity) {

    }
}
