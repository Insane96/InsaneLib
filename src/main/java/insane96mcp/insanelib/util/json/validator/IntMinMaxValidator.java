package insane96mcp.insanelib.util.json.validator;

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

	public static IntMinMaxValidator between(int min, int max) {
		return new IntMinMaxValidator(min, max);
	}

	public static IntMinMaxValidator atLeast(int min) {
		return new IntMinMaxValidator(min, Integer.MAX_VALUE);
	}

	public static IntMinMaxValidator atMost(int max) {
		return new IntMinMaxValidator(Integer.MIN_VALUE, max);
	}
}
