package moe.qbit.terarrian_trinkets.common.items;

import moe.qbit.movement_plus.api.common.attributes.Attributes;
import moe.qbit.terarrian_trinkets.TerrarianTrinkets;
import moe.qbit.terarrian_trinkets.common.items.component.ModComponentsTypes;
import moe.qbit.terarrian_trinkets.common.items.component.attributes.AttributeModifierProvider;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

public class ModItems {
    private static  final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TerrarianTrinkets.MODID);
    public static final RegistryObject<ComposableItem> CLOUD_IN_A_BOTTLE = ITEMS.register("cloud_in_a_bottle", () -> attributeCurio(Attributes.MULTI_JUMPS, "cloud", 1, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<ComposableItem> SLIMY_BALLOON = ITEMS.register("slimy_balloon", () -> attributeCurio(Attributes.JUMP_HEIGHT, "balloon", 0.5, AttributeModifier.Operation.MULTIPLY_BASE));
    public static final RegistryObject<ComposableItem> CLOUD_IN_A_BALLOON = ITEMS.register("cloud_in_a_balloon", () -> attributeCurio(AttributeModifierProvider.of(Attributes.MULTI_JUMPS, "cloud", 1, AttributeModifier.Operation.ADDITION).add(Attributes.JUMP_HEIGHT, "balloon", 0.5, AttributeModifier.Operation.MULTIPLY_BASE)));
    public static final RegistryObject<ComposableItem> CLIMBING_CLAWS = ITEMS.register("climbing_claws", () -> curio().component(ModComponentsTypes.CLIMBING, 0.2d));
    // shoe spikes
    // climbing gear
    // tabi
    // black belt
    // master ninja gear
    public static final RegistryObject<ComposableItem> LUCKY_HORSESHOE = ITEMS.register("lucky_horseshoe", () -> curio().component(ModComponentsTypes.FALL_DAMAGE, 0f));
    // flipper
    public static final RegistryObject<ComposableItem> OBSIDIAN_SKULL = ITEMS.register("obsidian_skull", () -> curio().component(ModComponentsTypes.FIREPROOF));
    // lava charm
    // molten charm
    // cobalt shield
    // obsidian shield
    public static final RegistryObject<ComposableItem> AGLET = ITEMS.register("aglet", () -> attributeCurio(() -> net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,"aglet", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
    public static final RegistryObject<ComposableItem> ANKLET_OF_WIND = ITEMS.register("anklet_of_wind", () -> attributeCurio(() -> net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,"anklet_of_wind", 0.30, AttributeModifier.Operation.MULTIPLY_BASE));
    // hermes boots
    // lightning boots
    // pufferfish balloon
    // flower boots

    protected static ComposableItem curio(){
        return new ComposableItem(new Item.Properties().stacksTo(1).tab(TerrarianTrinkets.TAB)).domain(ComposableItem.Domain.CURIO);
    }

    protected static ComposableItem attributeCurio(AttributeModifierProvider attributeModifierProvider){
        return curio().component(ModComponentsTypes.ATTRIBUTE_MODIFIER, attributeModifierProvider);
    }
    protected static ComposableItem attributeCurio(Supplier<Attribute> attributeSupplier, String identifier, double value, AttributeModifier.Operation operation){
        return attributeCurio(AttributeModifierProvider.of(attributeSupplier, identifier, value, operation));
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
    public static List<Item> getItems() { return ITEMS.getEntries().stream().map(RegistryObject::get).toList(); }
}
