package me.Pirat1345.switchPvP;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KitManager {
    private final SwitchPvP plugin;
    private final File kitsFolder;
    private final Random random = new Random();

    public KitManager(SwitchPvP plugin) {
        this.plugin = plugin;
        this.kitsFolder = new File(plugin.getDataFolder(), "kits");
        if (!kitsFolder.exists()) {
            kitsFolder.mkdirs();
        }
    }

    public void createKit(Player player, String name) {
        PlayerInventory inventory = player.getInventory();
        File kitFile = new File(kitsFolder, name + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(kitFile);

        // Save armor
        config.set("armor", inventory.getArmorContents());

        // Save inventory
        config.set("inventory", inventory.getContents());

        try {
            config.save(kitFile);
            player.sendMessage("§aKit '" + name + "' successfully created!");
        } catch (IOException e) {
            player.sendMessage("§cError saving kit: " + e.getMessage());
        }
    }

    public void deleteKit(Player player, String name) {
        File kitFile = new File(kitsFolder, name + ".yml");
        if (kitFile.exists() && kitFile.delete()) {
            player.sendMessage("§aKit '" + name + "' successfully deleted!");
        } else {
            player.sendMessage("§cKit '" + name + "' not found or couldn't be deleted!");
        }
    }

    public void listKits(Player player) {
        File[] kitFiles = kitsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (kitFiles == null || kitFiles.length == 0) {
            player.sendMessage("§cNo kits available!");
            return;
        }

        player.sendMessage("§6Available Kits:");
        for (File file : kitFiles) {
            String kitName = file.getName().replace(".yml", "");
            player.sendMessage("§7- " + kitName);
        }
    }

    public void giveRandomKit(Player player) {
        File[] kitFiles = kitsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (kitFiles == null || kitFiles.length == 0) {
            player.sendMessage("§cNo kits available!");
            return;
        }

        File randomKit = kitFiles[random.nextInt(kitFiles.length)];
        FileConfiguration config = YamlConfiguration.loadConfiguration(randomKit);

        PlayerInventory inventory = player.getInventory();
        inventory.setArmorContents(((List<ItemStack>) config.get("armor")).toArray(new ItemStack[0]));
        inventory.setContents(((List<ItemStack>) config.get("inventory")).toArray(new ItemStack[0]));

        player.sendMessage("§aYou received the kit: " + randomKit.getName().replace(".yml", ""));
    }

    public List<String> getKitNames() {
        List<String> names = new ArrayList<>();
        File[] kitFiles = kitsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (kitFiles != null) {
            for (File file : kitFiles) {
                names.add(file.getName().replace(".yml", ""));
            }
        }
        return names;
    }

    public void saveKits() {
        // Kits are saved individually when created
        // No additional saving needed here
    }
}