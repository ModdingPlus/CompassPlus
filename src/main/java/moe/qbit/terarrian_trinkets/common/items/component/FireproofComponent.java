package moe.qbit.terarrian_trinkets.common.items.component;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class FireproofComponent extends ComponentType.Dummy {
    public  static final FireproofComponent INSTANCE = new FireproofComponent();

    private FireproofComponent(){ }

    @Override
    public void tick(ItemStack stack, Void value, Entity entity) {
        entity.clearFire();
    }
}
