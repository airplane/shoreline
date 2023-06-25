package com.caspian.client.mixin.network;

import com.caspian.client.Caspian;
import com.caspian.client.api.event.EventStage;
import com.caspian.client.impl.event.network.MovementPacketsEvent;
import com.caspian.client.impl.event.network.MovementSlowdownEvent;
import com.caspian.client.impl.event.network.SetCurrentHandEvent;
import com.caspian.client.util.Globals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 
 * 
 * @author linus
 * @since 1.0
 * 
 * @see ClientPlayerEntity
 */
@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity
        implements Globals
{
    //
    @Shadow
    protected abstract void sendSprintingPacket();
    //
    @Shadow
    public abstract boolean isSneaking();
    @Shadow
    protected abstract boolean isCamera();
    //
    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;
    // Last tick values
    @Shadow
    private boolean lastSneaking;
    @Shadow
    private double lastX;
    @Shadow
    private double lastBaseY;
    @Shadow
    private double lastZ;
    @Shadow
    private float lastYaw;
    @Shadow
    private float lastPitch;
    @Shadow
    private boolean lastOnGround;
    //
    @Shadow
    private int ticksSinceLastPositionPacketSent;
    @Shadow
    private boolean autoJumpEnabled;
    //
    @Shadow
    @Final
    protected MinecraftClient client;
    @Shadow
    public Input input;

    /**
     * 
     */
    public MixinClientPlayerEntity() 
    {
        // Treating this class as ClientPlayerEntity with mc.player info works
        // Need a better solution with less bullshit
        super(MinecraftClient.getInstance().world,
                MinecraftClient.getInstance().player.getGameProfile());
    }

    /**
     *
     *
     * @param ci
     */
    @Inject(method = "sendMovementPackets", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookSendMovementPackets(CallbackInfo ci)
    {
        MovementPacketsEvent movementPacketsEvent =
                new MovementPacketsEvent(mc.player.getX(), mc.player.getY(),
                        mc.player.getZ(), mc.player.getYaw(),
                        mc.player.getPitch(), mc.player.isOnGround());
        movementPacketsEvent.setStage(EventStage.PRE);
        Caspian.EVENT_HANDLER.dispatch(movementPacketsEvent);
        ci.cancel();
        sendSprintingPacket();
        boolean bl = isSneaking();
        if (bl != lastSneaking)
        {
            ClientCommandC2SPacket.Mode mode = bl ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY :
                    ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
            networkHandler.sendPacket(new ClientCommandC2SPacket(this,
                    mode));
            lastSneaking = bl;
        }
        if (isCamera())
        {
            double d = getX() - lastX;
            double e = getY() - lastBaseY;
            double f = getZ() - lastZ;
            double g = getYaw() - lastYaw;
            double h = getPitch() - lastPitch;
            ++ticksSinceLastPositionPacketSent;
            boolean bl2 = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0e-4)
                    || ticksSinceLastPositionPacketSent >= 20;
            boolean bl3 = g != 0.0 || h != 0.0;
            if (hasVehicle())
            {
                Vec3d vec3d = getVelocity();
                networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                        vec3d.x, -999.0, vec3d.z, getYaw(),
                        getPitch(), onGround));
                bl2 = false;
            }
            else if (bl2 && bl3)
            {
                networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                        getX(), getY(), getZ(), getYaw(), getPitch(),
                        onGround));
            }
            else if (bl2)
            {
                networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        getX(), getY(), getZ(), onGround));
            }
            else if (bl3)
            {
                networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                        getYaw(), getPitch(), onGround));
            }
            else if (lastOnGround != onGround)
            {
                networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(
                        onGround));
            }
            if (bl2)
            {
                lastX = getX();
                lastBaseY = getY();
                lastZ = getZ();
                ticksSinceLastPositionPacketSent = 0;
            }
            if (bl3)
            {
                lastYaw = getYaw();
                lastPitch = getPitch();
            }
            lastOnGround = onGround;
            autoJumpEnabled = client.options.getAutoJump().getValue();
        }
        movementPacketsEvent.setStage(EventStage.POST);
        Caspian.EVENT_HANDLER.dispatch(movementPacketsEvent);
    }
    
    /**
     *
     *
     * @param ci
     */
    @Inject(method = "tickMovement", at = @At(value = "FIELD", target =
            "Lnet/minecraft/client/network/ClientPlayerEntity;" +
                    "ticksLeftToDoubleTapSprint:I", shift = At.Shift.AFTER))
    private void hookTickMovementPost(CallbackInfo ci)
    {
        MovementSlowdownEvent movementUpdateEvent =
                new MovementSlowdownEvent(input);
        Caspian.EVENT_HANDLER.dispatch(movementUpdateEvent);
    }
    
    /**
     *
     *
     * @param hand
     * @param ci
     */
    @Inject(method = "setCurrentHand", at = @At(value = "HEAD"))
    private void hookSetCurrentHand(Hand hand, CallbackInfo ci)
    {
        SetCurrentHandEvent setCurrentHandEvent = new SetCurrentHandEvent();
        Caspian.EVENT_HANDLER.dispatch(setCurrentHandEvent);
    }
}