package insane96mcp.insanelib.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import javax.annotation.Nullable;

/**
 * Same as a {@link RangedAttribute}, but the {@code descriptionId} is calculated from the attribute id and can define a {@code baseId} (green modifier).
 */
public class ILRangedAttribute extends RangedAttribute {
	@Nullable
	private final Identifier baseId;
	private String descriptionId;

	public ILRangedAttribute(double defaultValue, double min, double max) {
		this(defaultValue, min, max, null);
	}

	public ILRangedAttribute(double defaultValue, double min, double max, Identifier baseId) {
		super("", defaultValue, min, max);
		this.baseId = baseId;
	}

	protected String getOrCreateDescriptionId() {
		if (this.descriptionId == null)
			this.descriptionId = "attribute.name." + BuiltInRegistries.ATTRIBUTE.getKey(this).getPath();

		return this.descriptionId;
	}

	@Override
	public String getDescriptionId() {
		return this.getOrCreateDescriptionId();
	}

	@Override
	@Nullable
	public Identifier getBaseId() {
		return this.baseId;
	}
}
