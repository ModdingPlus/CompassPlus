package moe.qbyte.compass_plus.neoforge;

import moe.qbyte.compass_plus.CompassPlus;
import moe.qbyte.compass_plus.client.hud.CompassHUD;
import moe.qbyte.compass_plus.config.ClientConfig;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CompassPlus.MODID, dist = Dist.CLIENT)
public class CompassPlusNeoForge {
    public CompassPlusNeoForge(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);
        modEventBus.addListener((ModConfigEvent.Loading event) -> {
            if (event.getConfig().getSpec() == ClientConfig.CLIENT_SPEC) {
                ClientConfig.cache();
            }
        });
        modEventBus.addListener((ModConfigEvent.Reloading event) -> {
            if (event.getConfig().getSpec() == ClientConfig.CLIENT_SPEC) {
                ClientConfig.cache();
            }
        });

        // NeoForge's built-in config screen, reachable from the mod list
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener((RegisterGuiLayersEvent event) -> event.registerAboveAll(
                Identifier.fromNamespaceAndPath(CompassPlus.MODID, "compass_hud"),
                CompassHUD.INSTANCE::render));
    }
}
