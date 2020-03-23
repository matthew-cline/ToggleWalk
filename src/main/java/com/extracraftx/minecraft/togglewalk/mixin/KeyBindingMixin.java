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
    private boolean              toggled = false;
    private ToggleableKeyBinding opposite = null;

    private void toggle() {
        toggled = !toggled;
    }

   /////////////////////// Implements ToggleableKeyBinding /////////////////

    @Override
    public void init(ToggleableKeyBinding opposite) {
        if (this.opposite != null) {
            System.err.println("WARNING: init() already called on " +
                    "KeyBindingMixin");
            return;
        }

        this.opposite = opposite;
    }

    /**
     * We can't access the method KeyBinding.wasPressed(), created by Sponge
     * due to injecting into KeyBinding.isPressed(), due to the way that
     * the Sponge classloader works, so wasPressed() has to be accessed
     * from within the non-mixin class ToggleWalk and passed to the
     * KeyBindingMixin class.
     */
    @Override
    public void handleToggleTick(long time, boolean wasPressed,
                                 boolean oppositeWasPressed) {
        if(wasPressed)
            toggle();
        if(oppositeWasPressed)
            opposite.setToggled(false);
    }

    /**
     * We can't directly reference the KeyBindingMixin class here because
     * of the way the Sponge classloder works, so we can't reference the
     * private members of other intances of this class.  For instance, this
     * is illegal run time:
     *
     *     opposite.toggled = false;
     *
     * Do note that the code will compile, even though it won't run.
     */
    @Override
    public void setToggled(boolean toggled) {
        this.toggled = toggled;
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
     *
     * Also note that do to the way that Sponge works that the wasPressed()
     * method can't be accessed from within this class, no matter what
     * coding tricks are done; any attempt to do so in code whill result
     * in an IllegalClassLoadError at runtime.
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
