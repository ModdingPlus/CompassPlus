package moe.qbit.terarrian_trinkets.data.tags;

import moe.qbit.terarrian_trinkets.TerrarianTrinkets;
import moe.qbit.terarrian_trinkets.common.curios.SlotTypes;
import moe.qbit.terarrian_trinkets.common.items.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Collection;

public class ModItemTags extends ItemTagsProvider {
    Tag.Named<Item> TRINKET = ItemTags.bind(new ResourceLocation(CuriosApi.MODID, SlotTypes.TRINKET).toString());

    public ModItemTags(DataGenerator dataGenerator, BlockTagsProvider blockTagsProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, blockTagsProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        addAll(this.tag(TRINKET), ModItems.getItems());
    }

    protected TagAppender<Item> addAll(TagsProvider.TagAppender<Item> tagAppender, Collection<Item> items) {
        items.forEach(tagAppender::add);
        TerrarianTrinkets.LOGGER.info("Added {} items to tag.", items.size());
        return tagAppender;
    }

    @Override
    public String getName() {
        return String.format("%s Item Tags", TerrarianTrinkets.NAME);
    }
}
