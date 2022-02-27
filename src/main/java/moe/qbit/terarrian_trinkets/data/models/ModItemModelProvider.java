package moe.qbit.terarrian_trinkets.data.models;

import moe.qbit.terarrian_trinkets.common.items.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //noinspection ConstantConditions
        ModItems.getItems().forEach(
                item -> this.withExistingParent(item.getRegistryName().getPath(), mcLoc("item/generated"))
                                .texture("layer0", item.getRegistryName())
        );
    }
}
