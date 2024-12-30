package insane96mcp.insanelib.util.json.validator;

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

	public static DoubleMinMaxValidator between(double min, double max) {
		return new DoubleMinMaxValidator(min, max);
	}

	public static DoubleMinMaxValidator atLeast(double min) {
		return new DoubleMinMaxValidator(min, Double.MAX_VALUE);
	}

	public static DoubleMinMaxValidator atMost(double max) {
		return new DoubleMinMaxValidator(-Double.MAX_VALUE, max);
	}
}
