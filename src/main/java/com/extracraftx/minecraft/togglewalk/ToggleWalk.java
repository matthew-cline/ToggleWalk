package com.extracraftx.minecraft.togglewalk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.extracraftx.minecraft.togglewalk.config.Config;
import com.extracraftx.minecraft.togglewalk.config.Config.Toggle;
import com.extracraftx.minecraft.togglewalk.interfaces.ToggleableKeyBinding;
import com.extracraftx.minecraft.togglewalk.mixin.KeyBindingMixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
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

    private BindingTuple[]         tuples;
    private Set<String>            usedBindings;
    private Map<String, Boolean>   wasPressed;
    private ClientWorld            prevTickWorld;
    private List<String>           errMsgs;

    public void onTick(MinecraftClient mc) {
        // Wait for world to be available to give any error messages via
        // in-game chat.
        if (prevTickWorld == null && mc.world != null) {
            for (String msg : errMsgs)
                sendMsg(mc.player, "ToggleWalk error: " + msg);
        }

        prevTickWorld = mc.world;

        if (modFailed)
            return;

        if (tuples == null) {
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
        for (int i = 0; i < tuples.length; i++) {
            boolean pressed    = wasPressed.get(tuples[i].base.getTranslationKey());
            boolean oppPressed = tuples[i].opposite == null ?
                false : wasPressed.get(tuples[i].opposite.getTranslationKey());
            tuples[i].toggleable.handleToggleTick(time, pressed, oppPressed);
        }
    }

    private void load(MinecraftClient mc) {
        keysById = ((ToggleableKeyBinding)mc.options.keyForward).getKeysIdMap();

        usedBindings = new HashSet<>();
        wasPressed   = new HashMap<>();
        errMsgs      = new ArrayList<>();

        Config conf = Config.getInstance();

        if (conf == null) {
            modFailed = true;
            errMsgs.add("couldn't load config file");
            return;
        }

        // Simply discard any conf.toggle entry with a missing or invalid
        // "toggle" field, only keeping valid entries for other methods to
        // loop over.
        List<BindingTuple> tupleList = new ArrayList<>();

        for (int i = 0; i < conf.toggles.length; i++) {
            Toggle       toggle = conf.toggles[i];
            BindingTuple tup    = new BindingTuple();

            if (toggle.toggle == null) {
                errMsgs.add("a 'toggles' entry is missing the 'toggle' field");
                continue;
            }

            tup.base       = keysById.get("key." + toggle.toggle);
            tup.toggleable = (ToggleableKeyBinding) tup.base;

            if (tup.base == null) {
                errMsgs.add("no such keybinding as '" + toggle.toggle + "'");
                continue;
            }

            usedBindings.add(tup.base.getTranslationKey());
            tup.toggleable.setID(toggle.toggle);
            tup.toggleable.setKeyTapDelay(conf.keyTapDelay);

            if (toggle.untoggle != null) {
                tup.opposite = keysById.get("key." + toggle.untoggle);

                if (tup.opposite == null) {
                    errMsgs.add("no such keybinding as '" +
                                toggle.untoggle + "'");
                    continue;
                }
                usedBindings.add(tup.opposite.getTranslationKey());
            }
            tupleList.add(tup);
        }
        tuples = tupleList.toArray(new BindingTuple[0]);
    }

    ///////////////////////////////////////////////////////////////////////////

    private void sendMsg(ClientPlayerEntity sender, String msg) {
        sender.sendMessage(new LiteralText(msg), false);
    }

    private void cmdOn(ClientPlayerEntity sender) {
        for (int i = 0; i < tuples.length; i++)
            tuples[i].toggleable.setDisabled(false);
        sendMsg(sender, "toggling enabled");
    }

    private void cmdOff(ClientPlayerEntity sender) {
        for (int i = 0; i < tuples.length; i++)
            tuples[i].toggleable.setDisabled(true);
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

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    private static class BindingTuple {
        public ToggleableKeyBinding toggleable;
        public KeyBinding           opposite;
        public KeyBinding           base;
    }
}
