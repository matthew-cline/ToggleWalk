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

    /**
     * How much time (in ticks) the player has to release the key after first
     * pressing it for it to count as a "key tap".
     *
     * 20 ticks = 1 second
     */
    private long keyTapDelay = 1; // 0.05 seconds

    private boolean toggled              = false;
    private boolean disabled             = false;
    private long    key_release_deadline = -1;

    private void setToggled(boolean toggled) {
        if (disabled)
            toggled = false;

        if (this.toggled != toggled)
            key_release_deadline = -1;

        this.toggled = toggled;
    }

    /////////////////////// Implements ToggleableKeyBinding /////////////////

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
        if (disabled)
            return;

        if (wasPressed && oppositeWasPressed)
            System.err.println("ERROR: toggle and untoggle pressed at same " +
                    "time!!");

        // We want to only toggle to on if the player briefly taps the key.
        // That way if the player holds down "W" to walk around for a few
        // second it won't count as a tap, WON'T toggle it on, and when
        // the player releases the key the PC will stop walking.
        if (wasPressed) {
            if (toggled)
                setToggled(false);
            else if (key_release_deadline == -1)
                key_release_deadline = time + keyTapDelay;
        }
        else {
            if (!toggled && key_release_deadline != -1) {
                if (time <= key_release_deadline)
                    setToggled(true);

                key_release_deadline = -1;
            }
        }

        if (oppositeWasPressed)
            setToggled(false);
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        if (disabled) {
            setToggled(false);
            key_release_deadline = -1;
        }
    }

    @Override
    public void setKeyTapDelay(long delay) {
        keyTapDelay = delay;
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
        if (toggled) {
            info.setReturnValue(true);
        }
    }

    @Accessor(value = "keysById")
    public static Map<String, KeyBinding> getKeysById() {
        throw new NotImplementedException("keysById mixin failed to apply.");
    }
}
