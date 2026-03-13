package insane96mcp.insanelib.util.json.validator;

import java.util.function.Predicate;

/**
 * Abstract validator used with {@link insane96mcp.insanelib.util.json.ILGsonHelper} to validate
 * parsed JSON values and produce human-readable error messages on failure.
 *
 * @param <T> The type of value being validated.
 */
public abstract class Validator<T> implements Predicate<T> {

	@Override
	public boolean test(T t) {
		return true;
	}

	/**
	 * Returns a human-readable error message describing why {@code value} failed validation.
	 *
	 * @param value The value that failed validation.
	 * @return A descriptive error message.
	 */
	public abstract String getErrorMessage(T value);

}
