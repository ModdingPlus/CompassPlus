package moe.qbit.terarrian_trinkets.common.items.component;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Component<T> {
    private final ComponentType<T> type;
    private final T value;

    private Component(ComponentType<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, @Nullable LivingEntity entity) {
        return this.type.getAttributeModifiers(stack, value, entity);
    }

    public void tick(ItemStack stack, Entity entity) {
        this.type.tick(stack, this.value, entity);
    }

    @Nonnull
    public static <T> Component<T> create(@Nonnull ComponentType<T> type, @Nonnull T config) {
        return  new Component<T>(type, config);
    }

    @Nonnull
    public static Component<Void> create(ComponentType<Void> type) {
        return new Component<Void>(type, null);
    }

    public ComponentType<T> getType() {
        return this.type;
    }
    public T getValue() {
        return this.value;
    }
}
