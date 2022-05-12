package moe.qbit.compass_plus;

import moe.qbit.compass_plus.client.ClientProxy;
import moe.qbit.compass_plus.common.CommonProxy;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CompassPlus.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = CompassPlus.MODID)
public class CompassPlus
{
    public static final String NAME = "Compass Plus";
    public static final String MODID = "compass_plus";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static CommonProxy proxy;

    public CompassPlus() {
        CompassPlus.proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        LOGGER.info("Registering data providers.");

        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if (event.includeServer()) {
            LOGGER.info("Registering server data providers.");

            LOGGER.info("Finished registering server data providers. Total Providers: {}", generator.getProviders().size());
        }

        if (event.includeClient()) {
            LOGGER.info("Registering client data providers.");


            LOGGER.info("Finished registering client data providers. Total Providers: {}", generator.getProviders().size());
        }

        LOGGER.info("Finished registering data providers. Total Providers: {}", generator.getProviders().size());
    }
}
