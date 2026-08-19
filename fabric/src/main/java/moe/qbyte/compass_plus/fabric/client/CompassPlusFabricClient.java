package moe.qbyte.compass_plus.fabric.client;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import moe.qbyte.compass_plus.CompassPlus;
import moe.qbyte.compass_plus.client.hud.CompassHUD;
import moe.qbyte.compass_plus.config.ClientConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class CompassPlusFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigRegistry.INSTANCE.register(CompassPlus.MODID, ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);
        ModConfigEvents.loading(CompassPlus.MODID).register(config -> {
            if (config.getSpec() == ClientConfig.CLIENT_SPEC) {
                ClientConfig.cache();
            }
        });
        ModConfigEvents.reloading(CompassPlus.MODID).register(config -> {
            if (config.getSpec() == ClientConfig.CLIENT_SPEC) {
                ClientConfig.cache();
            }
        });

        // FCAP's port of NeoForge's config screen, exposed through Mod Menu
        ConfigScreenFactoryRegistry.INSTANCE.register(CompassPlus.MODID, ConfigurationScreen::new);

        HudRenderCallback.EVENT.register(CompassHUD.INSTANCE::render);
    }
}
