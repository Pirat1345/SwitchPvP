package me.Pirat1345.switchPvP;

import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class GameListener implements Listener {
    private final SwitchPvP plugin;

    public GameListener(SwitchPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Prevent item drops
        event.getDrops().clear();
        event.setKeepInventory(true);

        // Clear inventory (in case keepInventory doesn't work)
        victim.getInventory().clear();

        plugin.getGameManager().handlePlayerDeath(victim, killer);
        event.setDeathMessage(null); // Clear default death message
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getGameManager().isPlayerInGame(player)) {
            // Handle rejoining players (if needed)
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getGameManager().isPlayerInGame(player)) {
            plugin.getGameManager().leaveGame(player);
        }
    }
}