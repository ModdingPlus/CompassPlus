package moe.qbit.terarrian_trinkets.common.items;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import moe.qbit.terarrian_trinkets.common.items.component.Component;
import moe.qbit.terarrian_trinkets.common.items.component.ComponentType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.EnumSet;
import java.util.UUID;

public class ComposableItem extends Item implements ICurioItem {
    private final NonNullList<Component<?>> components = NonNullList.create();
    private EnumSet<Domain> domain = EnumSet.noneOf(Domain.class);

    public ComposableItem(Properties properties) {
        super(properties);
    }

    public ComposableItem component(@Nonnull Component<?> component) {
        this.components.add(component);
        return this;
    }
    public <T> ComposableItem component(@Nonnull ComponentType<T> componentType, @Nonnull T value) {
        return this.component(Component.create(componentType, value));
    }
    public ComposableItem component(@Nonnull ComponentType<Void> componentType) {
        return this.component(Component.create(componentType));
    }

    public boolean hasComponent(@Nonnull ItemStack stack, @Nonnull ComponentType<?> componentType) {
        return this.components.stream().anyMatch(component -> component.getType() == componentType);
    }

    @Nullable
    public <T> Component<T> getComponent(@Nonnull ItemStack stack, @Nonnull ComponentType<T> componentType) {
        //noinspection unchecked
        return (Component<T>) this.components.stream().filter(component -> component.getType() == componentType).findAny().orElse(null);
    }

    public NonNullList<Component<?>> getComponents() { return this.components; }
    public <T> NonNullList<Component<T>> getComponents(ComponentType<T> componentType) {
        return this.components.stream().filter(component -> component.getType() == componentType).collect(NonNullList::create, (l,r)->l.add((Component<T>) r), AbstractList::addAll);
    }

    public ComposableItem domain(@Nonnull Domain domain) {
        this.domain.add(domain);
        return this;
    }
    public EnumSet<Domain> getDomain() {
        return domain;
    }

    enum Domain {
        INVENTORY,
        ARMOR,
        CURIO
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int p_41407_, boolean p_41408_) {
        super.inventoryTick(stack, level, entity, p_41407_, p_41408_);
        if (getDomain().contains(Domain.INVENTORY)) this.components.forEach(component -> component.tick(stack, entity));
    }
    @Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {
        super.onArmorTick(stack, world, player);
        if (getDomain().contains(Domain.ARMOR)) this.components.forEach(component -> component.tick(stack, player));
    }
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        ICurioItem.super.curioTick(slotContext, stack);
        if (getDomain().contains(Domain.CURIO)) this.components.forEach(component -> component.tick(stack, slotContext.entity()));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (!getDomain().contains(Domain.ARMOR)) return super.getAttributeModifiers(slot, stack);
        return combinedAttributeModifiers(stack, null);
    }
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
         if (!getDomain().contains(Domain.CURIO)) return ICurioItem.super.getAttributeModifiers(slotContext, uuid, stack);
        return combinedAttributeModifiers(stack, slotContext.entity());
    }

    protected Multimap<Attribute, AttributeModifier> combinedAttributeModifiers(ItemStack stack, LivingEntity entity) {
        return this.components.stream().map(component -> component.getAttributeModifiers(stack, entity)).collect(ArrayListMultimap::create, Multimap::putAll, Multimap::putAll);
    }

    public static NonNullList<Component<?>>  getActiveComponents(Player player) {
        return Streams.concat(
                player.getInventory().items.stream()
                        .map(ItemStack::getItem)
                        .filter(item -> item instanceof ComposableItem)
                        .map(item -> (ComposableItem) item)
                        .filter(item ->  item.getDomain().contains(Domain.INVENTORY)),
                player.getInventory().offhand.stream()
                        .map(ItemStack::getItem)
                        .filter(item -> item instanceof ComposableItem)
                        .map(item -> (ComposableItem) item)
                        .filter(item ->  item.getDomain().contains(Domain.INVENTORY)),
                player.getInventory().armor.stream()
                        .map(ItemStack::getItem)
                        .filter(item -> item instanceof ComposableItem)
                        .map(item -> (ComposableItem) item)
                        .filter(item ->  item.getDomain().contains(Domain.ARMOR)),
                CuriosApi.getCuriosHelper().findCurios(player, itemStack -> true).stream()
                        .map(SlotResult::stack)
                        .map(ItemStack::getItem)
                        .filter(item -> item instanceof ComposableItem)
                        .map(item -> (ComposableItem) item)
                        .filter(item ->  item.getDomain().contains(Domain.CURIO))
        )
                .map(ComposableItem::getComponents)
                .flatMap(AbstractList::stream)
                .collect(NonNullList::create, AbstractList::add, AbstractList::addAll);
    }
}
