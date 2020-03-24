# ToggleWalk for minecraft

Makes it so that various keybinds can be toggled instead of being held! The keybinds can be configured through the config file, found in the `config` folder and is called `togglewalk_config.json`.

In order to toggle a keybinding on you must briefly tap the key bound to it
(that is, hold it down for only 0.1 second or less).  If you hold the key down
for longer than that then when you release the key the keybinding will *not* be
toggled on.  This means that, for example, you can hold down `w` to walk around
like normal and when you release `w` you'll stop walking.  To stop a keybinding
that has been toggled on, again tap the key bound to it.

If you want to temporarily disable toggling you can enter the command `/tw
off`, then enter `/tw on` to re-enable it.

Requires the [Fabric API](https://minecraft.curseforge.com/projects/fabric).

## Config

The "toggle" is the key that will be toggled upon being pressed and the "untoggle" (optional) is the key that, upon being pressed, will stop the toggled key. If the config file is deleted, a default one will be generated on the next start.

The keybinds can be any of:
* `forward`
* `back`
* `left`
* `right`
* `sneak`
* `jump`
* `sprint`
* `hotbar.1`, `hotbar.2`, etc.
* `chat`
* `attack`
* `use`
* `command`
* `inventory`
* `drop`
* `playerlist`
* `swapHands`
* `togglePerspective`
* `spectatorOutlines`
* `pickItem`
* `fullscreen`
* `smoothCamera`
* `advancements`
* `loadToolbarActivator`
* `saveToolbarActivator`
* `screenshot`
