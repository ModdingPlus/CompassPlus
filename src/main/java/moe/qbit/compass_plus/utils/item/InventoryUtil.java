package moe.qbit.compass_plus.utils.item;

import com.google.common.collect.Streams;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.AbstractList;
import java.util.List;

public class InventoryUtil {

    public static List<ItemStack> all(Inventory inventory) {
        return Streams.concat(
                        inventory.items.stream(),
                        inventory.offhand.stream(),
                        inventory.armor.stream()
                )
                .collect(NonNullList::create, AbstractList::add, AbstractList::addAll);
    }
}
