package insane96mcp.insanelib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;

public class ILCommand {
    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
        pDispatcher.register(Commands.literal("insanelib").requires((p_138819_)
                        -> p_138819_.hasPermission(2))
                .then(Commands.literal("set_time_played")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("time", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            int time = IntegerArgumentType.getInteger(ctx, "time");
                                            EntityArgument.getPlayers(ctx, "players").forEach(player ->
                                                    player.getStats().setValue(player, Stats.CUSTOM.get(Stats.PLAY_TIME), time));
                                            ctx.getSource().sendSuccess(() -> Component.literal("Set time played to " + time), false);
                                            return 1;
                                        })))));
    }
}
