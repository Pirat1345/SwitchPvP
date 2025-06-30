package me.Pirat1345.switchPvP;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;
import java.util.stream.Collectors;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class GameManager implements Listener {
    private final SwitchPvP plugin;
    private Location lobbyLocation;
    private List<Location> arenaSpawns = new ArrayList<>();
    private Set<Player> playersInGame = new HashSet<>();
    private Set<Player> playersInLobby = new HashSet<>();
    private Map<Player, Integer> killStreaks = new HashMap<>();
    private Map<String, Integer> persistentStats = new HashMap<>(); // UUID -> kills
    private Set<UUID> playersToRespawn = new HashSet<>();
    private ArmorStand coreBoard;
    private List<ArmorStand> leaderboardStands = new ArrayList<>();
    private boolean gameActive = false;
    private boolean lobbyCountdownActive = false;
    private BukkitTask countdownTask;
    private int lobbyCountdownTime = 5;

    public GameManager(SwitchPvP plugin) {
        this.plugin = plugin;
        loadConfig();
        // Register this class as event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean isPlayerInGame(Player player) {
        return playersInGame.contains(player);
    }

    public boolean isPlayerInLobby(Player player) {
        return playersInLobby.contains(player);
    }

    public void joinGame(Player player) {
        if (playersInGame.contains(player) || playersInLobby.contains(player)) {
            player.sendMessage(ChatColor.RED + "You are already in the game!");
            return;
        }

        if (gameActive) {
            // Game is running, put player in lobby
            playersInLobby.add(player);
            player.teleport(lobbyLocation != null ? lobbyLocation : player.getWorld().getSpawnLocation());
            player.setGameMode(GameMode.ADVENTURE);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.getInventory().clear();
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            player.sendMessage(ChatColor.YELLOW + "Game is already running! You are now in the lobby waiting for the next round.");
        } else {
            // No game running, add to lobby
            playersInLobby.add(player);
            player.teleport(lobbyLocation != null ? lobbyLocation : player.getWorld().getSpawnLocation());
            player.setGameMode(GameMode.ADVENTURE);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.getInventory().clear();
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            player.sendMessage(ChatColor.GREEN + "You joined the SwitchPvP lobby!");

            // Check if we can start countdown
            if (playersInLobby.size() >= 2 && !lobbyCountdownActive) {
                startLobbyCountdown();
            }
        }
    }

    public void leaveGame(Player player) {
        boolean wasInGame = playersInGame.contains(player);
        boolean wasInLobby = playersInLobby.contains(player);

        if (!wasInGame && !wasInLobby) {
            player.sendMessage(ChatColor.RED + "You are not in the game!");
            return;
        }

        playersInGame.remove(player);
        playersInLobby.remove(player);
        playersToRespawn.remove(player.getUniqueId());
        player.teleport(player.getWorld().getSpawnLocation());
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        killStreaks.remove(player);

        player.sendMessage(ChatColor.GREEN + "You left the SwitchPvP game!");

        // Check if we need to stop countdown or end game
        if (playersInLobby.size() < 2 && lobbyCountdownActive) {
            stopLobbyCountdown();
        }

        if (playersInGame.size() < 2 && gameActive) {
            endGame();
        }
    }

    private void startLobbyCountdown() {
        lobbyCountdownActive = true;

        countdownTask = new BukkitRunnable() {
            int countdown = lobbyCountdownTime; // Use config value instead of hardcoded 5

            @Override
            public void run() {
                if (playersInLobby.size() < 2) {
                    stopLobbyCountdown();
                    return;
                }

                if (countdown > 0) {
                    for (Player player : playersInLobby) {
                        player.sendTitle(ChatColor.GOLD + "Game starting in",
                                ChatColor.YELLOW + String.valueOf(countdown),
                                0, 20, 0);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    }
                    countdown--;
                } else {
                    // Start the game
                    startGame();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void stopLobbyCountdown() {
        lobbyCountdownActive = false;
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        for (Player player : playersInLobby) {
            player.sendMessage(ChatColor.RED + "Game start cancelled - not enough players!");
        }
    }

    public void setLobbyCountdown(int seconds) {
        this.lobbyCountdownTime = seconds;
        plugin.getConfig().set("lobby-countdown", seconds);
        plugin.saveConfig();
    }

    public int getLobbyCountdown() {
        return lobbyCountdownTime;
    }
    // Prevent PvP in lobby
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        // Cancel damage if either player is in lobby
        if (playersInLobby.contains(victim) || playersInLobby.contains(attacker)) {
            event.setCancelled(true);
            attacker.sendMessage(ChatColor.RED + "PvP is disabled in the lobby!");
        }
    }

    public void setLobby(Location location) {
        this.lobbyLocation = location;
        plugin.getConfig().set("lobby", location);
        plugin.saveConfig();
    }

    public void addArenaSpawn(Location location) {
        this.arenaSpawns.add(location);
        plugin.getConfig().set("arena.spawns", arenaSpawns);
        plugin.saveConfig();
    }

    public boolean removeNearestArenaSpawn(Location location) {
        if (arenaSpawns.isEmpty()) return false;

        Location nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Location spawn : arenaSpawns) {
            double distance = spawn.distanceSquared(location);
            if (distance < nearestDistance) {
                nearest = spawn;
                nearestDistance = distance;
            }
        }

        if (nearest != null && nearestDistance <= 100) { // 10 blocks max distance
            arenaSpawns.remove(nearest);
            plugin.getConfig().set("arena.spawns", arenaSpawns);
            plugin.saveConfig();
            return true;
        }
        return false;
    }

    public List<Location> getArenaSpawns() {
        return new ArrayList<>(arenaSpawns);
    }

    public void clearArenaSpawns() {
        this.arenaSpawns.clear();
        plugin.getConfig().set("arena.spawns", new ArrayList<>());
        plugin.saveConfig();
    }

    public void createCoreBoard(Location location) {
        // Remove existing leaderboard
        clearLeaderboard();

        // Create main leaderboard title (higher up)
        coreBoard = location.getWorld().spawn(location.clone().add(0, 1.5, 0), ArmorStand.class);
        coreBoard.setVisible(false);
        coreBoard.setGravity(false);
        coreBoard.setCustomNameVisible(true);
        coreBoard.setCustomName(ChatColor.GOLD + "✦ SwitchPvP Leaderboard ✦");
        leaderboardStands.add(coreBoard);

        // Create stands for top 3 players (higher positions)
        for (int i = 1; i <= 3; i++) {
            Location standLocation = location.clone().add(0, 1.5 - (i * 0.4), 0);
            ArmorStand stand = location.getWorld().spawn(standLocation, ArmorStand.class);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName(ChatColor.GRAY + "No player yet");
            leaderboardStands.add(stand);
        }

        // Create the #1 player display (full body)
        Location playerLocation = location.clone().add(0, 0, 0);
        ArmorStand playerStand = location.getWorld().spawn(playerLocation, ArmorStand.class);
        playerStand.setVisible(true);
        playerStand.setGravity(false);
        playerStand.setCustomNameVisible(true);
        playerStand.setCustomName(ChatColor.GOLD + "👑 Champion");
        playerStand.setBasePlate(false);
        playerStand.setArms(true);
        leaderboardStands.add(playerStand); // This will be index 4

        plugin.getConfig().set("coreboard.location", location);
        plugin.saveConfig();
        updateCoreBoard();
    }

    private void clearLeaderboard() {
        for (ArmorStand stand : leaderboardStands) {
            if (stand != null) {
                stand.remove();
            }
        }
        leaderboardStands.clear();
        coreBoard = null;
    }
    public boolean removeCoreBoard(Location playerLocation) {
        if (leaderboardStands.isEmpty()) {
            return false;
        }

        // Check if any leaderboard stand is within 5 blocks of the player
        boolean foundNearby = false;
        for (ArmorStand stand : leaderboardStands) {
            if (stand != null && stand.getLocation().distance(playerLocation) <= 5.0) {
                foundNearby = true;
                break;
            }
        }

        if (!foundNearby) {
            return false;
        }

        // Remove all leaderboard stands
        clearLeaderboard();

        // Remove from config
        plugin.getConfig().set("coreboard.location", null);
        plugin.saveConfig();

        return true;
    }

    public boolean hasCoreBoard() {
        return !leaderboardStands.isEmpty();
    }

    private void startGame() {
        gameActive = true;
        lobbyCountdownActive = false;

        // Move players from lobby to game
        playersInGame.addAll(playersInLobby);
        playersInLobby.clear();

        Bukkit.broadcastMessage(ChatColor.GOLD + "SwitchPvP game has started with " + playersInGame.size() + " players!");

        for (Player player : playersInGame) {
            plugin.getKitManager().giveRandomKit(player);
            if (!arenaSpawns.isEmpty()) {
                player.teleport(arenaSpawns.get(new Random().nextInt(arenaSpawns.size())));
            }
            player.sendTitle(ChatColor.GREEN + "FIGHT!", ChatColor.YELLOW + "Good luck!", 10, 40, 10);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
        }
    }

    private void endGame() {
        gameActive = false;
        Bukkit.broadcastMessage(ChatColor.GOLD + "SwitchPvP game has ended!");

        Player topPlayer = getTopPlayer();
        if (topPlayer != null) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "🏆 Winner: " + topPlayer.getName() +
                    " with " + killStreaks.get(topPlayer) + " kills!");
        }

        // Move all players (including those who are dead/respawning) to lobby
        playersInLobby.addAll(playersInGame);

        // Handle players who are currently dead/respawning
        for (UUID deadPlayerUUID : playersToRespawn) {
            Player deadPlayer = Bukkit.getPlayer(deadPlayerUUID);
            if (deadPlayer != null && deadPlayer.isOnline()) {
                // Add them to lobby so they'll be handled correctly on respawn
                playersInLobby.add(deadPlayer);
            }
        }

        playersInGame.clear();

        // Teleport all alive players to lobby immediately
        for (Player player : playersInLobby) {
            if (player.isOnline() && !player.isDead()) {
                if (lobbyLocation != null) {
                    player.teleport(lobbyLocation);
                }
                player.setGameMode(GameMode.ADVENTURE);
                player.getInventory().clear();
                player.setHealth(20);
                player.setFoodLevel(20);
                player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
                player.sendMessage(ChatColor.YELLOW + "Game ended! You are now in the lobby.");
            }
        }

        killStreaks.clear();
        // Don't clear playersToRespawn here - let the respawn handler deal with them
        // Don't clear persistent stats - they should remain
        updateCoreBoard();

        // Check if we can start a new game
        if (playersInLobby.size() >= 2) {
            startLobbyCountdown();
        }
    }

    public void handlePlayerDeath(Player victim, Player killer) {
        if (!gameActive || !playersInGame.contains(victim)) return;

        // Prevent item drops
        victim.getInventory().clear();

        // Mark player for custom respawn
        playersToRespawn.add(victim.getUniqueId());

        // Handle killer rewards
        if (killer != null && playersInGame.contains(killer)) {
            int streak = killStreaks.getOrDefault(killer, 0) + 1;
            killStreaks.put(killer, streak);

            // Update persistent stats
            String killerUUID = killer.getUniqueId().toString();
            int totalKills = persistentStats.getOrDefault(killerUUID, 0) + 1;
            persistentStats.put(killerUUID, totalKills);

            killer.sendTitle(ChatColor.GREEN + "Kill Streak: " + streak,
                    ChatColor.YELLOW + "+" + victim.getName(), 10, 40, 10);
            plugin.getKitManager().giveRandomKit(killer);
            killer.setHealth(20);
            killer.setFoodLevel(20);
            killer.getActivePotionEffects().forEach(effect -> killer.removePotionEffect(effect.getType()));
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            // Save stats and update leaderboard after kill
            saveStats();
            updateCoreBoard();
        }
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Check if player is part of our game system (either in lobby, game, or was marked for respawn)
        boolean wasMarkedForRespawn = playersToRespawn.contains(player.getUniqueId());
        boolean isInGameSystem = playersInGame.contains(player) || playersInLobby.contains(player);

        if (wasMarkedForRespawn || isInGameSystem) {
            // Remove from respawn set if they were in it
            playersToRespawn.remove(player.getUniqueId());

            // If game is not active or player is not in active game, send to lobby
            if (!gameActive || !playersInGame.contains(player)) {
                // Game ended or player not in active game, send them to lobby
                if (lobbyLocation != null) {
                    event.setRespawnLocation(lobbyLocation);
                } else {
                    event.setRespawnLocation(player.getWorld().getSpawnLocation());
                }

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        // Add player to lobby if they're not already there
                        if (!playersInLobby.contains(player)) {
                            playersInLobby.add(player);
                        }
                        // Remove from game if still there
                        playersInGame.remove(player);

                        player.setGameMode(GameMode.ADVENTURE);
                        player.getInventory().clear();
                        player.setHealth(20);
                        player.setFoodLevel(20);
                        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
                        player.sendTitle(ChatColor.YELLOW + "Game Ended!",
                                ChatColor.GREEN + "You are now in the lobby. Use /spvp leave to exit.", 20, 60, 20);
                        player.sendMessage(ChatColor.YELLOW + "The game ended while you were respawning. You are now in the lobby.");
                        player.sendMessage(ChatColor.GREEN + "Use /spvp leave to return to world spawn.");
                    }
                }, 1L);
                return;
            }

            // Game is still active and player is in game - normal respawn
            Location spawnLoc = getRandomArenaSpawn();
            event.setRespawnLocation(spawnLoc);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && playersInGame.contains(player)) {
                    player.setGameMode(GameMode.ADVENTURE);
                    plugin.getKitManager().giveRandomKit(player);
                    player.setHealth(20);
                    player.setFoodLevel(20);
                    player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
                    player.sendTitle(ChatColor.GREEN + "Respawned!",
                            ChatColor.YELLOW + "Fight back!", 10, 30, 10);
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playersToRespawn.remove(event.getPlayer().getUniqueId());
        if (playersInGame.contains(event.getPlayer()) || playersInLobby.contains(event.getPlayer())) {
            leaveGame(event.getPlayer());
        }
    }

    private Location getRandomArenaSpawn() {
        if (!arenaSpawns.isEmpty()) {
            return arenaSpawns.get(new Random().nextInt(arenaSpawns.size()));
        }
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    private Player getTopPlayer() {
        return killStreaks.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private List<Map.Entry<String, Integer>> getTopPlayersFromStats() {
        return persistentStats.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    private void updateCoreBoard() {
        if (leaderboardStands.isEmpty()) return;

        List<Map.Entry<String, Integer>> topPlayers = getTopPlayersFromStats();

        // Update top 3 positions
        for (int i = 0; i < 3; i++) {
            ArmorStand stand = leaderboardStands.get(i + 1); // +1 because index 0 is the title

            if (i < topPlayers.size()) {
                Map.Entry<String, Integer> entry = topPlayers.get(i);
                String playerUUID = entry.getKey();
                int kills = entry.getValue();

                // Try to get player name
                String playerName = "Unknown";
                try {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
                    if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
                        playerName = offlinePlayer.getName();
                    }
                } catch (Exception e) {
                    playerName = "Unknown";
                }

                String[] medals = {"🥇", "🥈", "🥉"};
                ChatColor[] colors = {ChatColor.GOLD, ChatColor.GRAY, ChatColor.DARK_RED};

                stand.setCustomName(colors[i] + medals[i] + " " + playerName +
                        ChatColor.WHITE + " - " + ChatColor.GREEN + kills + " kills");
                stand.setCustomNameVisible(true);
            } else {
                stand.setCustomName(ChatColor.GRAY + "No player yet");
                stand.setCustomNameVisible(true);
            }
        }

        // Update the champion display (full player body)
        if (leaderboardStands.size() > 4 && !topPlayers.isEmpty()) {
            ArmorStand championStand = leaderboardStands.get(4); // The full body stand
            Map.Entry<String, Integer> champion = topPlayers.get(0);
            String championUUID = champion.getKey();
            int championKills = champion.getValue();

            try {
                OfflinePlayer championPlayer = Bukkit.getOfflinePlayer(UUID.fromString(championUUID));
                String championName = championPlayer.hasPlayedBefore() || championPlayer.isOnline() ?
                        championPlayer.getName() : "Unknown Champion";

                championStand.setCustomName(ChatColor.GOLD + "👑 " + championName +
                        ChatColor.YELLOW + " (" + championKills + " kills)");

                // Set full player appearance
                if (championPlayer.isOnline()) {
                    Player onlineChampion = championPlayer.getPlayer();
                    championStand.setHelmet(getPlayerHead(onlineChampion));
                    championStand.setChestplate(onlineChampion.getInventory().getChestplate());
                    championStand.setLeggings(onlineChampion.getInventory().getLeggings());
                    championStand.setBoots(onlineChampion.getInventory().getBoots());
                    championStand.setItemInHand(onlineChampion.getInventory().getItemInMainHand());
                } else {
                    // Set default golden armor for offline champion
                    championStand.setHelmet(getPlayerHeadFromUUID(championUUID));
                    championStand.setChestplate(new org.bukkit.inventory.ItemStack(Material.GOLDEN_CHESTPLATE));
                    championStand.setLeggings(new org.bukkit.inventory.ItemStack(Material.GOLDEN_LEGGINGS));
                    championStand.setBoots(new org.bukkit.inventory.ItemStack(Material.GOLDEN_BOOTS));
                    championStand.setItemInHand(new org.bukkit.inventory.ItemStack(Material.GOLDEN_SWORD));
                }

                championStand.setVisible(true);
                championStand.setCustomNameVisible(true);
            } catch (Exception e) {
                championStand.setCustomName(ChatColor.GOLD + "👑 Champion");
                championStand.setVisible(false);
            }
        }
    }

    private org.bukkit.inventory.ItemStack getPlayerHead(Player player) {
        org.bukkit.inventory.ItemStack head = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            head.setItemMeta(meta);
        }
        return head;
    }

    private org.bukkit.inventory.ItemStack getPlayerHeadFromUUID(String uuid) {
        try {
            org.bukkit.inventory.ItemStack head = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
            org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
            if (meta != null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                meta.setOwningPlayer(offlinePlayer);
                head.setItemMeta(meta);
            }
            return head;
        } catch (Exception e) {
            return new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
        }
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        config.addDefault("config-version", 1);
        config.addDefault("respawn-cooldown", 5);
        config.addDefault("lobby-countdown", 5); // Add default countdown time

        lobbyLocation = (Location) config.get("lobby");
        arenaSpawns = (List<Location>) config.getList("arena.spawns", new ArrayList<>());
        gameActive = config.getBoolean("game.active", false);
        lobbyCountdownTime = config.getInt("lobby-countdown", 5); // Load countdown time

        // Load persistent stats
        if (config.contains("persistent-stats")) {
            ConfigurationSection statsSection = config.getConfigurationSection("persistent-stats");
            if (statsSection != null) {
                for (String uuid : statsSection.getKeys(false)) {
                    int kills = statsSection.getInt(uuid, 0);
                    persistentStats.put(uuid, kills);
                }
            }
        }
        clearLeaderboard();

        Location coreBoardLoc = (Location) config.get("coreboard.location");
        if (coreBoardLoc != null) {
            createCoreBoard(coreBoardLoc);
        }


        // Load current game kill streaks (for game recovery)
        if (config.contains("stats.killStreaks")) {
            ConfigurationSection killStreaksSection = config.getConfigurationSection("stats.killStreaks");
            if (killStreaksSection != null) {
                for (String uuid : killStreaksSection.getKeys(false)) {
                    int kills = killStreaksSection.getInt(uuid, 0);
                    Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                    if (player != null) {
                        killStreaks.put(player, kills);
                    }
                }
            }
        }

    }

    public void saveGameData() {
        plugin.getConfig().set("game.active", gameActive);

        Map<String, Integer> serializedStats = new HashMap<>();
        killStreaks.forEach((player, kills) ->
                serializedStats.put(player.getUniqueId().toString(), kills));
        plugin.getConfig().set("stats.killStreaks", serializedStats);

        plugin.saveConfig();
    }

    private void saveStats() {
        // Save persistent stats to config
        plugin.getConfig().set("persistent-stats", persistentStats);
        plugin.saveConfig();
    }

    // Cleanup method for plugin disable
    public void cleanup() {
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        saveStats(); // Save stats before cleanup
        clearLeaderboard();
    }

    // Method to reset all stats (for admin commands)
    public void resetAllStats() {
        persistentStats.clear();
        killStreaks.clear();
        saveStats();
        updateCoreBoard();
    }

    // Method to get player stats
    public int getPlayerKills(String playerUUID) {
        return persistentStats.getOrDefault(playerUUID, 0);
    }
}