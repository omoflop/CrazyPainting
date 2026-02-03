package com.github.omoflop.crazypainting.items;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.Identifiable;
import com.github.omoflop.crazypainting.content.CrazyEntities;
import com.github.omoflop.crazypainting.entities.CanvasEaselEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.function.Consumer;

public class EaselItem extends Item implements Identifiable {
    public final Identifier id;

    public EaselItem(String registryName) {
        super(new Settings().maxCount(16).registryKey(Identifiable.key(registryName)));
        this.id = CrazyPainting.id(registryName);
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        Direction direction = context.getSide();
        if (direction == Direction.DOWN) {
            return ActionResult.FAIL;
        } else {
            World world = context.getWorld();
            ItemPlacementContext itemPlacementContext = new ItemPlacementContext(context);
            BlockPos blockPos = itemPlacementContext.getBlockPos();
            ItemStack itemStack = context.getStack();
            Vec3d vec3d = Vec3d.ofBottomCenter(blockPos);
            Box box = CrazyEntities.EASEL_ENTITY_TYPE.getDimensions().getBoxAt(vec3d.getX(), vec3d.getY(), vec3d.getZ());
            if (world.isSpaceEmpty(null, box) && world.getOtherEntities(null, box).isEmpty()) {
                if (world instanceof ServerWorld serverWorld) {
                    Consumer<CanvasEaselEntity> consumer = EntityType.copier(serverWorld, itemStack, context.getPlayer());
                    CanvasEaselEntity easel = CrazyEntities.EASEL_ENTITY_TYPE.create(serverWorld, consumer, blockPos, SpawnReason.SPAWN_ITEM_USE, true, true);
                    if (easel == null) {
                        return ActionResult.FAIL;
                    }

                    float yaw = (float) MathHelper.floor((MathHelper.wrapDegrees(context.getPlayerYaw() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                    easel.refreshPositionAndAngles(easel.getX(), easel.getY(), easel.getZ(), yaw, 0.0F);
                    serverWorld.spawnEntityAndPassengers(easel);
                    world.playSound(null, easel.getX(), easel.getY(), easel.getZ(), SoundEvents.ENTITY_ARMOR_STAND_PLACE, SoundCategory.BLOCKS, 0.75F, 0.8F);
                    easel.emitGameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
                }

                itemStack.decrement(1);
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.FAIL;
            }
        }
    }



    @Override
    public Identifier getId() {
        return id;
    }
}
