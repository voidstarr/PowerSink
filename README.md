# PowerSink

### [Discord](https://discord.gg/eeZbM9umBy)

Sponge API 7.3.x Plugin that allows players to pay for energy, or earn money selling energy.

This plugin is in a rough proof of concept state.

Currently supported energy backends:
  * Forge
  * Mekanism
  
Current implementation borrows from [PowerMoney](https://github.com/AuraDevelopmentTeam/PowerMoney)'s MoneyCalculator.

## Definitions
  * Sink: `block to which energy is added`
  * Source: `block from which energy is removed`
  * Node: `block that is either a Sink or Source`

## Usage
To register a Sink, punch an energy storage block with a piece of glowstone dust.

To register a Source, punch an energy storage block with a piece of redstone.

To remove a Node, punch it with a block of bedrock.

Those items are configurable in `config/powersink/powersink.conf`:
```
activationItems {
    remove="minecraft:bedrock"
    sink="minecraft:glowstone_dust"
    source="minecraft:redstone"
}
```

## Commands
* `/powersink` or `/ps`: lists sub commands
* `/ps list <player>`: lists energy nodes for `<player>` or self
    * if used with `minecraft.commands.tp`, a TextAction will be generated to allow teleportation to nodes
    * if used with `powersink.remove.[self|other]`, a TextAction will be generated to allow removal of nodes from PowerSink

## General configuration values
```
powersink {
    currency=dollar #change this to match your economy's currency 
    tickInterval=2 #how often should PowerSink interact with Nodes?
}
```

## Permissions
  * `powersink.setup` register Sinks and Sources
  * `powersink.setup.sink` register Sinks
  * `powersink.setup.source` register Sources
  * `powersink.list` list Nodes for self or others
  * `powersink.list.self` list Nodes for self
  * `powersink.list.other` list Nodes for others
  * `powersink.remove` remove Nodes for self or others
  * `powersink.remove.self` remove Nodes for self
  * `powersink.remove.other` remove Nodes for others

### `powersink.limit.[admin|player|vip|*]` is special
  In `config/powersink/powersink.conf`, there is a block that looks like this:
  ```
    # Group limits on sources and sinks.
    limits=[
        {
            group=admin
            sink=1000
            source=1000
        },
        {
            group=player
            sink=4
            source=4
        }
    ]
  ```
You can define limit groups here, and assign that group to players. Suppose you create a group named `vip`, to give that limit to players, assign the `powersink.limit.vip` permission.
**Limit groups are evaluated in the order they are defined within the `limit` list in the config. Be conscious of this when assigning multiple limit groups to players.**