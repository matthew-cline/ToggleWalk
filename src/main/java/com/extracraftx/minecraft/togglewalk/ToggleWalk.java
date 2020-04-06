package com.extracraftx.minecraft.togglewalk;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.extracraftx.minecraft.togglewalk.config.Config;
import com.extracraftx.minecraft.togglewalk.config.Config.Toggle;
import com.extracraftx.minecraft.togglewalk.interfaces.ToggleableKeyBinding;
import com.extracraftx.minecraft.togglewalk.mixin.KeyBindingMixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
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
    private static boolean                 modFailed;

    private ToggleableKeyBinding[] bindings;
    private KeyBinding[]           opposites;
    private KeyBinding[]           baseBindings;
    private Set<String>            usedBindings;
    private Map<String, Boolean>   wasPressed;
    private ClientWorld            prevTickWorld;
    private String                 failMsg;

    public void onTick(MinecraftClient mc) {
        // Wait for world to be available to give any error messages via
        // in-game chat.
        if (prevTickWorld == null && mc.world != null) {
            if (modFailed && failMsg != null)
                sendMsg(mc.player, failMsg);
        }

        prevTickWorld = mc.world;

        if (modFailed)
            return;

        if (bindings == null) {
            load(mc);
            if (modFailed)
                return;
        }

        if (mc.world == null)
            // No world yet in which to do anything.
            return;

        // NOTE: KeyBindingMixin can't access KeyBinding.wasPressed(), so we
        // have to do it for them.
        //
        // NOTE: wasPressed() has the side effect of resetting the value to
        // the default (false) and is only updated on the next tick.  Since
        // we might invoke wasPressed() twice for the same binding (once
        // for toggle and once for untoggle) we have to remmeber what the
        // return value was when it was first invoked this tick.
        for (String id : usedBindings)
            wasPressed.put(id, keysById.get(id).wasPressed());

        long time = mc.world.getTime();
        for (int i = 0; i < bindings.length; i++) {
            boolean pressed    = wasPressed.get(baseBindings[i].getId());
            boolean oppPressed = opposites[i] == null ?
                false : wasPressed.get(opposites[i].getId());
            bindings[i].handleToggleTick(time, pressed, oppPressed);
        }
    }

    private void load(MinecraftClient mc) {
        keysById = ((ToggleableKeyBinding)mc.options.keyForward).getKeysIdMap();

        usedBindings = new HashSet<>();
        wasPressed = new HashMap<>();

        Config conf = Config.getInstance();

        if (conf == null) {
            modFailed = true;
            failMsg = "ToggleWalk error: couldn't load config file";
            return;
        }

        bindings     = new ToggleableKeyBinding[conf.toggles.length];
        opposites    = new KeyBinding[conf.toggles.length];
        baseBindings = new KeyBinding[conf.toggles.length];

        for (int i = 0; i < bindings.length; i++) {
            Toggle toggle = conf.toggles[i];

            baseBindings[i] = keysById.get("key." + toggle.toggle);
            opposites[i]    = keysById.get("key." + toggle.untoggle);
            bindings[i]     = (ToggleableKeyBinding) baseBindings[i];

            if (baseBindings[i] != null)
                usedBindings.add(baseBindings[i].getId());
            if (opposites[i] != null)
                usedBindings.add(opposites[i].getId());

            bindings[i].setKeyTapDelay(conf.keyTapDelay);
            bindings[i].setID(toggle.toggle);
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private void sendMsg(ClientPlayerEntity sender, String msg) {
        sender.addChatMessage(new LiteralText(msg), false);
    }

    private void cmdOn(ClientPlayerEntity sender) {
        for (int i = 0; i < bindings.length; i++) {
            bindings[i].setDisabled(false);
        }
        sendMsg(sender, "toggling enabled");
    }

    private void cmdOff(ClientPlayerEntity sender) {
        for (int i = 0; i < bindings.length; i++) {
            bindings[i].setDisabled(true);
        }
        sendMsg(sender, "toggling disabled");
    }

    private void cmdHelp(ClientPlayerEntity sender) {
        String str = "/tw off: disable ToggleWalk toggling\n" +
                     "/tw on:  enable ToggleWalk toggling";
        sendMsg(sender, str);
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
