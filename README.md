# ToggleWalk Mk. II for minecraft

A sequel to the original
[ToggleWalk](https://www.curseforge.com/minecraft/mc-mods/togglewalk) mod by
[ExtraCraftTX](https://www.curseforge.com/members/extracraftx/followers).  The
main differences are that you toggle by briefly tapping a key, and that toggling
can be temporarily disabled.  [**WARNING**: If you have the original ToggleWalk
mod installed you'll have to remove it to get ToggleWalk Mk. II to work.]

Makes it so that various keybinds can be toggled instead of being held! The
keybinds can be configured through the config file, found in the `config`
folder and named `togglewalk_mk_ii.json`.

In order to toggle a keybinding on you must briefly tap the key bound to it
(that is, hold it down for only 0.05 second or less).  If you hold the key down
for longer than that then when you release the key the keybinding will *not* be
toggled on.  This means that, for example, you can hold down `w` to walk around
like normal and when you release `w` you'll stop walking.  To stop a keybinding
that has been toggled on, again tap the key bound to it.

If you want to temporarily disable toggling you can enter the command `/tw
off`, then enter `/tw on` to re-enable it.

Requires the [Fabric API](https://minecraft.curseforge.com/projects/fabric).

## Config

In the "`toggles`" array the "`toggle`" field is the key that will be toggled
upon being tapped and the "`untoggle`" field (optional) is the key that, upon
being tapped, will stop the toggled key. If the config file is deleted, a
default one will be generated on the next start.

The default Minecraft keybinds can be any of:

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

For keybinds provided by mods the keybind name needs to be preceded by the
mod's id and a dot.  For instance, the
[Grid](https://www.curseforge.com/minecraft/mc-mods/grid) keybind "`here`" must
be specified as "`grid.here`".

"`keyTapDelay`" is the maximum amount of time in ticks (20 ticks = 1 second)
that you can hold down a key for it to count as a "tap" which will toggle a
keybinding on.  If 0.05 seconds is too short for you then you can increase it.
