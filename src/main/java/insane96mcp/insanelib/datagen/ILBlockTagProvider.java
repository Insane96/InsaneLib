package insane96mcp.insanelib.datagen;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.module.base.betterfallingblocks.BetterFallingBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ILBlockTagProvider extends BlockTagsProvider {

    public ILBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, InsaneLib.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BetterFallingBlocks.BLACKLISTED_BLOCKS).add(Blocks.DRAGON_EGG, Blocks.POINTED_DRIPSTONE, Blocks.SCAFFOLDING);
    }

    @Override
    public String getName() {
        return "InsaneLib Block Tags";
    }
}
