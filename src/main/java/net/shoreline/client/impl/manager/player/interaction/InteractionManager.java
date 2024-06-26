package net.shoreline.client.impl.manager.player.interaction;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.shoreline.client.Shoreline;
import net.shoreline.client.init.Managers;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.world.SneakBlocks;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Set;

/**
 * @author xgraza
 * @since 1.0
 */
public final class InteractionManager implements Globals
{
    public InteractionManager()
    {
        Shoreline.EVENT_HANDLER.subscribe(this);
    }

    public boolean placeBlock(final BlockPos pos,
                              final int slot,
                              final boolean strictDirection,
                              final boolean clientSwing,
                              final RotationCallback rotationCallback)
    {
        Direction direction = getInteractDirection(pos, strictDirection);
        if (Modules.AIR_PLACE.isEnabled() && direction == null && !strictDirection)
        {
            // TODO: this should be not like this
            direction = Direction.UP;
        }
        if (direction == null)
        {
            return false;
        }
        final BlockPos neighbor = pos.offset(direction.getOpposite());
        return placeBlock(neighbor, direction, slot, clientSwing, rotationCallback);
    }

    public boolean placeBlockPacket(final BlockPos pos,
                                    final int slot,
                                    final boolean strictDirection,
                                    final boolean clientSwing,
                                    final RotationCallback rotationCallback)
    {
        Direction direction = getInteractDirection(pos, strictDirection);
        if (Modules.AIR_PLACE.isEnabled() && direction == null && !strictDirection)
        {
            // TODO: this should be not like this
            direction = Direction.UP;
        }
        if (direction == null)
        {
            return false;
        }
        final BlockPos neighbor = pos.offset(direction.getOpposite());
        return placeBlockPacket(neighbor, direction, slot, clientSwing, rotationCallback);
    }

    public boolean placeBlock(final BlockPos pos,
                              final Direction direction,
                              final int slot,
                              final boolean clientSwing,
                              final RotationCallback rotationCallback)
    {
        Vec3d hitVec = pos.toCenterPos().add(new Vec3d(direction.getUnitVector()).multiply(0.5));
        return placeBlock(new BlockHitResult(hitVec, direction, pos, false),
                slot, clientSwing, rotationCallback);
    }

    public boolean placeBlockPacket(final BlockPos pos,
                              final Direction direction,
                              final int slot,
                              final boolean clientSwing,
                              final RotationCallback rotationCallback)
    {
        Vec3d hitVec = pos.toCenterPos().add(new Vec3d(direction.getUnitVector()).multiply(0.5));
        return placeBlockPacket(new BlockHitResult(hitVec, direction, pos, false),
                slot, clientSwing, rotationCallback);
    }

    public boolean placeBlock(final BlockHitResult hitResult,
                              final int slot,
                              final boolean clientSwing,
                              final RotationCallback rotationCallback)
    {
        final boolean isSpoofing = slot != Managers.INVENTORY.getServerSlot();
        if (isSpoofing)
        {
            Managers.INVENTORY.setSlot(slot);
            // mc.player.getInventory().selectedSlot = slot;
        }

        final boolean isRotating = rotationCallback != null;
        if (isRotating)
        {
            float[] angles = RotationUtil.getRotationsTo(mc.player.getEyePos(), hitResult.getPos());
            rotationCallback.handleRotation(true, angles);
        }

        final boolean result = placeBlockImmediately(hitResult, clientSwing);
        if (isRotating)
        {
            float[] angles = RotationUtil.getRotationsTo(mc.player.getEyePos(), hitResult.getPos());
            rotationCallback.handleRotation(false, angles);
        }

        if (isSpoofing)
        {
            Managers.INVENTORY.syncToClient();
            //mc.player.getInventory().selectedSlot = previousSlot;
        }

        return result;
    }

    public boolean placeBlockPacket(final BlockHitResult hitResult,
                              final int slot,
                              final boolean clientSwing,
                              final RotationCallback rotationCallback)
    {
        final int previousSlot = mc.player.getInventory().selectedSlot;
        final boolean isSpoofing = slot != previousSlot;
        if (isSpoofing)
        {
            Managers.INVENTORY.setSlot(slot);
            // mc.player.getInventory().selectedSlot = slot;
        }

        final boolean isRotating = rotationCallback != null;
        if (isRotating)
        {
            float[] angles = RotationUtil.getRotationsTo(mc.player.getEyePos(), hitResult.getPos());
            rotationCallback.handleRotation(true, angles);
        }

        final boolean result = placeBlockImmediatelyPacket(hitResult, clientSwing);
        if (isRotating)
        {
            float[] angles = RotationUtil.getRotationsTo(mc.player.getEyePos(), hitResult.getPos());
            rotationCallback.handleRotation(false, angles);
        }

        if (isSpoofing)
        {
            Managers.INVENTORY.syncToClient();
            //mc.player.getInventory().selectedSlot = previousSlot;
        }

        return result;
    }

    public boolean placeBlockImmediately(final BlockHitResult result, final boolean clientSwing)
    {
        final BlockState state = mc.world.getBlockState(result.getBlockPos());
        final boolean shouldSneak = SneakBlocks.isSneakBlock(state) && !mc.player.isSneaking();
        if (shouldSneak)
        {
            
        }

        final ActionResult actionResult = placeBlockInternally(result);
        if (actionResult.isAccepted() && actionResult.shouldSwingHand())
        {
            if (clientSwing)
            {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            else
            {
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
        if (shouldSneak)
        {

        }
        return actionResult.isAccepted();
    }

    public boolean placeBlockImmediatelyPacket(final BlockHitResult result, final boolean clientSwing)
    {
        final BlockState state = mc.world.getBlockState(result.getBlockPos());
        final boolean shouldSneak = SneakBlocks.isSneakBlock(state) && !mc.player.isSneaking();
        if (shouldSneak)
        {

        }

        final ActionResult actionResult = placeBlockPacket(result);
        if (actionResult.isAccepted() && actionResult.shouldSwingHand())
        {
            if (clientSwing)
            {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            else
            {
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
        if (shouldSneak)
        {

        }
        return actionResult.isAccepted();
    }

    private ActionResult placeBlockInternally(final BlockHitResult hitResult)
    {
        return mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
    }

    private ActionResult placeBlockPacket(final BlockHitResult hitResult)
    {
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, id));
        return ActionResult.SUCCESS;
    }

    /**
     * @param blockPos
     * @param strictDirection
     * @return
     */
    public Direction getInteractDirection(final BlockPos blockPos, final boolean strictDirection)
    {
        Set<Direction> ncpDirections = Managers.NCP.getPlaceDirectionsNCP(mc.player.getEyePos(), blockPos.toCenterPos());
        Direction interactDirection = null;
        for (final Direction direction : Direction.values())
        {
            final BlockState state = mc.world.getBlockState(blockPos.offset(direction));
            if (state.isAir() || !state.getFluidState().isEmpty())
            {
                continue;
            }
            if (strictDirection && !ncpDirections.contains(direction.getOpposite()))
            {
                continue;
            }
            interactDirection = direction;
            break;
        }
        if (interactDirection == null)
        {
            return null;
        }
        return interactDirection.getOpposite();
    }
}
