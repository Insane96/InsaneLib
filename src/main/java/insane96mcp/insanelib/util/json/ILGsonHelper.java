package insane96mcp.insanelib.util.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import insane96mcp.insanelib.util.json.validator.Validator;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class ILGsonHelper {
	/**
	 * Wrapper of GsonHelper.getAsInt, but if there is no member returns null
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
	 * Wrapper of GsonHelper.getAsDouble, but if there is no member returns null
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
	 * Wrapper of GsonHelper.getAsFloat, but if there is no member returns null
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
	 * Wrapper of GsonHelper.getAsInt, but if there is no member returns null
	 */
	@Nullable
	public static Integer getAsNullableInt(JsonObject jObject, String memberName) {
		return getAsNullableInt(jObject, memberName, null);
	}
	/**
	 * Wrapper of GsonHelper.getAsDouble, but if there is no member returns null
	 */
	@Nullable
	public static Double getAsNullableDouble(JsonObject jObject, String memberName) {
		return getAsNullableDouble(jObject, memberName, null);
	}
	/**
	 * Wrapper of GsonHelper.getAsFloat, but if there is no member returns null
	 */
	@Nullable
	public static Float getAsNullableFloat(JsonObject jObject, String memberName) {
		return getAsNullableFloat(jObject, memberName, null);
	}
	/**
	 * Wrapper of GsonHelper.getAsBoolean, but if there is no member returns null
	 */
	@Nullable
	public static Boolean getAsNullableBoolean(JsonObject jObject, String memberName) {
		if (!jObject.has(memberName))
			return null;
		return GsonHelper.getAsBoolean(jObject, memberName);
	}

	/**
	 * Writes the property to the JsonObject, but ignores it if it's null
	 */
	public static void addProperty(JsonObject jObject, String memberName, @Nullable Object value) {
		if (value == null)
			return;
		if (value instanceof Number number)
			jObject.addProperty(memberName, number);
		else if (value instanceof Character character)
			jObject.addProperty(memberName, character);
		else if (value instanceof String string)
			jObject.addProperty(memberName, string);
		else if (value instanceof Boolean bool)
			jObject.addProperty(memberName, bool);
		else
			throw new ClassCastException("Cannot add value of type %s to %s".formatted(value.getClass(), jObject));
	}

	/**
	 * Writes the property to the JsonObject, but ignores it if it's null
	 */
	public static void add(JsonObject jObject, JsonSerializationContext context, String memberName, @Nullable Object value) {
		if (value == null)
			return;
		jObject.add(memberName, context.serialize(value));
	}

	/**
	 * Writes the property to the JsonObject, but ignores it if it's null
	 */
	public static void add(JsonObject jObject, JsonSerializationContext context, String memberName, @Nullable Object value, Type type) {
		if (value == null)
			return;
		jObject.add(memberName, context.serialize(value));
	}
}
