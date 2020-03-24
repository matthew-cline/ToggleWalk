package com.extracraftx.minecraft.togglewalk;

import java.util.Map;

import com.extracraftx.minecraft.togglewalk.config.Config;
import com.extracraftx.minecraft.togglewalk.config.Config.Toggle;
import com.extracraftx.minecraft.togglewalk.interfaces.ToggleableKeyBinding;
import com.extracraftx.minecraft.togglewalk.mixin.KeyBindingMixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.LiteralText;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;

import static io.github.cottonmc.clientcommands.ArgumentBuilders.argument;
import static io.github.cottonmc.clientcommands.ArgumentBuilders.literal;
import        io.github.cottonmc.clientcommands.ClientCommandPlugin;
import        io.github.cottonmc.clientcommands.ClientCommands;
import        io.github.cottonmc.clientcommands.CottonClientCommandSource;

public class ToggleWalk implements ClientModInitializer, ClientCommandPlugin {

    private static Map<String, KeyBinding> keysById;
    private static ToggleWalk              instance;

    private ToggleableKeyBinding[] bindings;
    private KeyBinding[]           opposites;
    private KeyBinding[]           baseBindings;

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

    private void cmdOn(ClientPlayerEntity sender) {
        for(int i = 0; i < bindings.length; i++) {
            bindings[i].setDisabled(false);
        }
        sender.addChatMessage(new LiteralText("toggling enabled"), false);
    }

    private void cmdOff(ClientPlayerEntity sender) {
        for(int i = 0; i < bindings.length; i++) {
            bindings[i].setDisabled(true);
        }
        sender.addChatMessage(new LiteralText("toggling disabled"), false);
    }

    private void cmdHelp(ClientPlayerEntity sender) {
        String str = "/tw off: disable ToggleWalk toggling\n" +
                     "/tw on:  enable ToggleWalk toggling";
        sender.addChatMessage(new LiteralText(str), false);
    }

    ////////////////////////////////////////////////////////////////////////
    ////////////////////// ClientModInitializer methods ////////////////////
    ////////////////////////////////////////////////////////////////////////
    @Override
    public void onInitializeClient() {
        instance = this;
        ClientTickCallback.EVENT.register((mc)->{
            onTick(mc);
        });
    }

    ////////////////////////////////////////////////////////////////////////
    ////////////////////// ClientCommandPlugin methods /////////////////////
    ////////////////////////////////////////////////////////////////////////


    /**
     * NOTE: this code only works with Minecraft 1.15+, and is incompatible
     * with 1.14
     */
    @Override
    public
    void registerCommands(CommandDispatcher<CottonClientCommandSource> cd) {
        cd.register(
            literal("tw")
                .then(
                    literal("on").executes(c->{
                        instance.cmdOn(MinecraftClient.getInstance().player);
                        return 1;
                    })
                )
                .then(
                    literal("off").executes(c->{
                        instance.cmdOff(MinecraftClient.getInstance().player);
                        return 1;
                    })
                )
                .then(
                    literal("help").executes(c->{
                        instance.cmdHelp(MinecraftClient.getInstance().player);
                        return 1;
                    })
                )
                .executes(c->{
                    instance.cmdHelp(MinecraftClient.getInstance().player);
                    return 1;
                })
        );
    }
}
