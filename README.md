# SwitchPvP
A Minecraft 1.21.5 PvP minigame plugin with switchable kits

# SwitchPvP - Minecraft PvP Plugin

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.5%2B-blue)
![License](https://img.shields.io/github/license/yourusername/SwitchPvP)

A dynamic PvP plugin for Minecraft servers featuring random kits, kill streaks, and persistent leaderboards.

## ✨ Features
- 🎮 **Random Kit System** - Players receive different equipment each spawn
- 🔥 **Kill Streaks** - Track and reward consecutive kills
- 🏆 **Persistent Leaderboard** - Shows top players with visual armor stands
- ⏱️ **Auto Game Start** - Begins when enough players join
- ⚙️ **Fully Configurable** - Customize spawns, countdowns, and more
- 📊 **Statistics Tracking** - Records player kills across sessions

## 🚀 Installation
1. Download the latest `.jar` from [Releases](https://github.com/yourusername/SwitchPvP/releases)
2. Place in your server's `plugins` folder
3. Restart your server
4. Configure using the commands below

## 🎮 Game Flow
1. Players join lobby with `/spvp join`
2. When 2+ players are ready, countdown begins
3. Players teleport to random arena spawns with random kits
4. Battle until one player remains
5. Winner is announced and players return to lobby
6. New game starts automatically if enough players remain

## 📋 Commands

### Player Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/spvp join` | Join the PvP arena | None |
| `/spvp leave` | Leave the current game | None |
| `/spvp stats` | View your PvP statistics | None |

### Admin Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/spvp setlobby` | Set lobby location | `switchpvp.admin` |
| `/spvp addspawn` | Add arena spawn point | `switchpvp.admin` |
| `/spvp removespaw` | Remove nearest spawn | `switchpvp.admin` |
| `/spvp clearspawns` | Remove all spawns | `switchpvp.admin` |
| `/spvp createboard` | Create leaderboard | `switchpvp.admin` |
| `/spvp removeboard` | Remove leaderboard | `switchpvp.admin` |
| `/spvp setcountdown [sec]` | Set countdown time | `switchpvp.admin` |
| `/spvp resetstats` | Reset all stats | `switchpvp.admin` |
| `/spvp start` | Force start game | `switchpvp.admin` |
| `/spvp end` | Force end game | `switchpvp.admin` |

## ⚙️ Configuration
The plugin automatically generates `config.yml` with these settings:

```yaml
lobby: world,100,64,100,0,0  # Lobby coordinates
arena:
  spawns: []  # List of arena spawn points
lobby-countdown: 5  # Countdown time in seconds
persistent-stats: {}  # Player kill statistics
coreboard:
  location: null  # Leaderboard location
