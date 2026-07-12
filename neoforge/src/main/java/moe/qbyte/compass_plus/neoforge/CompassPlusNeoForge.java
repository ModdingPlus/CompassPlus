package moe.qbyte.compass_plus.neoforge;

import moe.qbyte.compass_plus.CompassPlus;
import moe.qbyte.compass_plus.client.hud.CompassHUD;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@Mod(value = CompassPlus.MODID, dist = Dist.CLIENT)
public class CompassPlusNeoForge {
    public CompassPlusNeoForge(IEventBus modEventBus) {
        CompassHUD hud = new CompassHUD();
        modEventBus.addListener((RegisterGuiLayersEvent event) -> event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(CompassPlus.MODID, "compass_hud"), hud::render));
    }
}
