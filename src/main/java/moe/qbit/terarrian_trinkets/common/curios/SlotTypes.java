package moe.qbit.terarrian_trinkets.common.curios;

import moe.qbit.terarrian_trinkets.TerrarianTrinkets;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

public class SlotTypes {
    public static final String TRINKET = "curio";

    @SubscribeEvent
    public static void IMCEnqueue(InterModEnqueueEvent event){
        TerrarianTrinkets.LOGGER.info("Registering Curio Trinket Slots");
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.CURIO.getMessageBuilder().size(4).build());
    }
}
