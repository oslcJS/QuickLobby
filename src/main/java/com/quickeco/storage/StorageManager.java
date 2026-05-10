package com.quickeco.storage;

import com.quickeco.QuickEco;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageManager {

    private final QuickEco plugin;
    private final File file;

    public StorageManager(QuickEco plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "balances.yml");
    }

    public void load() {
        if (!file.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        Map<UUID, Double> data = new HashMap<>();
        for (String key : cfg.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double balance = cfg.getDouble(key, 0.0);
                data.put(uuid, balance);
            } catch (Exception ignored) {}
        }
        plugin.getEconomyProvider().setBalances(data);
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        plugin.getEconomyProvider().applyToAll((uuid, bal) -> cfg.set(uuid.toString(), bal));
        try {
            cfg.save(file);
        } catch (Exception ex) {
            plugin.getLogger().warning("Failed to save balances: " + ex.getMessage());
        }
    }
}
