package insane96mcp.insanelib.util.json.validator;

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

	public static FloatMinMaxValidator between(float min, float max) {
		return new FloatMinMaxValidator(min, max);
	}

	public static FloatMinMaxValidator atLeast(float min) {
		return new FloatMinMaxValidator(min, Float.MAX_VALUE);
	}

	public static FloatMinMaxValidator atMost(float max) {
		return new FloatMinMaxValidator(-Float.MAX_VALUE, max);
	}
}
