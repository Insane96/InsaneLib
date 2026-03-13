package insane96mcp.insanelib.util.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import insane96mcp.insanelib.util.json.validator.Validator;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Utility methods that extend {@link GsonHelper} with nullable getters and null-safe writers.
 */
public class ILGsonHelper {

	/**
	 * Returns the member as an {@code int}, or {@code null} if the member is absent.
	 * Optionally validates the value with the given {@link Validator}.
	 *
	 * @param jObject    The JSON object to read from.
	 * @param memberName The name of the member.
	 * @param validator  Optional validator; if non-null and the value fails, a {@link JsonParseException} is thrown.
	 * @return The parsed value, or {@code null} if the member is absent.
	 * @throws JsonParseException If the member is present but fails validation.
	 */
	@Nullable
	public static Integer getAsNullableInt(JsonObject jObject, String memberName, @Nullable Validator<Integer> validator) {
		if (!jObject.has(memberName))
			return null;
		int i = GsonHelper.getAsInt(jObject, memberName);
		if (validator != null && !validator.test(i))
			throw new JsonParseException(validator.getErrorMessage(i));
		return i;
	}

	/**
	 * Returns the member as a {@code double}, or {@code null} if the member is absent.
	 * Optionally validates the value with the given {@link Validator}.
	 *
	 * @param jObject    The JSON object to read from.
	 * @param memberName The name of the member.
	 * @param validator  Optional validator; if non-null and the value fails, a {@link JsonParseException} is thrown.
	 * @return The parsed value, or {@code null} if the member is absent.
	 * @throws JsonParseException If the member is present but fails validation.
	 */
	@Nullable
	public static Double getAsNullableDouble(JsonObject jObject, String memberName, @Nullable Validator<Double> validator) {
		if (!jObject.has(memberName))
			return null;
		double d = GsonHelper.getAsDouble(jObject, memberName);
		if (validator != null && !validator.test(d))
			throw new JsonParseException(validator.getErrorMessage(d));
		return d;
	}

	/**
	 * Returns the member as a {@code float}, or {@code null} if the member is absent.
	 * Optionally validates the value with the given {@link Validator}.
	 *
	 * @param jObject    The JSON object to read from.
	 * @param memberName The name of the member.
	 * @param validator  Optional validator; if non-null and the value fails, a {@link JsonParseException} is thrown.
	 * @return The parsed value, or {@code null} if the member is absent.
	 * @throws JsonParseException If the member is present but fails validation.
	 */
	@Nullable
	public static Float getAsNullableFloat(JsonObject jObject, String memberName, @Nullable Validator<Float> validator) {
		if (!jObject.has(memberName))
			return null;
		float f = GsonHelper.getAsFloat(jObject, memberName);
		if (validator != null && !validator.test(f))
			throw new JsonParseException(validator.getErrorMessage(f));
		return f;
	}

	/**
	 * Returns the member as an {@code int}, or {@code null} if the member is absent.
	 *
	 * @param jObject    The JSON object to read from.
	 * @param memberName The name of the member.
	 * @return The parsed value, or {@code null} if the member is absent.
	 */
	@Nullable
	public static Integer getAsNullableInt(JsonObject jObject, String memberName) {
		return getAsNullableInt(jObject, memberName, null);
	}

	/**
	 * Returns the member as a {@code double}, or {@code null} if the member is absent.
	 *
	 * @param jObject    The JSON object to read from.
	 * @param memberName The name of the member.
	 * @return The parsed value, or {@code null} if the member is absent.
	 */
	@Nullable
	public static Double getAsNullableDouble(JsonObject jObject, String memberName) {
		return getAsNullableDouble(jObject, memberName, null);
	}

	/**
	 * Returns the member as a {@code float}, or {@code null} if the member is absent.
	 *
	 * @param jObject    The JSON object to read from.
	 * @param memberName The name of the member.
	 * @return The parsed value, or {@code null} if the member is absent.
	 */
	@Nullable
	public static Float getAsNullableFloat(JsonObject jObject, String memberName) {
		return getAsNullableFloat(jObject, memberName, null);
	}

	/**
	 * Returns the member as a {@code boolean}, or {@code null} if the member is absent.
	 *
	 * @param jObject    The JSON object to read from.
	 * @param memberName The name of the member.
	 * @return The parsed value, or {@code null} if the member is absent.
	 */
	@Nullable
	public static Boolean getAsNullableBoolean(JsonObject jObject, String memberName) {
		if (!jObject.has(memberName))
			return null;
		return GsonHelper.getAsBoolean(jObject, memberName);
	}

	/**
	 * Writes a primitive property to the JSON object. Does nothing if {@code value} is {@code null}.
	 * Supports {@link Number}, {@link Character}, {@link String}, and {@link Boolean} values.
	 *
	 * @param jObject    The JSON object to write to.
	 * @param memberName The name of the property.
	 * @param value      The value to write, or {@code null} to skip.
	 * @throws ClassCastException If {@code value} is a non-null type not supported by {@link JsonObject#addProperty}.
	 */
	public static void addProperty(JsonObject jObject, String memberName, @Nullable Object value) {
        switch (value) {
            case null -> {
                return;
            }
            case Number number -> jObject.addProperty(memberName, number);
            case Character character -> jObject.addProperty(memberName, character);
            case String string -> jObject.addProperty(memberName, string);
            case Boolean bool -> jObject.addProperty(memberName, bool);
            default ->
                    throw new ClassCastException("Cannot add value of type %s to %s".formatted(value.getClass(), jObject));
        }
    }

	/**
	 * Serializes {@code value} and adds it to the JSON object. Does nothing if {@code value} is {@code null}.
	 *
	 * @param jObject    The JSON object to write to.
	 * @param context    The serialization context.
	 * @param memberName The name of the property.
	 * @param value      The value to serialize and write, or {@code null} to skip.
	 */
	public static void add(JsonObject jObject, JsonSerializationContext context, String memberName, @Nullable Object value) {
		if (value == null)
			return;
		jObject.add(memberName, context.serialize(value));
	}

	/**
	 * Serializes {@code value} as the given {@code type} and adds it to the JSON object. Does nothing if {@code value} is {@code null}.
	 *
	 * @param jObject    The JSON object to write to.
	 * @param context    The serialization context.
	 * @param memberName The name of the property.
	 * @param value      The value to serialize and write, or {@code null} to skip.
	 * @param type       The type to use for serialization.
	 */
	public static void add(JsonObject jObject, JsonSerializationContext context, String memberName, @Nullable Object value, Type type) {
		if (value == null)
			return;
		jObject.add(memberName, context.serialize(value, type));
	}
}
