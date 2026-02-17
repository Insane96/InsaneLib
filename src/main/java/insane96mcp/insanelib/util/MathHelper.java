package insane96mcp.insanelib.util;

import net.minecraft.util.RandomSource;

public class MathHelper {
	/**
	 * Rounds a double value to the specified number of decimal places.
	 * @param value the value to round
	 * @param places the number of decimal places
	 */
	public static double round(double value, int places) {
		double scale = Math.pow(10, places);
		return Math.round(value * scale) / scale;
	}

	/**
	 * Rounds a float value to the specified number of decimal places.
	 * @param value the value to round
	 * @param places the number of decimal places
	 */
	public static float round(float value, int places) {
		double scale = Math.pow(10, places);
		return (float) (Math.round(value * scale) / scale);
	}

	/**
	 * @see #getAmountWithDecimalChance(RandomSource, double)
	 */
	public static int getAmountWithDecimalChance(RandomSource rand, float f) {
		return getAmountWithDecimalChance(rand, (double) f);
	}

	/**
	 * Returns the integer part of a value, with a random chance to add 1 based on the decimal part.
	 * E.g. 1.2 has a 20% chance to return 2, 80% chance to return 1. If the value has no decimal part, returns it as-is.
	 * @param rand the random source
	 * @param f the value to process
	 */
	public static int getAmountWithDecimalChance(RandomSource rand, double f) {
		double mod = f - (int)f;
		if (mod == 0f)
			return (int) f;
		f -= mod;
		if (rand.nextDouble() < mod)
			f++;
		return (int) f;
	}
}
