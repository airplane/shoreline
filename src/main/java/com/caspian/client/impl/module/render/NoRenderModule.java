package com.caspian.client.impl.module.render;

import com.caspian.client.api.config.Config;
import com.caspian.client.api.config.setting.BooleanConfig;
import com.caspian.client.api.config.setting.EnumConfig;
import com.caspian.client.api.event.EventStage;
import com.caspian.client.api.event.listener.EventListener;
import com.caspian.client.api.module.ModuleCategory;
import com.caspian.client.api.module.ToggleModule;
import com.caspian.client.impl.event.TickEvent;
import com.caspian.client.impl.event.chunk.light.RenderSkylightEvent;
import com.caspian.client.impl.event.gui.hud.RenderOverlayEvent;
import com.caspian.client.impl.event.particle.ParticleEvent;
import com.caspian.client.impl.event.render.HurtCamEvent;
import com.caspian.client.impl.event.render.RenderFloatingItemEvent;
import com.caspian.client.impl.event.render.RenderFogEvent;
import com.caspian.client.impl.event.render.RenderNauseaEvent;
import com.caspian.client.impl.event.render.block.RenderTileEntityEvent;
import com.caspian.client.impl.event.render.entity.RenderArmorEvent;
import com.caspian.client.impl.event.render.entity.RenderFireworkRocketEvent;
import com.caspian.client.impl.event.render.entity.RenderItemEvent;
import com.caspian.client.impl.event.render.entity.RenderWitherSkullEvent;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;

/**
 *
 *
 * @author linus
 * @since 1.0
 */
public class NoRenderModule extends ToggleModule
{
    //
    Config<Boolean> hurtCamConfig = new BooleanConfig("NoHurtCam",
            "Prevents the hurt camera shake effect from rendering", true);
    Config<Boolean> armorConfig = new BooleanConfig("Armor",
            "Prevents armor pieces from rendering", false);
    Config<Boolean> fireOverlayConfig = new BooleanConfig("Overlay-Fire",
            "Prevents the fire Hud overlay from rendering", true);
    Config<Boolean> waterOverlayConfig = new BooleanConfig("Overlay-Water",
            "Prevents the water Hud overlay from rendering", true);
    Config<Boolean> blockOverlayConfig = new BooleanConfig("Overlay-Block",
            "Prevents the block Hud overlay from rendering", true);
    Config<Boolean> spyglassOverlayConfig = new BooleanConfig("Overlay-Spyglass",
            "Prevents the spyglass Hud overlay from rendering", false);
    Config<Boolean> pumpkinOverlayConfig = new BooleanConfig("Overlay-Pumpkin",
            "Prevents the pumpkin Hud overlay from rendering", true);
    Config<Boolean> bossOverlayConfig = new BooleanConfig("Overlay-BossBar",
            "Prevents the boss bar Hud overlay from rendering", true);
    Config<Boolean> nauseaConfig = new BooleanConfig("Nausea",
            "Prevents nausea effect from rendering (includes portal effect)", false);
    Config<Boolean> blindnessConfig = new BooleanConfig("Blindness",
            "Prevents blindness effect from rendering", false);
    Config<Boolean> frostbiteConfig = new BooleanConfig("Frostbite",
            "Prevents frostbite effect from rendering", false);
    Config<Boolean> skylightConfig = new BooleanConfig("Skylight",
            "Prevents skylight from rendering", true);
    Config<Boolean> witherSkullsConfig = new BooleanConfig("WitherSkulls",
            "Prevents flying wither skulls from rendering", false);
    Config<Boolean> tileEntitiesConfig = new BooleanConfig("TileEntities",
            "Prevents special tile entity properties from rendering (i.e. " +
                    "enchantment table books or cutting table saws)", false);
    Config<Boolean> fireworksConfig = new BooleanConfig("Fireworks",
            "Prevents firework particles from rendering", true);
    Config<Boolean> explosionsConfig = new BooleanConfig("Explosions",
            "Prevents explosion particles from rendering", true);
    Config<Boolean> campfiresConfig = new BooleanConfig("Campfires",
            "Prevents campfire particles from rendering", false);
    Config<Boolean> totemConfig = new BooleanConfig("Totems",
            "Prevents totem particles from rendering", false);
    Config<FogRender> fogConfig = new EnumConfig<>("Fog", "Prevents fog from " +
            "rendering in the world", FogRender.OFF, FogRender.values());
    Config<ItemRender> itemsConfig = new EnumConfig<>("Items",
            "Prevents dropped items from rendering", ItemRender.OFF,
            ItemRender.values());

    /**
     *
     */
    public NoRenderModule()
    {
        super("NoRender", "Prevents certain game elements from rendering",
                ModuleCategory.RENDER);
    }

    // insane code below

    /**
     *
     * @param event
     */
    @EventListener
    public void onTick(TickEvent event)
    {
        if (itemsConfig.getValue() == ItemRender.REMOVE && event.getStage() == EventStage.PRE)
        {
            for (Entity entity : Lists.newArrayList(mc.world.getEntities()))
            {
                if (entity instanceof ItemEntity)
                {
                    mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onHurtCam(HurtCamEvent event)
    {
        if (hurtCamConfig.getValue())
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderArmor(RenderArmorEvent event)
    {
        if (armorConfig.getValue() && event.getEntity() instanceof PlayerEntity)
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderOverlayFire(RenderOverlayEvent.Fire event)
    {
        if (fireOverlayConfig.getValue())
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderOverlayWater(RenderOverlayEvent.Water event)
    {
        if (waterOverlayConfig.getValue())
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderOverlayBlock(RenderOverlayEvent.Block event)
    {
        if (blockOverlayConfig.getValue())
        {
            event.cancel();
        }
    }
    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderOverlaySpyglass(RenderOverlayEvent.Spyglass event)
    {
        if (spyglassOverlayConfig.getValue())
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderOverlayPumpkin(RenderOverlayEvent.Pumpkin event)
    {
        if (pumpkinOverlayConfig.getValue())
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderOverlayBossBar(RenderOverlayEvent.BossBar event)
    {
        if (bossOverlayConfig.getValue())
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderOverlayFrostbite(RenderOverlayEvent.Frostbite event)
    {
        if (frostbiteConfig.getValue())
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderNausea(RenderNauseaEvent event)
    {
        if (nauseaConfig.getValue())
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderSkylight(RenderSkylightEvent event)
    {
        if (skylightConfig.getValue())
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderWitherSkull(RenderWitherSkullEvent event)
    {
        if (witherSkullsConfig.getValue())
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderEnchantingTableBook(RenderTileEntityEvent.EnchantingTableBook event)
    {
        if (tileEntitiesConfig.getValue())
        {
            event.cancel();
        }
    }

    /**
     *
     *
     * @param event
     */
    @EventListener
    public void onParticle(ParticleEvent event)
    {
        if (explosionsConfig.getValue() && (event.getParticleType() == ParticleTypes.EXPLOSION
                || event.getParticleType() == ParticleTypes.EXPLOSION_EMITTER)
                || fireworksConfig.getValue() && event.getParticleType() == ParticleTypes.FIREWORK
                || campfiresConfig.getValue() && event.getParticleType() == ParticleTypes.CAMPFIRE_COSY_SMOKE)
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderFireworkRocket(RenderFireworkRocketEvent event)
    {
        if (fireworksConfig.getValue())
        {
            event.cancel();
        }
    }

    /**
     *
     *
     * @param event
     */
    @EventListener
    public void onParticleEmitter(ParticleEvent.Emitter event)
    {
        if (totemConfig.getValue() && event.getParticleType() == ParticleTypes.TOTEM_OF_UNDYING)
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderFloatingItem(RenderFloatingItemEvent event)
    {
        if (totemConfig.getValue() && event.getFloatingItem() == Items.TOTEM_OF_UNDYING)
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderFog(RenderFogEvent event)
    {
        if (fogConfig.getValue() == FogRender.LIQUID_VISION
                && mc.player != null && mc.player.isSubmergedIn(FluidTags.LAVA))
        {
            event.cancel();
        }
        else if (fogConfig.getValue() == FogRender.CLEAR)
        {
            event.cancel();
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onRenderItem(RenderItemEvent event)
    {
        if (itemsConfig.getValue() == ItemRender.HIDE)
        {
            event.cancel();
        }
    }

    public enum FogRender
    {
        CLEAR,
        LIQUID_VISION,
        OFF
    }

    public enum ItemRender
    {
        REMOVE,
        HIDE,
        OFF
    }
}