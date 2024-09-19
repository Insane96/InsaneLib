package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;

@Label(name = "Base")
@LoadFeature(module = "insanelib:base", canBeDisabled = false)
public class BaseFeature extends Feature {

    public BaseFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }
}
