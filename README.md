# CSSPVP - Cope Studios Simple PVP Plugin

A comprehensive PVP management plugin for Minecraft Paper servers (version 1.21.4).

## Features

- **Arena System**: Create, configure, and manage PVP arenas
- **Team System**: Create and manage teams for team-based combat
- **Duel System**: Challenge other players to duels in specific arenas
- **Random Duel System**: Automatically select random players for duels
- **Protection System**: Prevent unwanted PVP, block breaking, etc. outside arenas
- **GUI System**: Easy-to-use graphical interfaces for all features
- **Customizable Messages**: All plugin messages can be customized

## Commands

### Main Commands

- `/csspvp` - Opens the main GUI menu
- `/csspvp reload` - Reloads configuration
- `/csspvp setspawn` - Sets the general spawn point
- `/csspvp spawn` - Teleports to the general spawn
- `/csspvp help` - Shows help information

### Arena Commands

- `/cssarena` - Opens the arena management GUI
- `/cssarena create <name>` - Creates a new arena
- `/cssarena delete <name>` - Deletes an arena
- `/cssarena setspawn <name>` - Sets the spawn point for an arena
- `/cssarena setcorner1 <name>` - Sets the first corner for an arena
- `/cssarena setcorner2 <name>` - Sets the second corner for an arena
- `/cssarena setlives <name> <lives>` - Sets the number of lives for an arena
- `/cssarena setteamsize <name> <size>` - Sets the team size for an arena
- `/cssarena join <name>` - Joins an arena
- `/cssarena leave` - Leaves the current arena
- `/cssarena start [name]` - Starts an arena
- `/cssarena info [name]` - Shows information about an arena
- `/cssarena list` - Lists all arenas

### Team Commands

- `/cssteam` - Opens the team management GUI
- `/cssteam create <name>` - Creates a new team
- `/cssteam delete <name>` - Deletes a team
- `/cssteam join <name>` - Joins a team
- `/cssteam leave` - Leaves the current team
- `/cssteam add <name> <player>` - Adds a player to a team
- `/cssteam remove <name> <player>` - Removes a player from a team
- `/cssteam info [name]` - Shows information about a team
- `/cssteam list` - Lists all teams

### Duel Commands

- `/cssduel` - Shows duel help
- `/cssduel challenge <player> <arena>` - Challenges a player to a duel
- `/cssduel <player> <arena>` - Short form of challenge command
- `/cssduel accept` - Accepts a pending duel request
- `/cssduel decline` - Declines a pending duel request
- `/cssduel random <arena>` - Starts a random duel
- `/cssrduel <arena>` - Starts a random duel between 2 random players (OP only)

## Permissions

- `csspvp.use` - Access to basic plugin features
- `csspvp.admin` - Access to all admin commands
- `csspvp.arena.create` - Ability to create and modify arenas
- `csspvp.arena.delete` - Ability to delete arenas
- `csspvp.arena.join` - Ability to join arenas
- `csspvp.team` - Access to team commands
- `csspvp.duel` - Ability to duel other players

## Configuration

### config.yml

You can configure various aspects of the plugin through the `config.yml` file:

```yaml
general:
  # Whether the general spawn is enabled
  spawn-enabled: true
  # The general spawn location (set with /csspvp setspawn)

arena:
  # Default lives per player
  default-lives: 3
  # Default team size (1 for solo)
  default-team-size: 1
  # Enable random duels
  enable-random-duels: true
  # Cooldown between random duels (in seconds)
  random-duel-cooldown: 300

# GUI customization
gui:
  # Colors can be hex codes (#RRGGBB) or color names
  title-color: "#FF5555"
  button-color: "#55FF55"
  border-color: "#5555FF"
  # Item to use for arena selection
  arena-item: "DIAMOND_SWORD"
  # Item to use for team selection
  team-item: "SHIELD"
  # Item to use for duel selection
  duel-item: "GOLDEN_SWORD"

# Protection settings
protection:
  # Prevent PVP outside arenas
  prevent-pvp-outside-arena: true
  # Prevent block breaking outside arenas
  prevent-block-break: true
  # Prevent block placing outside arenas
  prevent-block-place: true
  # Prevent item dropping outside arenas
  prevent-item-drop: true
  # Prevent hunger in lobby areas
  prevent-hunger: true
```

### messages.yml

All plugin messages can be customized through the `messages.yml` file. You can use both legacy color codes (`&`) and MiniMessage format (`<color>`) for colorizing messages.

Example:
```yaml
arena:
  created: "%prefix%<#55FF55>Arena %name% has been created successfully!</color>"
  join: "%prefix%<#55FF55>You have joined arena %name%!</color>"
```

## Setup Guide

1. Install the plugin by dropping the JAR file into your server's plugins folder.
2. Restart your server.
3. Use `/csspvp setspawn` to set the general spawn point.
4. Create an arena with `/cssarena create <n>`.
5. Configure the arena:
    - Set the spawn point with `/cssarena setspawn <n>`.
    - Define the arena bounds with `/cssarena setcorner1 <n>` and `/cssarena setcorner2 <n>`.
    - Optionally set lives and team size with `/cssarena setlives <n> <lives>` and `/cssarena setteamsize <n> <size>`.
6. Now players can join the arena with `/cssarena join <n>` or start duels with `/cssduel <player> <arena>`.

## Random Duel Feature

The plugin includes a special feature for OP players to start random duels:

- `/cssrduel <arena>` - Selects two random players (excluding the command executor) and puts them in a duel arena.
- This command doesn't require player acceptance - it forces the players into the duel.

## Tips for Administrators

- Make sure to properly define arena bounds to prevent players from escaping.
- Set appropriate permissions for your server's rank system.
- Customize messages to match your server's theme.
- Use the GUI system for easier management.
- Regular players need only the basic permissions: `csspvp.use`, `csspvp.arena.join`, `csspvp.team`, and `csspvp.duel`.

## Support

For issues or feature requests, please contact the plugin author or open an issue on the plugin's repository.