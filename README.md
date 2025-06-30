# SwitchPvP
A Minecraft 1.21.5 PvP minigame plugin with switchable kits

Features
Random kit system - players get different equipment each time they spawn
Kill streak tracking with rewards
Persistent leaderboard showing top players
Customizable lobby and arena spawn points
Automatic game start when enough players join
Configurable lobby countdown timer
Player statistics tracking (total kills)
Visual leaderboard with armor stands
Game Flow
Players join the lobby using /spvp join
When 2+ players are in lobby, a countdown starts automatically
After countdown, game starts and players are teleported to random arena spawns
Players receive random kits and fight until the game ends
When only 1 player remains, the game ends and winner is announced
Players are returned to lobby, and a new game can start automatically
Commands
Player Commands
| Command | Description | Permission | |---------|------------|------------| | /spvp join | Join the PvP game | None | | /spvp leave | Leave the PvP game | None | | /spvp stats | View your PvP statistics | None |

Admin Commands
| Command | Description | Permission | |---------|------------|------------| | /spvp setlobby | Set the lobby spawn point | switchpvp.admin | | /spvp addspawn | Add an arena spawn point | switchpvp.admin | | /spvp removespaw | Remove nearest arena spawn | switchpvp.admin | | /spvp clearspawns | Remove all arena spawns | switchpvp.admin | | /spvp createboard | Create the leaderboard at your location | switchpvp.admin | | /spvp removeboard | Remove the leaderboard | switchpvp.admin | | /spvp setcountdown [seconds] | Set lobby countdown time | switchpvp.admin | | /spvp resetstats | Reset all player statistics | switchpvp.admin | | /spvp start | Force start the game | switchpvp.admin | | /spvp end | Force end the game | switchpvp.admin |

Configuration
The plugin automatically creates a config.yml with these settings:

lobby: Lobby spawn location
arena.spawns: List of arena spawn locations
lobby-countdown: Countdown time in seconds (default: 5)
persistent-stats: Player kill statistics (UUID -> kills)
coreboard.location: Leaderboard location
Installation
Place the plugin JAR in your server's plugins folder
Restart your server
Configure spawn points and lobby using admin commands
Players can join using /spvp join
