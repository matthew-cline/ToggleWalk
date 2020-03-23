package com.extracraftx.minecraft.togglewalk.interfaces;

import java.util.Map;

import net.minecraft.client.options.KeyBinding;

public interface ToggleableKeyBinding {
    public Map<String,KeyBinding> getKeysIdMap();

    /**
     * We can't access the method KeyBinding.wasPressed(), created by Sponge
     * due to injecting into KeyBinding.isPressed(), due to the way that
     * the Sponge classloader works, so wasPressed() has to be accessed
     * from within the non-mixin class ToggleWalk and passed to the
     * KeyBindingMixin class.
     */
    public void handleToggleTick(long time, boolean wasPressed,
                                 boolean oppositeWasPressed);

    /**
     * We can't directly reference the KeyBindingMixin class from within itself
     * because of the way the Sponge classloder works, so we can't reference
     * the private members of other intances of this class.  For instance, this
     * is illegal run time:
     *
     *     opposite.toggled = false;
     *
     * Do note that the code will compile, even though it won't run.
     */
    public void setToggled(boolean toggled);
}
