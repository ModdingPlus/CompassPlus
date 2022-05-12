package moe.qbit.compass_plus.common;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CommonProxy {
    protected ModLoadingContext modLoadingContext;
    protected IEventBus modEventBus;

    public CommonProxy(){
        this.modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        this.modLoadingContext = ModLoadingContext.get();

        registerListeners(modEventBus);
    }
    
    public void registerListeners(IEventBus modEventBus){
        modEventBus.addListener(this::setup);
    }

    public void setup(final FMLCommonSetupEvent event) {}
}
