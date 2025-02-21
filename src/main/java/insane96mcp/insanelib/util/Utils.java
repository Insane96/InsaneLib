package insane96mcp.insanelib.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class Utils {
	public static <T extends Enum<?>> T searchEnum(Class<T> enumeration, String search) {
		for (T each : enumeration.getEnumConstants()) {
			if (each.name().compareToIgnoreCase(search) == 0) {
				return each;
			}
		}
		return null;
	}

	public static String formatDecimal(double decimal, String format) {
		DecimalFormat df = new DecimalFormat(format);
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(dfs);
		return df.format(decimal);
	}

	public static String toSpacedSentence(String camelCase) {
		String[] words = camelCase.split("(?<!^)(?=[A-Z])");
		String sentence = String.join(" ", words).toLowerCase();
		return sentence.substring(0, 1).toUpperCase() + sentence.substring(1);
	}

}
