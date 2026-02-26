package insane96mcp.insanelib.command;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.JsonOps;
import insane96mcp.insanelib.module.base.items.ItemComponentsReloadListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;

public class ILCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("insanelib").requires(source -> source.hasPermission(2))
                .then(Commands.literal("set_time_played")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("time", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            int time = IntegerArgumentType.getInteger(ctx, "time");
                                            EntityArgument.getPlayers(ctx, "players").forEach(player ->
                                                    player.getStats().setValue(player, Stats.CUSTOM.get(Stats.PLAY_TIME), time));
                                            ctx.getSource().sendSuccess(() -> Component.literal("Set time played to " + time), false);
                                            return 1;
                                        }))))
                .then(Commands.literal("get_data_components")
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .executes(ctx -> {
                                    ResourceLocation id = ResourceLocationArgument.getId(ctx, "item");
                                    Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                                    if (item == null) {
                                        ctx.getSource().sendFailure(Component.literal("Unknown item: " + id));
                                        return 0;
                                    }

                                    RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, ctx.getSource().registryAccess());
                                    boolean isPatched = ItemComponentsReloadListener.PATCHED_COMPONENTS.containsKey(item);
                                    String header = id + (isPatched ? " [patched by ItemComponents]" : "");
                                    ctx.getSource().sendSuccess(() -> Component.literal("=== " + header + " ==="), false);

                                    // item.components() returns the patched map when available (via ItemMixin)
                                    DataComponentMap components = item.components();
                                    for (TypedDataComponent<?> component : components) {
                                        ResourceLocation typeId = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type());
                                        String value = encodeComponent(component, ops);
                                        ctx.getSource().sendSuccess(() -> Component.literal("  " + typeId + ": " + value), false);
                                    }
                                    return 1;
                                }))));
    }

    private static <T> String encodeComponent(TypedDataComponent<T> component, RegistryOps<JsonElement> ops) {
        var codec = component.type().codec();
        if (codec == null)
            return "<transient>";
        return codec.encodeStart(ops, component.value())
                .result()
                .map(JsonElement::toString)
                .orElse("<encode error>");
    }
}