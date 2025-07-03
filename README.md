# SwitchPvP
A Minecraft 1.21.5 PvP minigame plugin with switchable kits

# SwitchPvP - Minecraft PvP Plugin

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.5%2B-blue)

A dynamic PvP plugin for Minecraft servers featuring random kits, kill streaks, and persistent leaderboards.

## ✨ Features
- 🎮 **Random Kit System** - Players receive different equipment each spawn
- 🏆 **Leaderboard** - Shows top player with visual armor stand
- ⏱️ **Auto Game Start** - Begins when 2 or more players join
- ⚙️ **Fully Configurable** - Customize spawns, countdown, and more
- 📊 **Statistics Tracking** - Records player kills across sessions

## 🚀 Installation
1. Download the latest `.jar` from [Releases](https://github.com/Pirat1345/SwitchPvP/releases)
2. Place in your server's `plugins` folder
3. Restart your server
4. Configure using the commands below

## ⚙️ Setup
1. set Lobby with `/switchpvp set lobby`
2. set as many arena spawnpoints with `/switchpvp set arena` as you want
3. now put items in your inventory you want in your fist kit.
4. to save your fist kit make `/switchpvp kit create [kit name]`
5. create more kits
6. set Scorebord (Corebord) with `/switchpvp set corebord`

## 🎮 Game Flow short version
1. Players join lobby with `/spvp join`
2. When 2+ players are ready, countdown begins
3. Players teleport to random arena spawns with random kits
4. Battle until one player leaves
5. Winner is announced and player return to lobby
6. New game starts automatically if enough players remain

## 🎮 Game Flow long version
You use `/switchpvp join` and you get teleported to the lobby.
If there are 2 or more players in the lobby,
the game will start the cooldown.
After the cooldown, 
you will be spawned at an arena spawn point and get a kit. 
You fight against each other until one dies. 
The winner and the loser will receive a new random kit and can continue fighting. 
You can get out with `/switchpvp leave`.

## 📹 Tutorial
 [Youtube](https://www.youtube.com/watch?v=_upK_Z0NRxc&ab_channel=Pirat1345)

## 📋 Commands

### Player Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/spvp join` | Join the PvP arena | None |
| `/spvp leave` | Leave the current game | None |

### Admin Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/spvp set lobby` | Set lobby location | `switchpvp.admin` |
| `/spvp set arena` | Add arena spawns point | `switchpvp.admin` |
| `/spvp remove arena` | Remove nearest spawn | `switchpvp.admin` |
| `/spvp clear arenas` | Remove all spawns | `switchpvp.admin` |
| `/spvp set coreboard` | Create leaderboard | `switchpvp.admin` |
| `/spvp remove coreboard` | Remove leaderboard | `switchpvp.admin` |
| `/spvp set countdown [sec]` | Set countdown time | `switchpvp.admin` |
| `/spvp reset stats` | Reset all stats | `switchpvp.admin` |
| `/spvp kit create <name>` | create Kit | `switchpvp.admin` |
| `/spvp kit list` | list all Kits | `switchpvp.admin` |
| `/spvp kit delete <name>` | delete Kit | `switchpvp.admin` |

## ⚙️ Configuration
The plugin automatically generates `config.yml`
