package com.github.omoflop.crazypainting.entities;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.components.CanvasDataComponent;
import com.github.omoflop.crazypainting.content.CrazyComponents;
import com.github.omoflop.crazypainting.content.CrazyItems;
import com.github.omoflop.crazypainting.items.CanvasItem;
import com.github.omoflop.crazypainting.items.PaletteItem;
import com.github.omoflop.crazypainting.network.ChangeRecord;
import com.github.omoflop.crazypainting.network.event.PaintingChangeEvent;
import com.github.omoflop.crazypainting.network.s2c.UpdateEaselCanvasIdS2C;
import com.github.omoflop.crazypainting.network.types.ChangeKey;
import com.github.omoflop.crazypainting.network.types.PaintingId;
import com.github.omoflop.crazypainting.state.CanvasManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class CanvasEaselEntity extends LivingEntity {
    public static final TrackedData<ItemStack> CANVAS_ITEM = DataTracker.registerData(CanvasEaselEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    public long lastHitTime;

    public CanvasEaselEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer createAttributes() {
        return createLivingAttributes()
                .add(EntityAttributes.MAX_HEALTH, 4)
                .build();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(CANVAS_ITEM, ItemStack.EMPTY);
        super.initDataTracker(builder);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        // Ignore offhand interactions
        if (hand == Hand.OFF_HAND) return ActionResult.PASS;

        ItemStack displayStack = this.getDisplayStack();
        ItemStack playerHeldStack = player.getStackInHand(hand);

        boolean displayItemIsCanvas = displayStack.getItem() instanceof CanvasItem;

        // If both the display stand is empty and the player held stand is empty, return early
        if (displayStack.isEmpty() && playerHeldStack.isEmpty()) return ActionResult.FAIL;

        // Return true if the player uses any ink on the canvas
        if (displayItemIsCanvas && checkInk(playerHeldStack, displayStack, player)) return ActionResult.SUCCESS;

        // If there's no item and the player is holding one, transfer the item from the player into this
        if (displayStack.isEmpty()) {
            if (!playerHeldStack.isEmpty()) {
                setDisplayStack(playerHeldStack.copy());

                // Only remove the player's held item if they're not in creative mode
                if (!player.isCreative()) {
                    player.setStackInHand(hand, ItemStack.EMPTY);
                }
            }
        } else if (!displayItemIsCanvas || player.isSneaking()) {
            if (hand == Hand.MAIN_HAND && playerHeldStack.isEmpty()) {
                player.setStackInHand(hand, displayStack);
                setDisplayStack(ItemStack.EMPTY);
            }
        } else if (displayStack.getItem() instanceof CanvasItem canvasItem) {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.SUCCESS;

            boolean hasPaletteInEitherHand =
                    player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof PaletteItem ||
                    player.getStackInHand(Hand.OFF_HAND).getItem() instanceof PaletteItem;

            boolean edit = hasPaletteInEitherHand;
            if (CanvasItem.isSigned(displayStack)) edit = false;

            int canvasId = CanvasItem.getCanvasId(displayStack);
            if (canvasId == -1 && hasPaletteInEitherHand) {
                canvasId = CanvasManager.getServerState(Objects.requireNonNull(serverPlayer.getServer())).getNextId();
                displayStack.set(CrazyComponents.CANVAS_DATA, CanvasDataComponent.withId(displayStack.get(CrazyComponents.CANVAS_DATA), canvasId));
                edit = true;
            }

            Optional<ChangeKey> change = Optional.empty();
            if (edit) {
                ChangeRecord changeRecord = new ChangeRecord(ChangeKey.create(), new PaintingId(canvasId));
                change = Optional.of(changeRecord.key());
                CanvasManager.CHANGE_IDS.put(serverPlayer.getUuid(), changeRecord);
            }

            try {
                MinecraftServer server = Objects.requireNonNull(serverPlayer.getServer());
                PaintingChangeEvent changeEvent = new PaintingChangeEvent(change, CanvasManager.createOrLoad(canvasId, canvasItem.getSize(), server), CanvasItem.getTitle(displayStack), this.getId());
                ServerPlayNetworking.send(serverPlayer, changeEvent);
                for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                    ServerPlayNetworking.send(serverPlayerEntity, new UpdateEaselCanvasIdS2C(this.getId(), new PaintingId(canvasId)));
                }
            } catch (IOException ignored) {

            }
        }


        return super.interact(player, hand);
    }

    private boolean checkInk(ItemStack playerHeldStack, ItemStack displayStack, PlayerEntity player) {
        boolean holdingGlowItem = playerHeldStack.getItem() == CrazyPainting.GLOW_ITEM;
        boolean holdingUnGlowItem = playerHeldStack.getItem() == CrazyPainting.UNGLOW_ITEM;

        // Check if the player is using
        if (holdingGlowItem || holdingUnGlowItem) {
            CanvasDataComponent data = displayStack.get(CrazyComponents.CANVAS_DATA);

            boolean success = false;
            if (data == null) {
                data = CanvasDataComponent.DEFAULT.withGlow(holdingGlowItem);

                success = true;
            } else if (data.glow() == holdingUnGlowItem) {
                data = data.withGlow(holdingGlowItem);
                success = true;
            }

            if (success) {
                displayStack.set(CrazyComponents.CANVAS_DATA, data);
                player.playSound(holdingGlowItem ? SoundEvents.ITEM_GLOW_INK_SAC_USE : SoundEvents.ITEM_INK_SAC_USE);
                playerHeldStack.decrementUnlessCreative(1, player);
                return true;
            }
        }

        return false;
    }

    @Override
    protected void pushAway(Entity entity) { }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public Text getDisplayName() {
        return getStackInHand(Hand.MAIN_HAND).getFormattedName();
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    protected void turnHead(float bodyRotation) {
        this.lastBodyYaw = this.lastYaw;
        this.bodyYaw = this.getYaw();
    }

    public void setBodyYaw(float bodyYaw) {
        this.lastBodyYaw = this.lastYaw = bodyYaw;
        this.lastHeadYaw = this.headYaw = bodyYaw;
    }

    public void setHeadYaw(float headYaw) {
        this.lastBodyYaw = this.lastYaw = headYaw;
        this.lastHeadYaw = this.headYaw = headYaw;
    }

    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (source.isSourceCreativePlayer()) {
            this.breakAndDropItem(world, source, true);
            this.remove(RemovalReason.KILLED);
        } else if (this.isRemoved()) {
            return false;
        } else if (!world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && source.getAttacker() instanceof MobEntity) {
            return false;
        } else if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.breakAndDropItem(world, source, true);
            this.remove(RemovalReason.KILLED);
            return false;
        } else if (!this.isInvulnerableTo(world, source)) {
            if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
                this.breakAndDropItem(world, source, false);
                this.remove(RemovalReason.KILLED);
                return false;
            } else if (source.isIn(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
                if (this.isOnFire()) {
                    this.updateHealth(world, source, 0.15F);
                } else {
                    this.setOnFireFor(5.0F);
                }

                return false;
            } else if (source.isIn(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5F) {
                this.updateHealth(world, source, 4.0F);
                return false;
            } else {
                boolean bl = source.isIn(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
                boolean bl2 = source.isIn(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
                if (!bl && !bl2) {
                    return false;
                } else {
                    Entity attacker = source.getAttacker();
                    if (attacker instanceof PlayerEntity playerEntity) {
                        if (!playerEntity.getAbilities().allowModifyWorld) {
                            return false;
                        }
                    }

                    if (source.isSourceCreativePlayer()) {
                        this.playBreakSound();
                        this.spawnBreakParticles();
                        this.remove(RemovalReason.KILLED);
                        return true;
                    } else {
                        long l = world.getTime();
                        if (l - this.lastHitTime > 5L && !bl2) {
                            world.sendEntityStatus(this, (byte)32);
                            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
                            this.lastHitTime = l;
                        } else {
                            this.breakAndDropItem(world, source, false);
                            this.spawnBreakParticles();
                            this.remove(RemovalReason.KILLED);
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void handleStatus(byte status) {
        if (status == 32) {
            if (this.getWorld().isClient) {
                this.getWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_HIT, this.getSoundCategory(), 0.3F, 1.0F, false);
                this.lastHitTime = this.getWorld().getTime();
            }
        } else {
            super.handleStatus(status);
        }
    }

    public boolean shouldRender(double distance) {
        double d = this.getBoundingBox().getAverageSideLength() * (double)4.0F;
        if (Double.isNaN(d) || d == (double)0.0F) {
            d = 4.0F;
        }

        d *= 64.0F;
        return distance < d * d;
    }

    private void spawnBreakParticles() {
        if (this.getWorld() instanceof ServerWorld) {
            ((ServerWorld)this.getWorld()).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.getDefaultState()), this.getX(), this.getBodyY(0.6666666666666666), this.getZ(), 10, (double)(this.getWidth() / 4.0F), (double)(this.getHeight() / 4.0F), (double)(this.getWidth() / 4.0F), 0.05);
        }

    }

    private void updateHealth(ServerWorld world, DamageSource damageSource, float amount) {
        float f = this.getHealth();
        f -= amount;
        if (f <= 0.5F) {
            breakAndDropItem(world, damageSource, false);
            this.kill(world);
        } else {
            this.setHealth(f);
            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getAttacker());
        }

    }

    private void breakAndDropItem(ServerWorld world, DamageSource damageSource, boolean isCreative) {
        if (!isCreative) {
            Block.dropStack(this.getWorld(), this.getBlockPos(), getEaselItemStack());
        }
        Block.dropStack(this.getWorld(), this.getBlockPos(), getDisplayStack());
        this.onBreak(world, damageSource);
    }

    private ItemStack getEaselItemStack() {
        ItemStack itemStack = new ItemStack(CrazyItems.EASEL_ITEM);
        itemStack.set(DataComponentTypes.CUSTOM_NAME, this.getCustomName());
        return itemStack;
    }

    private void onBreak(ServerWorld world, DamageSource damageSource) {
        this.playBreakSound();
        this.drop(world, damageSource);
    }

    @Override
    public @Nullable ItemStack getPickBlockStack() {
        ItemStack display = getDisplayStack();
        if (display.isEmpty()) return getEaselItemStack();
        return display.copy();
    }

    private void playBreakSound() {
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_BREAK, this.getSoundCategory(), 1.0F, 1.0F);
    }

    public ItemStack getDisplayStack() {
        return dataTracker.get(CANVAS_ITEM);
    }

    public void setDisplayStack(ItemStack value) {
        this.setAsStackHolder(value);
        this.getDataTracker().set(CANVAS_ITEM, value);
    }

    private void setAsStackHolder(ItemStack stack) {
        if (!stack.isEmpty() && stack.getFrame() == null) {
            stack.setHolder(this);
        }
    }

    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        ItemStack itemStack = this.getDisplayStack();
        if (!itemStack.isEmpty()) {
            view.put("Item", ItemStack.CODEC, itemStack);
        }
    }

    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        ItemStack itemStack = view.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);

        this.setDisplayStack(itemStack);
    }
}
