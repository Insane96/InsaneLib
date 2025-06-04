package insane96mcp.insanelib.module.base;

import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.data.ObjTag;
import insane96mcp.insanelib.util.ILLogger;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = "insanelib:base", canBeDisabled = false)
public class BaseFeature extends JsonFeature {
    public static final List<ObjTag<Block>> TEST_BLOCKS_DEFAULT = List.of(ObjTag.of("minecraft:stone", Registries.BLOCK), ObjTag.of("minecraft:dirt", Registries.BLOCK), ObjTag.of("#minecraft:cherry_logs", Registries.BLOCK));
    public static final List<ObjTag<Block>> testBlocks = new ArrayList<>();

    private static final Type BLOCK_LIST_TYPE = (new TypeToken<ArrayList<ObjTag<Block>>>() {}).getType();

    public BaseFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        addJsonConfig(
                new JsonConfig<>("test_blocks.json", testBlocks, TEST_BLOCKS_DEFAULT, BLOCK_LIST_TYPE)
                .withRegistryFor(Block.class)
        );
    }

    @Override
    public String getModConfigFolder() {
        return InsaneLib.CONFIG_FOLDER;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof Player player))
            return;
        if (player.level().dimension() == Level.OVERWORLD)
        for (ObjTag<Block> testBlock : testBlocks) {
            if (testBlock.matches(player.level().getBlockState(event.getEntity().blockPosition().below()).getBlock())) {
                ILLogger.info("Entity %s hurt when standing on test block", event.getEntity().getName().getString());
            }
        }
    }
}
