package moe.qbit.terarrian_trinkets.common.items.component.attributes;

import com.google.common.collect.Multimap;
import moe.qbit.terarrian_trinkets.common.items.component.ComponentType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AttributeModifierComponent implements ComponentType<AttributeModifierProvider> {
    public static final AttributeModifierComponent INSTANCE = new AttributeModifierComponent();

    private AttributeModifierComponent() { }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, AttributeModifierProvider value, @Nullable LivingEntity entity) {
        return value.asMultimap();
    }
}
