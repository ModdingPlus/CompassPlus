package moe.qbit.terarrian_trinkets.common;

import moe.qbit.terarrian_trinkets.common.config.ServerConfig;
import moe.qbit.terarrian_trinkets.common.curios.SlotTypes;
import moe.qbit.terarrian_trinkets.common.items.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.SlotTypePreset;

public class CommonProxy {
    protected ModLoadingContext modLoadingContext;
    protected IEventBus modEventBus;

    public CommonProxy(){
        this.modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        this.modLoadingContext = ModLoadingContext.get();

        this.modLoadingContext.registerConfig(ModConfig.Type.SERVER, ServerConfig.SERVER_SPEC);

        registerListeners(modEventBus);
    }
    
    public void registerListeners(IEventBus modEventBus){
        modEventBus.addListener(this::setup);
        modEventBus.register(SlotTypes.class);
        ModItems.register(modEventBus);
    }

    public void setup(final FMLCommonSetupEvent event) {}
}
