package com.extracraftx.minecraft.togglewalk.interfaces;

import java.util.Map;

import net.minecraft.client.options.KeyBinding;

public interface ToggleableKeyBinding {
    public Map<String,KeyBinding> getKeysIdMap();

    public void setDisabled(boolean disabled);
    public void setKeyTapDelay(long delay);
    public void setID(String ID);

    /**
     * We can't access the method KeyBinding.wasPressed(), created by Sponge
     * due to injecting into KeyBinding.isPressed(), due to the way that
     * the Sponge classloader works, so wasPressed() has to be accessed
     * from within the non-mixin class ToggleWalk and passed to the
     * KeyBindingMixin class.
     */
    public void handleToggleTick(long time, boolean wasPressed,
                                 boolean oppositeWasPressed);
}
