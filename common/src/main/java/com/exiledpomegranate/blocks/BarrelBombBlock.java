package com.exiledpomegranate.blocks;

import com.exiledpomegranate.BarrelBombExplosion;
import com.exiledpomegranate.ConfigHandler;
import com.exiledpomegranate.entities.BarrelBombEntity;
import com.exiledpomegranate.items.ItemInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import static com.exiledpomegranate.BarrelBombExplosion.VecItoD;

public class BarrelBombBlock extends Block {
    public static final DirectionProperty FACING = Properties.FACING;
    // When the block is blown up, the blockstate can no longer be accessed.
    private BlockState cached_state;
    public static final IntProperty EXTRAGUNPOWDER = IntProperty.of("extragunpowder", 0, 128);
    public static final IntProperty EXTRABLAZEPOWDER = IntProperty.of("extrablazepowder", 0, 128);

    public BarrelBombBlock() {
        super(Settings.create().mapColor(MapColor.BROWN));
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH).with(EXTRAGUNPOWDER, 0).with(EXTRABLAZEPOWDER, 0));
    }

    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            cached_state = world.getBlockState(pos);
            if (world.isReceivingRedstonePower(pos)) {
                kaboom(world, pos);
                world.removeBlock(pos, false);
            }
        }
    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        cached_state = world.getBlockState(pos);
        if (world.isReceivingRedstonePower(pos)) {
            kaboom(world, pos);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!world.isClient && !player.isCreative()) {
            ItemStack dropped = new ItemStack(ItemInit.BARRELBOMBITEM.get());
            ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dropped);
            world.spawnEntity(item);

            dropped = new ItemStack(Items.GUNPOWDER);
            dropped.setCount(state.get(EXTRAGUNPOWDER));
            item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dropped);
            world.spawnEntity(item);

            dropped = new ItemStack(Items.BLAZE_POWDER);
            dropped.setCount(state.get(EXTRABLAZEPOWDER));
            item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dropped);
            world.spawnEntity(item);
        }
    }

    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion x) {
        Random random = Random.create();
        Vec3d facing = VecItoD(cached_state.get(FACING).getVector());
        int extraGunpowder = cached_state.get(EXTRAGUNPOWDER);
        int extraBlazePowder = cached_state.get(EXTRABLAZEPOWDER);
        BarrelBombEntity.playKaboomSound(world, pos.toCenterPos(), random);
        BarrelBombEntity.spawnSmokeCloud(pos.toCenterPos(), random, world);
        BarrelBombExplosion explosion = new BarrelBombExplosion(world, x.entity, pos.getX(),
                pos.getY() + 0.1, pos.getZ(), ConfigHandler.config().power, false,
                Explosion.DestructionType.DESTROY, facing, extraGunpowder, extraBlazePowder);
        explosion.kaboom();
    }

    public static void kaboom(World world, BlockPos pos) {
        kaboom(world, pos, null);
    }

    private static void kaboom(World world, BlockPos pos, @Nullable LivingEntity igniter) {
        if (!world.isClient) {
            BlockState blockState = world.getBlockState(pos);
            Direction facing = blockState.get(FACING);
            int extraGunpowder = blockState.get(EXTRAGUNPOWDER);
            int extraBlazePowder = blockState.get(EXTRABLAZEPOWDER);
            BarrelBombEntity barrelBomb = new BarrelBombEntity(world, (double)pos.getX() + (double)0.5F, pos.getY(),
                    (double)pos.getZ() + (double)0.5F, facing, igniter, extraGunpowder, extraBlazePowder);
            barrelBomb.setFacing(world.getBlockState(pos).get(FACING));
            world.spawnEntity(barrelBomb);
            world.playSound(null, barrelBomb.getX(), barrelBomb.getY(), barrelBomb.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(igniter, GameEvent.PRIME_FUSE, pos);
        }
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.GUNPOWDER) || itemStack.isOf(Items.BLAZE_POWDER)) {
            IntProperty property = itemStack.isOf(Items.GUNPOWDER) ? EXTRAGUNPOWDER : EXTRABLAZEPOWDER;
            int addativeToAdd = 1;
            if (hand == Hand.OFF_HAND) {
                addativeToAdd = itemStack.getCount();
            }
            int totalExtraAdditives = world.getBlockState(pos).get(EXTRAGUNPOWDER) + world.getBlockState(pos).get(EXTRABLAZEPOWDER);
            int extraAddative = world.getBlockState(pos).get(property);
            addativeToAdd = Math.min(addativeToAdd, ConfigHandler.config().additiveCap - totalExtraAdditives);
            itemStack.decrement(addativeToAdd);
            world.setBlockState(pos, world.getBlockState(pos).with(property, extraAddative + addativeToAdd));
            player.sendMessage(Text.literal("Extra Additives: " + (totalExtraAdditives + addativeToAdd) +
                    "/" + ConfigHandler.config().additiveCap), true);
            cached_state = world.getBlockState(pos);
            return ActionResult.SUCCESS;
        }
        if (itemStack.isOf(Items.FLINT_AND_STEEL) || itemStack.isOf(Items.FIRE_CHARGE)) {
            kaboom(world, pos, player);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
            Item item = itemStack.getItem();
            if (!player.isCreative()) {
                if (itemStack.isOf(Items.FLINT_AND_STEEL)) {
                    itemStack.damage(1, player, (playerx) -> playerx.sendToolBreakStatus(hand));
                } else {
                    itemStack.decrement(1);
                }
            }

            player.incrementStat(Stats.USED.getOrCreateStat(item));
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        if (!world.isClient) {
            BlockPos blockPos = hit.getBlockPos();
            Entity entity = projectile.getOwner();
            if (projectile.isOnFire() && projectile.canModifyAt(world, blockPos)) {
                kaboom(world, blockPos, entity instanceof LivingEntity ? (LivingEntity)entity : null);
                world.removeBlock(blockPos, false);
            }
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getPlayerLookDirection();
        if (ctx.getPlayer() != null && ctx.getPlayer().isSneaking()) {
            direction = direction.getOpposite();
        }
        return this.getDefaultState().with(FACING, direction);
    }

    protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(EXTRAGUNPOWDER);
        builder.add(EXTRABLAZEPOWDER);
    }
}
