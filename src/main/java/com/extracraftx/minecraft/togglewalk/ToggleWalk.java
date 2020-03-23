package com.extracraftx.minecraft.togglewalk;

import java.util.Map;

import com.extracraftx.minecraft.togglewalk.config.Config;
import com.extracraftx.minecraft.togglewalk.config.Config.Toggle;
import com.extracraftx.minecraft.togglewalk.interfaces.ToggleableKeyBinding;
import com.extracraftx.minecraft.togglewalk.mixin.KeyBindingMixin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;

public class ToggleWalk implements ClientModInitializer {

    private static Map<String, KeyBinding> keysById;

    private ToggleableKeyBinding[] bindings;
    private KeyBinding[]           opposites;
    private KeyBinding[]           baseBindings;

    @Override
    public void onInitializeClient() {
        ClientTickCallback.EVENT.register((mc)->{
            onTick(mc);
        }); }

    public void onTick(MinecraftClient mc){
        if(bindings == null){
            keysById = ((ToggleableKeyBinding)mc.options.keyForward).
                getKeysIdMap();
            load(mc);
        }

        if (mc.world == null)
            // No world yet in which to do anything.
            return;

        long time = mc.world.getTime();
        for(int i = 0; i < bindings.length; i++) {
            // NOTE: KeyBindingMixin can't access KeyBinding.wasPressed(),
            // so we have to do it for them.
            boolean pressed    = baseBindings[i].wasPressed();
            boolean oppPressed = opposites[i] == null ?
                false : opposites[i].wasPressed();
            bindings[i].handleToggleTick(time, pressed, oppPressed);
        }
    }

    public void load(MinecraftClient mc){
        Config.loadConfigs();

        bindings     = new ToggleableKeyBinding[Config.INSTANCE.toggles.length];
        opposites    = new KeyBinding[Config.INSTANCE.toggles.length];
        baseBindings = new KeyBinding[Config.INSTANCE.toggles.length];

        for(int i = 0; i < bindings.length; i++){
            Toggle toggle = Config.INSTANCE.toggles[i];

            baseBindings[i] = keysById.get("key." + toggle.toggle);
            opposites[i]    = keysById.get("key." + toggle.untoggle);
            bindings[i]     = (ToggleableKeyBinding) baseBindings[i];
        }
    }
}
