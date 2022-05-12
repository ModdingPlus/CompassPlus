package moe.qbit.compass_plus.client;

import moe.qbit.compass_plus.client.hud.CompassHUD;
import moe.qbit.compass_plus.common.CommonProxy;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerListeners(IEventBus modEventBus){
        super.registerListeners(modEventBus);

        modEventBus.addListener(this::clientSetup);
    }

    public void clientSetup(final FMLClientSetupEvent event) {
        OverlayRegistry.registerOverlayBottom("compass", new CompassHUD());
    }
}
