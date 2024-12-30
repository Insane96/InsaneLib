package insane96mcp.insanelib.util.json.validator;

import java.util.function.Predicate;

public abstract class Validator<T> implements Predicate<T> {

	@Override
	public boolean test(T t) {
		return true;
	}

	public abstract String getErrorMessage(T value);

}
