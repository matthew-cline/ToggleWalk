package com.extracraftx.minecraft.togglewalk.interfaces;

import java.util.Map;

import net.minecraft.client.options.KeyBinding;

public interface ToggleableKeyBinding {
    public void                   toggle();
    public void                   setToggled(boolean value);
    public Map<String,KeyBinding> getKeysIdMap();
}
