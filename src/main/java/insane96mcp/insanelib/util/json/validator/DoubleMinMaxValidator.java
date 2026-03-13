package insane96mcp.insanelib.util.json.validator;

/**
 * {@link Validator} that checks whether a {@code double} value falls within an inclusive {@code [min, max]} range.
 *
 * @see #between(double, double)
 * @see #atLeast(double)
 * @see #atMost(double)
 */
public class DoubleMinMaxValidator extends Validator<Double> {
	private final double min;
	private final double max;

	public DoubleMinMaxValidator(double min, double max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean test(Double value) {
		return value >= min && value <= max;
	}

	@Override
	public String getErrorMessage(Double value) {
		return "Value %s must be between %s and %s".formatted(value, min, max);
	}

	/**
	 * Creates a validator that accepts values in {@code [min, max]}.
	 *
	 * @param min Inclusive lower bound.
	 * @param max Inclusive upper bound.
	 */
	public static DoubleMinMaxValidator between(double min, double max) {
		return new DoubleMinMaxValidator(min, max);
	}

	/**
	 * Creates a validator that accepts values {@code >= min}.
	 *
	 * @param min Inclusive lower bound.
	 */
	public static DoubleMinMaxValidator atLeast(double min) {
		return new DoubleMinMaxValidator(min, Double.MAX_VALUE);
	}

	/**
	 * Creates a validator that accepts values {@code <= max}.
	 *
	 * @param max Inclusive upper bound.
	 */
	public static DoubleMinMaxValidator atMost(double max) {
		return new DoubleMinMaxValidator(-Double.MAX_VALUE, max);
	}
}
