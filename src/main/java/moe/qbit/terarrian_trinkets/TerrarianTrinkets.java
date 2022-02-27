package moe.qbit.terarrian_trinkets;

import moe.qbit.terarrian_trinkets.client.ClientProxy;
import moe.qbit.terarrian_trinkets.common.CommonProxy;
import moe.qbit.terarrian_trinkets.common.items.ModItems;
import moe.qbit.terarrian_trinkets.data.models.ModItemModelProvider;
import moe.qbit.terarrian_trinkets.data.tags.ModBlockTags;
import moe.qbit.terarrian_trinkets.data.tags.ModItemTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TerrarianTrinkets.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = TerrarianTrinkets.MODID)
public class TerrarianTrinkets
{
    public static final String NAME = "Terrarian Trinkets";
    public static final String MODID = "terrarian_trinkets";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final CreativeModeTab TAB = new CreativeModeTab(MODID) {
        @Override
        public ItemStack makeIcon() {
            return ModItems.SLIMY_BALLOON.get().getDefaultInstance();
        }
    };

    public static CommonProxy proxy;

    public TerrarianTrinkets() {
        TerrarianTrinkets.proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        LOGGER.info("Registering data providers.");

        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if (event.includeServer()) {
            LOGGER.info("Registering server data providers.");

            ModBlockTags blockTags = new ModBlockTags(generator, MODID, existingFileHelper);
            generator.addProvider(blockTags);
            generator.addProvider(new ModItemTags(generator, blockTags, MODID, existingFileHelper));

            LOGGER.info("Finished registering server data providers. Total Providers: {}", generator.getProviders().size());
        }

        if (event.includeClient()) {
            LOGGER.info("Registering client data providers.");

            generator.addProvider(new ModItemModelProvider(generator, MODID, existingFileHelper));

            LOGGER.info("Finished registering client data providers. Total Providers: {}", generator.getProviders().size());
        }

        LOGGER.info("Finished registering data providers. Total Providers: {}", generator.getProviders().size());
    }
}
