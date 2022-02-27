package moe.qbit.terarrian_trinkets.common.items.component;

import moe.qbit.terarrian_trinkets.TerrarianTrinkets;
import moe.qbit.terarrian_trinkets.common.items.ComposableItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = TerrarianTrinkets.MODID)
public class FallDamageComponent extends ComponentType.Float {
    public static final FallDamageComponent INSTANCE = new FallDamageComponent();

    private FallDamageComponent() { }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        LivingEntity livingEntity = event.getEntityLiving();
        if (livingEntity instanceof Player) {
            for (Component<?> component: ComposableItem.getActiveComponents((Player) livingEntity)) {
                if (component.getType() == INSTANCE) {
                    event.setDamageMultiplier(event.getDamageMultiplier() * (float)component.getValue());
                }
            }
        }
    }
}
