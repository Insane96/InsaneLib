package insane96mcp.insanelib.util.json.validator;

/**
 * {@link Validator} that checks whether a {@code float} value falls within an inclusive {@code [min, max]} range.
 *
 * @see #between(float, float)
 * @see #atLeast(float)
 * @see #atMost(float)
 */
public class FloatMinMaxValidator extends Validator<Float> {
	private final float min;
	private final float max;

	public FloatMinMaxValidator(float min, float max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean test(Float value) {
		return value >= min && value <= max;
	}

	@Override
	public String getErrorMessage(Float value) {
		return "Value %s must be between %s and %s".formatted(value, min, max);
	}

	/**
	 * Creates a validator that accepts values in {@code [min, max]}.
	 *
	 * @param min Inclusive lower bound.
	 * @param max Inclusive upper bound.
	 */
	public static FloatMinMaxValidator between(float min, float max) {
		return new FloatMinMaxValidator(min, max);
	}

	/**
	 * Creates a validator that accepts values {@code >= min}.
	 *
	 * @param min Inclusive lower bound.
	 */
	public static FloatMinMaxValidator atLeast(float min) {
		return new FloatMinMaxValidator(min, Float.MAX_VALUE);
	}

	/**
	 * Creates a validator that accepts values {@code <= max}.
	 *
	 * @param max Inclusive upper bound.
	 */
	public static FloatMinMaxValidator atMost(float max) {
		return new FloatMinMaxValidator(-Float.MAX_VALUE, max);
	}
}
