package moe.qbyte.compass_plus.fabric.client;

import moe.qbyte.compass_plus.client.hud.CompassHUD;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class CompassPlusFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CompassHUD hud = new CompassHUD();
        HudRenderCallback.EVENT.register(hud::render);
    }
}
