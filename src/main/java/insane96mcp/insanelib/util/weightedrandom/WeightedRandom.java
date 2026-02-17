package insane96mcp.insanelib.util.weightedrandom;

import net.minecraft.Util;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.List;

public class WeightedRandom {
	/**
	 * Returns the total weight of all items in a collection.
	 */
	public static int getTotalWeight(List<? extends IWeightedRandom> list) {
		int totalWeight = 0;

		for (IWeightedRandom weightedRandom : list) {
			totalWeight += weightedRandom.getWeight();
		}

		return totalWeight;
	}

	public static <T extends IWeightedRandom> T getRandomItem(RandomSource random, List<T> list) {
		return getRandomItem(random, list, getTotalWeight(list));
	}

	public static <T extends IWeightedRandom> T getRandomItem(RandomSource random, List<T> list, int totalWeight) {
		if (totalWeight <= 0) {
			throw Util.pauseInIde(new IllegalArgumentException());
		}
		else {
			int i = random.nextInt(totalWeight);
			return getWeightedItem(list, i);
		}
	}

	@Nullable
	public static <T extends IWeightedRandom> T getWeightedItem(List<T> list, int w) {
		for (T weightedRandom : list) {
			w -= weightedRandom.getWeight();
			if (w < 0)
				return weightedRandom;
		}

		return null;
	}
}