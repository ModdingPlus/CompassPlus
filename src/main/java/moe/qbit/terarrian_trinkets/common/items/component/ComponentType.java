package moe.qbit.terarrian_trinkets.common.items.component;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface ComponentType<T> {

    default Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, T value, @Nullable LivingEntity entity) {
        return ImmutableMultimap.of();
    }

    default void tick(ItemStack stack, T value, Entity entity) {}

    class Dummy implements ComponentType<Void> { }
    class Float implements ComponentType<java.lang.Float> { }
    class Double implements ComponentType<java.lang.Double> { }
    class Integer implements ComponentType<java.lang.Integer> { }

}
