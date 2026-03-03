package insane96mcp.insanelib.event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;

public class ILEventFactory {

    public static boolean doPlayerSprintCheck(LocalPlayer player)
    {
        PlayerSprintEvent event = new PlayerSprintEvent(player);
        NeoForge.EVENT_BUS.post(event);
        return event.canSprint();
    }

    public static void onBlockBurnt(Level level, BlockPos pos, BlockState state)
    {
        BlockBurntEvent event = new BlockBurntEvent(level, pos, state);
        NeoForge.EVENT_BUS.post(event);
    }

    public static float onPlayerExhaustionEvent(Player player, float amount)
    {
        PlayerExhaustionEvent event = new PlayerExhaustionEvent(player, amount);
        NeoForge.EVENT_BUS.post(event);
        return event.getAmount();
    }

    public static void onCakeEatEvent(Player player, BlockPos pos, LevelAccessor level)
    {
        CakeEatEvent event = new CakeEatEvent(player, pos, level);
        NeoForge.EVENT_BUS.post(event);
    }

    public static void onFallingBlockLand(FallingBlockEntity fallingBlock)
    {
        FallingBlockLandEvent event = new FallingBlockLandEvent(fallingBlock);
        NeoForge.EVENT_BUS.post(event);
    }

    public static int getHurtAmount(ItemStack stack, int amount, RandomSource random, LivingEntity livingEntity)
    {
        HurtItemStackEvent event = new HurtItemStackEvent(stack, amount, random, livingEntity);
        NeoForge.EVENT_BUS.post(event);
        return event.getAmount();
    }

    public static boolean onAddEatEffect(LivingEntity livingEntity, FoodProperties foodProperties)
    {
        AddEatEffectEvent event = new AddEatEffectEvent(livingEntity, foodProperties);
        NeoForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }

    public static float onUseItemMovementSpeedModifier(LocalPlayer player, ItemStack useItem) {
        PlayerUseItemMovSpeedEvent event = new PlayerUseItemMovSpeedEvent(player, useItem);
        NeoForge.EVENT_BUS.post(event);
        return event.getSpeedModifier();
    }
}
