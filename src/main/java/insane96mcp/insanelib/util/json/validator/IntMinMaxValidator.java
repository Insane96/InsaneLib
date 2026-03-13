package insane96mcp.insanelib.util.json.validator;

/**
 * {@link Validator} that checks whether an {@code int} value falls within an inclusive {@code [min, max]} range.
 *
 * @see #between(int, int)
 * @see #atLeast(int)
 * @see #atMost(int)
 */
public class IntMinMaxValidator extends Validator<Integer> {
	private final int min;
	private final int max;

	public IntMinMaxValidator(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean test(Integer i) {
		return i >= min && i <= max;
	}

	@Override
	public String getErrorMessage(Integer value) {
		return "Value %s must be between %s and %s".formatted(value, min, max);
	}

	/**
	 * Creates a validator that accepts values in {@code [min, max]}.
	 *
	 * @param min Inclusive lower bound.
	 * @param max Inclusive upper bound.
	 */
	public static IntMinMaxValidator between(int min, int max) {
		return new IntMinMaxValidator(min, max);
	}

	/**
	 * Creates a validator that accepts values {@code >= min}.
	 *
	 * @param min Inclusive lower bound.
	 */
	public static IntMinMaxValidator atLeast(int min) {
		return new IntMinMaxValidator(min, Integer.MAX_VALUE);
	}

	/**
	 * Creates a validator that accepts values {@code <= max}.
	 *
	 * @param max Inclusive upper bound.
	 */
	public static IntMinMaxValidator atMost(int max) {
		return new IntMinMaxValidator(Integer.MIN_VALUE, max);
	}
}
