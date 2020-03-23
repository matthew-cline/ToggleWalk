package com.extracraftx.minecraft.togglewalk.mixin;

import java.util.Map;

import com.extracraftx.minecraft.togglewalk.interfaces.ToggleableKeyBinding;

import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.options.KeyBinding;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin implements ToggleableKeyBinding{
    private boolean toggled = false;

    /////////////////////// Implements ToggleableKeyBinding /////////////////
    @Override
    public void toggle() {
        toggled = !toggled;
    }

    @Override
    public void setToggled(boolean value) {
        toggled = value;
    }

    @Override
    public Map<String, KeyBinding> getKeysIdMap() {
        return getKeysById();
    }

    /////////////////////////// Non-interface methods ////////////////////////

    /*
     * NOTE: this seems to create a wasPressed() method in
     * net.minecraft.client.options.KeyBinding which returns what the value
     * WOULD be if our injected code hadn't messed with anything.  However, I
     * can't find any documentation for this.
     */
    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    public void onIsPressed(CallbackInfoReturnable<Boolean> info) {
        if(toggled){
            info.setReturnValue(true);
        }
    }

    @Accessor(value = "keysById")
    public static Map<String, KeyBinding> getKeysById(){
        throw new NotImplementedException("keysById mixin failed to apply.");
    }
}
