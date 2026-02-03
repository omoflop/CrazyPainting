package com.github.omoflop.crazypainting.entities;

import com.github.omoflop.crazypainting.content.CrazyEntities;
import com.github.omoflop.crazypainting.items.CanvasItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class CanvasEntity extends HangingEntity {
    public static final EntityDataAccessor<ItemStack> CANVAS_ITEM = SynchedEntityData.defineId(CanvasEntity.class, EntityDataSerializers.ITEM_STACK);
    public static final EntityDataAccessor<Byte> ROTATION = SynchedEntityData.defineId(CanvasEntity.class, EntityDataSerializers.BYTE);

    public CanvasEntity(EntityType<? extends HangingEntity> entityType, Level world) {
        super(entityType, world);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CANVAS_ITEM, ItemStack.EMPTY);
        builder.define(ROTATION, (byte)0);
    }

    public static CanvasEntity create(Level world, ItemStack stack, BlockPos pos, Direction facing) {
        CanvasEntity entity = new CanvasEntity(CrazyEntities.CANVAS_ENTITY_TYPE, world);
        entity.setPos(pos.getCenter());
        entity.setHeldItemStack(stack);
        entity.setDirection(facing);
        return entity;
    }

    private double offs(int l) {
        return l % 32 == 0 ? 0.5D : 0.0D;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (getHeldItemStack().getItem() instanceof CanvasItem canvasItem) {

            if (canvasItem.width == canvasItem.height) {
                setRotation((byte) (getItemRotation() + 1));
            } else {
                setRotation((byte) (getItemRotation() + 2));
            }
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction side) {
        float width = 1;
        float height = 1;

        if (entityData.get(CANVAS_ITEM).getItem() instanceof CanvasItem canvas) {
            width = canvas.width;
            height = canvas.height;
        }

        float widthTweak = width % 2 == 0 ? 0.5f : 0;
        float heightTweak = height % 2 == 0 ? 0.5f : 0;

        final float distFromWall = 0.46f;
        final float pixel = 1/16f;

        width -= pixel*2.35f;
        height -= pixel*2.35f;

        if (side == Direction.UP) {
            return AABB.ofSize(pos.getCenter().subtract(widthTweak, distFromWall, heightTweak), width, pixel, height);
        } else if (side == Direction.DOWN) {
            return AABB.ofSize(pos.getCenter().subtract(widthTweak, -distFromWall, heightTweak), width, pixel, height);
        } else if (side == Direction.NORTH) {
            return AABB.ofSize(pos.getCenter().subtract(widthTweak, heightTweak, -distFromWall), width, height, pixel);
        }  else if (side == Direction.EAST) {
            return AABB.ofSize(pos.getCenter().subtract(distFromWall, heightTweak, widthTweak), pixel, height, width);
        } else if (side == Direction.SOUTH) {
            return AABB.ofSize(pos.getCenter().subtract(widthTweak, heightTweak, distFromWall), width, height, pixel);
        } else if (side == Direction.WEST) {
            return AABB.ofSize(pos.getCenter().subtract(-distFromWall, heightTweak, widthTweak), pixel, height, width);
        }

        return AABB.ofSize(pos.getCenter(), 1, 1, 1);
    }

    protected void setDirection(Direction facing) {
        super.setDirectionRaw(facing);
        if (facing.getAxis().isHorizontal()) {
            this.setXRot(0.0F);
            this.setYRot((float)(facing.get2DDataValue() * 90));
        } else {
            this.setXRot((float)(-90 * facing.getAxisDirection().getStep()));
            this.setYRot(0.0F);
        }

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return getHeldItemStack().copy();
    }

    @Override
    public void playPlacementSound() {

    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void dropItem(ServerLevel world, @Nullable Entity breaker) {
        if (breaker instanceof Player player && player.isCreative()) return;
        spawnAtLocation(world, entityData.get(CANVAS_ITEM));
    }

    public ItemStack getHeldItemStack() {
        return entityData.get(CANVAS_ITEM);
    }

    public void setHeldItemStack(ItemStack value) {
        if (!value.isEmpty()) {
            value = value.copyWithCount(1);
        }

        this.setAsStackHolder(value);
        this.getEntityData().set(CANVAS_ITEM, value);
    }

    private void setAsStackHolder(ItemStack stack) {
        if (!stack.isEmpty() && stack.getFrame() == null) {
            stack.setEntityRepresentation(this);
        }

        this.recalculateBoundingBox();
    }

    public byte getItemRotation() {
        return this.getEntityData().get(ROTATION);
    }

    public void setItemRotation(byte rotation) {
        this.getEntityData().set(ROTATION, rotation);
    }

    public void setRotation(byte value) {
        this.getEntityData().set(ROTATION, (byte)(value % 4));
    }

    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        ItemStack itemStack = this.getHeldItemStack();
        if (!itemStack.isEmpty()) view.store("Item", ItemStack.CODEC, itemStack);

        view.putByte("ItemRotation", this.getItemRotation());
        view.store("Facing", Direction.LEGACY_ID_CODEC, this.getNearestViewDirection());
    }

    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        ItemStack itemStack = view.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);

        this.setHeldItemStack(itemStack);
        this.setItemRotation(view.getByteOr("ItemRotation", (byte)0));
        this.setDirection(view.read("Facing", Direction.LEGACY_ID_CODEC).orElse(Direction.DOWN));
    }

}
