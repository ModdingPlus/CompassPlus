package moe.qbit.compass_plus.client;

import moe.qbit.compass_plus.CompassPlus;
import moe.qbit.compass_plus.client.hud.CompassHUD;
import moe.qbit.compass_plus.common.CommonProxy;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerListeners(IEventBus modEventBus){
        super.registerListeners(modEventBus);

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerOverlay);
    }

    public void clientSetup(final FMLClientSetupEvent event) {
    }

    public void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(CompassPlus.MODID, new CompassHUD());
    }
}
