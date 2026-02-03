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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class CanvasEaselEntity extends LivingEntity {
    public static final EntityDataAccessor<ItemStack> CANVAS_ITEM = SynchedEntityData.defineId(CanvasEaselEntity.class, EntityDataSerializers.ITEM_STACK);

    public long lastHitTime;

    public CanvasEaselEntity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier createAttributes() {
        return createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 4)
                .build();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CANVAS_ITEM, ItemStack.EMPTY);
        super.defineSynchedData(builder);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        // Ignore offhand interactions
        if (hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;

        ItemStack displayStack = this.getDisplayStack();
        ItemStack playerHeldStack = player.getItemInHand(hand);

        boolean displayItemIsCanvas = displayStack.getItem() instanceof CanvasItem;

        // If both the display stand is empty and the player held stand is empty, return early
        if (displayStack.isEmpty() && playerHeldStack.isEmpty()) return InteractionResult.FAIL;

        // Return true if the player uses any ink on the canvas
        if (displayItemIsCanvas && checkInk(playerHeldStack, displayStack, player)) return InteractionResult.SUCCESS;

        // If there's no item and the player is holding one, transfer the item from the player into this
        if (displayStack.isEmpty()) {
            if (!playerHeldStack.isEmpty()) {
                setDisplayStack(playerHeldStack.copy());

                // Only remove the player's held item if they're not in creative mode
                if (!player.isCreative()) {
                    player.setItemInHand(hand, ItemStack.EMPTY);
                }
            }
        } else if (!displayItemIsCanvas || player.isShiftKeyDown()) {
            if (hand == InteractionHand.MAIN_HAND && playerHeldStack.isEmpty()) {
                player.setItemInHand(hand, displayStack);
                setDisplayStack(ItemStack.EMPTY);
            }
        } else if (displayStack.getItem() instanceof CanvasItem canvasItem) {
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;

            boolean hasPaletteInEitherHand =
                    player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PaletteItem ||
                    player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof PaletteItem;

            boolean edit = hasPaletteInEitherHand;
            if (CanvasItem.isSigned(displayStack)) edit = false;

            int canvasId = CanvasItem.getCanvasId(displayStack);
            if (canvasId == -1 && hasPaletteInEitherHand) {
                MinecraftServer server = Objects.requireNonNull(serverPlayer.level()).getServer();
                canvasId = CanvasManager.getServerState(server).getNextId();
                displayStack.set(CrazyComponents.CANVAS_DATA, CanvasDataComponent.withId(displayStack.get(CrazyComponents.CANVAS_DATA), canvasId));
                edit = true;
            }

            Optional<ChangeKey> change = Optional.empty();
            if (edit) {
                ChangeRecord changeRecord = new ChangeRecord(ChangeKey.create(), new PaintingId(canvasId));
                change = Optional.of(changeRecord.key());
                CanvasManager.CHANGE_IDS.put(serverPlayer.getUUID(), changeRecord);
            }

            try {
                MinecraftServer server = Objects.requireNonNull(serverPlayer.level()).getServer();
                PaintingChangeEvent changeEvent = new PaintingChangeEvent(change, CanvasManager.createOrLoad(canvasId, canvasItem.getSize(), server), CanvasItem.getTitle(displayStack), this.getId());
                ServerPlayNetworking.send(serverPlayer, changeEvent);
                for (ServerPlayer serverPlayerEntity : server.getPlayerList().getPlayers()) {
                    ServerPlayNetworking.send(serverPlayerEntity, new UpdateEaselCanvasIdS2C(this.getId(), new PaintingId(canvasId)));
                }
            } catch (IOException ignored) {

            }
        }


        return super.interact(player, hand);
    }

    private boolean checkInk(ItemStack playerHeldStack, ItemStack displayStack, Player player) {
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
                player.makeSound(holdingGlowItem ? SoundEvents.GLOW_INK_SAC_USE : SoundEvents.INK_SAC_USE);
                playerHeldStack.consume(1, player);
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doPush(Entity entity) { }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public Component getDisplayName() {
        return getItemInHand(InteractionHand.MAIN_HAND).getStyledHoverName();
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    protected void tickHeadTurn(float bodyRotation) {
        this.yBodyRotO = this.yRotO;
        this.yBodyRot = this.getYRot();
    }

    public void setYBodyRot(float bodyYaw) {
        this.yBodyRotO = this.yRotO = bodyYaw;
        this.yHeadRotO = this.yHeadRot = bodyYaw;
    }

    public void setYHeadRot(float headYaw) {
        this.yBodyRotO = this.yRotO = headYaw;
        this.yHeadRotO = this.yHeadRot = headYaw;
    }

    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        if (source.isCreativePlayer()) {
            this.breakAndDropItem(world, source, true);
            this.remove(RemovalReason.KILLED);
        } else if (this.isRemoved()) {
            return false;
        } else if (!world.getGameRules().get(GameRules.MOB_GRIEFING) && source.getEntity() instanceof Mob) {
            return false;
        } else if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.breakAndDropItem(world, source, true);
            this.remove(RemovalReason.KILLED);
            return false;
        } else if (!this.isInvulnerableTo(world, source)) {
            if (source.is(DamageTypeTags.IS_EXPLOSION)) {
                this.breakAndDropItem(world, source, false);
                this.remove(RemovalReason.KILLED);
                return false;
            } else if (source.is(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
                if (this.isOnFire()) {
                    this.updateHealth(world, source, 0.15F);
                } else {
                    this.igniteForSeconds(5.0F);
                }

                return false;
            } else if (source.is(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5F) {
                this.updateHealth(world, source, 4.0F);
                return false;
            } else {
                boolean bl = source.is(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
                boolean bl2 = source.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
                if (!bl && !bl2) {
                    return false;
                } else {
                    Entity attacker = source.getEntity();
                    if (attacker instanceof Player playerEntity) {
                        if (!playerEntity.getAbilities().mayBuild) {
                            return false;
                        }
                    }

                    if (source.isCreativePlayer()) {
                        this.playBreakSound();
                        this.spawnBreakParticles();
                        this.remove(RemovalReason.KILLED);
                        return true;
                    } else {
                        long l = world.getGameTime();
                        if (l - this.lastHitTime > 5L && !bl2) {
                            world.broadcastEntityEvent(this, (byte)32);
                            this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
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

    public void handleEntityEvent(byte status) {
        if (status == 32) {
            Level level = level();
            if (level.isClientSide()) {
                level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_HIT, this.getSoundSource(), 0.3F, 1.0F, false);
                this.lastHitTime = level.getGameTime();
            }
        } else {
            super.handleEntityEvent(status);
        }
    }

    public boolean shouldRenderAtSqrDistance(double distance) {
        double d = this.getBoundingBox().getSize() * (double)4.0F;
        if (Double.isNaN(d) || d == (double)0.0F) {
            d = 4.0F;
        }

        d *= 64.0F;
        return distance < d * d;
    }

    private void spawnBreakParticles() {
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), this.getX(), this.getY(0.6666666666666666), this.getZ(), 10, (double)(this.getBbWidth() / 4.0F), (double)(this.getBbHeight() / 4.0F), (double)(this.getBbWidth() / 4.0F), 0.05);
        }

    }

    private void updateHealth(ServerLevel world, DamageSource damageSource, float amount) {
        float f = this.getHealth();
        f -= amount;
        if (f <= 0.5F) {
            breakAndDropItem(world, damageSource, false);
            this.kill(world);
        } else {
            this.setHealth(f);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
        }

    }

    private void breakAndDropItem(ServerLevel world, DamageSource damageSource, boolean isCreative) {
        if (!isCreative) {
            Block.popResource(this.level(), this.blockPosition(), getEaselItemStack());
        }
        Block.popResource(this.level(), this.blockPosition(), getDisplayStack());
        this.onBreak(world, damageSource);
    }

    private ItemStack getEaselItemStack() {
        ItemStack itemStack = new ItemStack(CrazyItems.EASEL_ITEM);
        itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        return itemStack;
    }

    private void onBreak(ServerLevel world, DamageSource damageSource) {
        this.playBreakSound();
        this.dropAllDeathLoot(world, damageSource);
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        ItemStack display = getDisplayStack();
        if (display.isEmpty()) return getEaselItemStack();
        return display.copy();
    }

    private void playBreakSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK, this.getSoundSource(), 1.0F, 1.0F);
    }

    public ItemStack getDisplayStack() {
        return entityData.get(CANVAS_ITEM);
    }

    public void setDisplayStack(ItemStack value) {
        this.setAsStackHolder(value);
        this.getEntityData().set(CANVAS_ITEM, value);
    }

    private void setAsStackHolder(ItemStack stack) {
        if (!stack.isEmpty() && stack.getFrame() == null) {
            stack.setEntityRepresentation(this);
        }
    }

    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        ItemStack itemStack = this.getDisplayStack();
        if (!itemStack.isEmpty()) {
            view.store("Item", ItemStack.CODEC, itemStack);
        }
    }

    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        ItemStack itemStack = view.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);

        this.setDisplayStack(itemStack);
    }
}
