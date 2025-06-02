package com.exiledpomegranate;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;


// Mostly copied from the Explosion class.
public class BarrelBombExplosion extends Explosion {
    private static final ExplosionBehavior DEFAULT_BEHAVIOR = new ExplosionBehavior();
    private final boolean createFire;
    private final Explosion.DestructionType destructionType;
    private final Random random;
    private final World world;
    private double x;
    private double y;
    private double z;
    @Nullable
    public Entity entity;
    public float power;
    private final DamageSource damageSource;
    private final ExplosionBehavior behavior;
    private final ObjectArrayList<BlockPos> affectedBlocks;
    private final Map<PlayerEntity, Vec3d> affectedPlayers;
    private final Vec3d direction;
    private final int extraBlazePowder;

    public BarrelBombExplosion(World world, @Nullable Entity entity, double x, double y, double z, float power, Vec3d direction, List<BlockPos> affectedBlocks, int extraGunpowder, int extraBlazePowder) {
        this(world, entity, x, y, z, power, false, Explosion.DestructionType.DESTROY_WITH_DECAY, direction, affectedBlocks, extraGunpowder, extraBlazePowder);
    }

    public BarrelBombExplosion(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType, Vec3d direction, List<BlockPos> affectedBlocks, int extraGunpowder, int extraBlazePowder) {
        this(world, entity, x, y, z, power, createFire, destructionType, direction, extraGunpowder, extraBlazePowder);
        this.affectedBlocks.addAll(affectedBlocks);
    }

    public BarrelBombExplosion(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType, Vec3d direction, int extraGunpowder, int extraBlazePowder) {
        this(world, entity, null, null, x, y, z, power, createFire, destructionType, direction, extraGunpowder, extraBlazePowder);
    }

    public BarrelBombExplosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType, Vec3d direction, int extraGunpowder, int extraBlazePowder) {
        super(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType);
        this.random = Random.create();
        this.affectedBlocks = new ObjectArrayList<>();
        this.affectedPlayers = Maps.newHashMap();
        this.world = world;
        this.entity = entity;
        this.power = modifyPower(power, extraGunpowder);
        this.x = x;
        this.y = y;
        this.z = z;
        this.createFire = createFire;
        this.destructionType = destructionType;
        this.damageSource = damageSource == null ? world.getDamageSources().explosion(this) : damageSource;
        this.behavior = behavior == null ? this.chooseBehavior(entity) : behavior;
        this.direction = direction;
        this.extraBlazePowder = extraBlazePowder;
    }

    private static float modifyPower(float power, int extraGunpowder) {
        return (float) (0.6 * Math.pow(extraGunpowder, 0.9) + power);
    }

    public static Vec3d VecItoD(Vec3i direction) {
        return new Vec3d(direction.getX(), direction.getY(), direction.getZ());
    }

    private ExplosionBehavior chooseBehavior(@Nullable Entity entity) {
        return entity == null ? DEFAULT_BEHAVIOR : new EntityExplosionBehavior(entity);
    }

    public static float getExposure(Vec3d source, Entity entity) {
        Box box = entity.getBoundingBox();
        double d = (double)1.0F / ((box.maxX - box.minX) * (double)2.0F + (double)1.0F);
        double e = (double)1.0F / ((box.maxY - box.minY) * (double)2.0F + (double)1.0F);
        double f = (double)1.0F / ((box.maxZ - box.minZ) * (double)2.0F + (double)1.0F);
        double g = ((double)1.0F - Math.floor((double)1.0F / d) * d) / (double)2.0F;
        double h = ((double)1.0F - Math.floor((double)1.0F / f) * f) / (double)2.0F;
        if (!(d < (double)0.0F) && !(e < (double)0.0F) && !(f < (double)0.0F)) {
            int i = 0;
            int j = 0;

            for(double k = 0.0F; k <= (double)1.0F; k += d) {
                for(double l = 0.0F; l <= (double)1.0F; l += e) {
                    for(double m = 0.0F; m <= (double)1.0F; m += f) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                        Vec3d vec3d = new Vec3d(n + g, o, p + h);
                        if (entity.getWorld().raycast(new RaycastContext(vec3d, source, ShapeType.COLLIDER, FluidHandling.NONE, entity)).getType() == Type.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float)i / (float)j;
        } else {
            return 0.0F;
        }
    }

    private void updatePos(Vec3d pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public Vec3d getPos() {
        return new Vec3d(this.x, this.y, this.z);
    }

    private static int modifyDirectionalOffset(int offset, int extraBlazePowder) {
        return ((int) Math.ceil((float) extraBlazePowder / 2)) + offset;
    }

    public void kaboom() {
        this.collectBlocks();
        Vec3d position = getPos();
        int x = modifyDirectionalOffset(ConfigHandler.config().directionalOffset, this.extraBlazePowder);
        for (int i = 0; i < x; i++) {
            updatePos(position.add(this.direction.multiply(i + 1)));
            this.collectBlocks();
        }
        updatePos(position);

        this.damageEntities();
        this.affectWorld(true);
    }

    public void collectBlocks() {
        this.world.emitGameEvent(this.entity, GameEvent.EXPLODE, new Vec3d(this.x, this.y, this.z));
        Set<BlockPos> set = Sets.newHashSet();

        for(int j = 0; j < 16; ++j) {
            for(int k = 0; k < 16; ++k) {
                for(int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d = (float)j / 15.0F * 2.0F - 1.0F;
                        double e = (float)k / 15.0F * 2.0F - 1.0F;
                        double f = (float)l / 15.0F * 2.0F - 1.0F;
                        double g = Math.sqrt(d * d + e * e + f * f);
                        d /= g;
                        e /= g;
                        f /= g;
                        float h = this.power * (0.7F + this.world.random.nextFloat() * 0.6F);
                        double m = this.x;
                        double n = this.y;
                        double o = this.z;

                        for(; h > 0.0F; h -= 0.22500001F) {
                            BlockPos blockPos = BlockPos.ofFloored(m, n, o);
                            BlockState blockState = this.world.getBlockState(blockPos);
                            FluidState fluidState = this.world.getFluidState(blockPos);
                            if (!this.world.isInBuildLimit(blockPos)) {
                                break;
                            }

                            Optional<Float> optional = this.behavior.getBlastResistance(this, this.world, blockPos, blockState, fluidState);
                            if (optional.isPresent()) {
                                h -= (optional.get() / ConfigHandler.config().penetration + 0.3F) * 0.3F;
                            }

                            if (h > 0.0F && this.behavior.canDestroyBlock(this, this.world, blockPos, blockState, h)
                            && !ConfigHandler.config().immuneList.contains(blockState.getBlock())) {
                                set.add(blockPos);
                            }

                            m += d * (double)0.3F;
                            n += e * (double)0.3F;
                            o += f * (double)0.3F;
                        }
                    }
                }
            }
        }
        this.affectedBlocks.addAll(set);
    }

    public void damageEntities() {
        float q = this.power * 2.0F;
        int k = MathHelper.floor(this.x - (double)q - (double)1.0F);
        int l = MathHelper.floor(this.x + (double)q + (double)1.0F);
        int r = MathHelper.floor(this.y - (double)q - (double)1.0F);
        int s = MathHelper.floor(this.y + (double)q + (double)1.0F);
        int t = MathHelper.floor(this.z - (double)q - (double)1.0F);
        int u = MathHelper.floor(this.z + (double)q + (double)1.0F);
        List<Entity> list = this.world.getOtherEntities(this.entity, new Box(k, r, t, l, s, u));
        Vec3d vec3d = new Vec3d(this.x, this.y, this.z);

        for (Entity value : list) {
            if (!value.isImmuneToExplosion()) {
                double w = Math.sqrt(value.squaredDistanceTo(vec3d)) / (double) q;
                if (w <= (double) 1.0F) {
                    double x = value.getX() - this.x;
                    double y = (value instanceof TntEntity ? value.getY() : value.getEyeY()) - this.y;
                    double z = value.getZ() - this.z;
                    double aa = Math.sqrt(x * x + y * y + z * z);
                    if (aa != (double) 0.0F) {
                        x /= aa;
                        y /= aa;
                        z /= aa;
                        double ab = getExposure(vec3d, value);
                        double ac = ((double) 1.0F - w) * ab;
                        value.damage(this.getDamageSource(), (float) ((int) ((ac * ac + ac) * 8.0)));
                        double ad;
                        if (value instanceof LivingEntity livingEntity) {
                            ad = ProtectionEnchantment.transformExplosionKnockback(livingEntity, ac);
                        } else {
                            ad = ac;
                        }

                        x *= ad;
                        y *= ad;
                        z *= ad;
                        Vec3d vec3d2 = new Vec3d(x, y, z);
                        value.setVelocity(value.getVelocity().add(vec3d2));
                        if (value instanceof PlayerEntity playerEntity) {
                            if (!playerEntity.isSpectator() && (!playerEntity.isCreative() || !playerEntity.getAbilities().flying)) {
                                this.affectedPlayers.put(playerEntity, vec3d2);
                            }
                        }
                    }
                }
            }
        }
    }

    public void affectWorld(boolean particles) {
        boolean bl = this.shouldDestroy();

        if (bl) {
            ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList = new ObjectArrayList<>();
            boolean bl2 = this.getCausingEntity() instanceof PlayerEntity;
            Util.shuffle(this.affectedBlocks, this.world.random);
            ObjectListIterator<?> var5 = this.affectedBlocks.iterator();

            while(var5.hasNext()) {
                BlockPos blockPos = (BlockPos)var5.next();
                BlockState blockState = this.world.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (!blockState.isAir()) {
                    BlockPos blockPos2 = blockPos.toImmutable();
                    this.world.getProfiler().push("explosion_blocks");
                    if (block.shouldDropItemsOnExplosion(this)) {
                        if (this.world instanceof ServerWorld serverWorld
                                && this.random.nextFloat() <= ConfigHandler.config().dropPercentage
                                && !ConfigHandler.config().dropBlacklist.contains(block)) {
                            BlockEntity blockEntity = blockState.hasBlockEntity() ? this.world.getBlockEntity(blockPos) : null;
                            LootContextParameterSet.Builder builder = (new LootContextParameterSet.Builder(serverWorld)).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(blockPos)).add(LootContextParameters.TOOL, ItemStack.EMPTY).addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity).addOptional(LootContextParameters.THIS_ENTITY, this.entity);
                            if (this.destructionType == Explosion.DestructionType.DESTROY_WITH_DECAY) {
                                builder.add(LootContextParameters.EXPLOSION_RADIUS, this.power);
                            }

                            blockState.onStacksDropped(serverWorld, blockPos, ItemStack.EMPTY, bl2);
                            blockState.getDroppedStacks(builder).forEach((stack) -> tryMergeStack(objectArrayList, stack, blockPos2));
                        }
                    }

                    this.world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
                    block.onDestroyedByExplosion(this.world, blockPos, this);
                    this.world.getProfiler().pop();
                }
            }

            var5 = objectArrayList.iterator();

            while(var5.hasNext()) {
                Pair<ItemStack, BlockPos> pair = (Pair<ItemStack, BlockPos>) var5.next();
                Block.dropStack(this.world, pair.getSecond(), pair.getFirst());
            }
        }

        if (this.createFire) {
            for (BlockPos blockPos3 : this.affectedBlocks) {
                if (this.random.nextInt(3) == 0 && this.world.getBlockState(blockPos3).isAir() && this.world.getBlockState(blockPos3.down()).isOpaqueFullCube(this.world, blockPos3.down())) {
                    this.world.setBlockState(blockPos3, AbstractFireBlock.getState(this.world, blockPos3));
                }
            }
        }

    }

    public boolean shouldDestroy() {
        return this.destructionType != Explosion.DestructionType.KEEP;
    }

    private static void tryMergeStack(ObjectArrayList<Pair<ItemStack, BlockPos>> stacks, ItemStack stack, BlockPos pos) {
        int i = stacks.size();

        for(int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPos> pair = stacks.get(j);
            ItemStack itemStack = pair.getFirst();
            if (ItemEntity.canMerge(itemStack, stack)) {
                ItemStack itemStack2 = ItemEntity.merge(itemStack, stack, 16);
                stacks.set(j, Pair.of(itemStack2, pair.getSecond()));
                if (stack.isEmpty()) {
                    return;
                }
            }
        }

        stacks.add(Pair.of(stack, pos));
    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    public Map<PlayerEntity, Vec3d> getAffectedPlayers() {
        return this.affectedPlayers;
    }

    @Nullable
    public LivingEntity getCausingEntity() {
        if (this.entity == null) {
            return null;
        } else {
            Entity entity = this.entity;
            if (entity instanceof TntEntity tntEntity) {
                return tntEntity.getOwner();
            } else {
                if (entity instanceof LivingEntity) {
                    return (LivingEntity)entity;
                } else {
                    if (entity instanceof ProjectileEntity projectileEntity) {
                        entity = projectileEntity.getOwner();
                        if (entity instanceof LivingEntity) {
                            return (LivingEntity)entity;
                        }
                    }

                    return null;
                }
            }
        }
    }

    @Nullable
    public Entity getEntity() {
        return this.entity;
    }

    public void clearAffectedBlocks() {
        this.affectedBlocks.clear();
    }

    public List<BlockPos> getAffectedBlocks() {
        return this.affectedBlocks;
    }
}
