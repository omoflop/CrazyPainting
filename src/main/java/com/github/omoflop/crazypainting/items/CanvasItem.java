package com.github.omoflop.crazypainting.items;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.Identifiable;
import com.github.omoflop.crazypainting.content.CrazyComponents;
import com.github.omoflop.crazypainting.entities.CanvasEntity;
import com.github.omoflop.crazypainting.network.types.PaintingSize;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;


public class CanvasItem extends Item implements Identifiable {
    public static final String UNTITLED = "Untitled Painting";

    public final Identifier id;

    public final byte width;
    public final byte height;

    public CanvasItem(String registryName, byte width, byte height) {
        super(new Properties().stacksTo(1).setId(Identifiable.key(registryName))
                .component(DataComponents.LORE, new ItemLore(List.of(), List.of(Component.literal(width + "x" + height).withStyle(ChatFormatting.WHITE))))
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setSwappable(false).setEquipOnInteract(false).build())
        );
        this.id = CrazyPainting.id(registryName);
        this.width = width;
        this.height = height;

    }

    @Override
    public Component getName(ItemStack stack) {
        String title = getTitle(stack);
        if (title.equals(UNTITLED)) return super.getName(stack);
        return Component.literal(title);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction side = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        Level world = context.getLevel();
        ItemStack usageStack = context.getItemInHand();

        if (CanvasItem.getCanvasId(usageStack) == -1) return InteractionResult.PASS;
        BlockState blockState = world.getBlockState(pos);

        //if (tryPlaceBed(usageStack, world, pos, side, blockState)) {
        //    context.getStack().decrementUnlessCreative(1, context.getPlayer());
        //    return ActionResult.SUCCESS;
        //}

        if (tryPlaceSolid(usageStack, world, pos, side, blockState)) {
            context.getItemInHand().consume(1, context.getPlayer());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    //private boolean tryPlaceBed(ItemStack usageStack, World world, BlockPos pos, Direction side, BlockState blockState) {
    //    if (!blockState.isIn(BlockTags.BEDS)) return false;
    //
    //    ItemStack stack = usageStack.copyComponentsToNewStack(usageStack.getItem(), 1);
    //    CanvasEntity entity = CanvasEntity.create(world, stack, pos.add(side.getVector()), side);
    //    world.spawnEntity(entity);
    //
    //    return true;
    //}

    private boolean tryPlaceSolid(ItemStack usageStack, Level world, BlockPos pos, Direction side, BlockState blockState) {
        if (!blockState.isFaceSturdy(world, pos, side, SupportType.CENTER)) return false;

        ItemStack stack = usageStack.transmuteCopy(usageStack.getItem(), 1);
        BlockPos placePos = pos.offset(side.getUnitVec3i());
        CanvasEntity entity = CanvasEntity.create(world, stack, placePos, side);
        world.addFreshEntity(entity);

        return true;
    }


    public static int getCanvasId(ItemStack stack) {
        if (!stack.hasNonDefault(CrazyComponents.CANVAS_DATA)) return -1;
        var component = stack.getComponents().get(CrazyComponents.CANVAS_DATA);
        if (component == null) return -1;
        return component.id();
    }


    public static int getGeneration(ItemStack stack) {
        if (!stack.hasNonDefault(CrazyComponents.CANVAS_DATA)) return -1;
        var component = stack.getComponents().get(CrazyComponents.CANVAS_DATA);
        if (component == null) return -1;
        return component.generation();
    }

    public static boolean getGlow(ItemStack stack) {
        if (!stack.hasNonDefault(CrazyComponents.CANVAS_DATA)) return false;
        var component = stack.getComponents().get(CrazyComponents.CANVAS_DATA);
        if (component == null) return false;
        return component.glow();
    }

    public static @Nullable String getSignedBy(ItemStack stack) {
        if (!stack.hasNonDefault(CrazyComponents.CANVAS_DATA)) return null;

        var component = stack.getComponents().get(CrazyComponents.CANVAS_DATA);
        if (component == null) return null;

        String signedBy = component.signedBy();
        if (signedBy.isEmpty()) return null;

        return signedBy;
    }

    public static String getTitle(ItemStack stack) {
        if (!stack.hasNonDefault(CrazyComponents.CANVAS_DATA)) return UNTITLED;

        var component = stack.getComponents().get(CrazyComponents.CANVAS_DATA);
        if (component == null) return UNTITLED;

        return component.title();
    }

    public PaintingSize getSize() {
        return new PaintingSize(width, height);
    }

    public static boolean isSigned(ItemStack heldStack) {
        return getSignedBy(heldStack) != null;
    }

    @Override
    public Identifier getId() {
        return id;
    }
}
