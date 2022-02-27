package moe.qbit.terarrian_trinkets.common.items.component.attributes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Map;
import java.util.function.Supplier;

public class AttributeModifierProvider {
    private final Multimap<Supplier<Attribute>, AttributeModifier> attributesMap = ArrayListMultimap.create();

    private AttributeModifierProvider() {}

    public static AttributeModifierProvider create() {
        return new AttributeModifierProvider();
    }

    public static AttributeModifierProvider of(Supplier<Attribute> attribute, AttributeModifier attributeModifier){
        return new AttributeModifierProvider().add(attribute, attributeModifier);
    }

    public static AttributeModifierProvider of(Supplier<Attribute> attribute, String identifier, double value, AttributeModifier.Operation operation){
        return new AttributeModifierProvider().add(attribute, identifier, value, operation);
    }

    public AttributeModifierProvider add(Supplier<Attribute> attribute, AttributeModifier attributeModifier){
        this.attributesMap.put(attribute,attributeModifier);
        return this;
    }

    public AttributeModifierProvider add(Supplier<Attribute> attribute, String identifier, double value, AttributeModifier.Operation operation){
        this.attributesMap.put(attribute, new AttributeModifier(identifier, value, operation));
        return this;
    }

    public ImmutableMultimap<Attribute, AttributeModifier> asMultimap() {
        //noinspection UnstableApiUsage
        return ImmutableMultimap.copyOf(
                        this.attributesMap.entries()
                                .stream()
                                .map(entry -> Map.entry(entry.getKey().get(), entry.getValue()))
                                .toList()
                );
    }
}
