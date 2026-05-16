package com.quickeco.compat;

import com.quickeco.QuickEco;
import com.quickeco.economy.EconomyProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ImportManager {

    public enum Source { ESSENTIALS, CMI }

    private final QuickEco plugin;

    public ImportManager(QuickEco plugin) { this.plugin = plugin; }

    public Result run(Source source) {
        backup();
        return switch (source) {
            case ESSENTIALS -> importEssentials();
            case CMI -> importCmi();
        };
    }

    private void backup() {
        File data = new File(plugin.getDataFolder(), "balances.yml");
        if (!data.exists()) return;
        File backup = new File(plugin.getDataFolder(),
                "balances.yml.bak." + System.currentTimeMillis());
        try {
            Files.copy(data.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().warning("Backup before import failed: " + e.getMessage());
        }
    }

    private Result importEssentials() {
        File dir = new File("plugins/Essentials/userdata");
        if (!dir.isDirectory()) return Result.missing("plugins/Essentials/userdata");

        EconomyProvider eco = plugin.getEconomyProvider();
        int imported = 0;
        int skipped = 0;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return Result.ok(0, 0);

        for (File f : files) {
            try {
                UUID uuid = UUID.fromString(f.getName().replace(".yml", ""));
                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
                Object rawMoney = cfg.get("money");
                if (rawMoney == null) { skipped++; continue; }
                double balance = parseDouble(rawMoney);
                OfflinePlayer p = plugin.getServer().getOfflinePlayer(uuid);
                eco.setBalance(p, balance);
                imported++;
            } catch (Exception e) {
                skipped++;
            }
        }

        plugin.getStorageManager().save();
        return Result.ok(imported, skipped);
    }

    private Result importCmi() {
        File[] candidates = new File[]{
                new File("plugins/CMI/userdata"),
                new File("plugins/CMI/data/userdata"),
                new File("plugins/CMI/players")
        };
        File dir = null;
        for (File c : candidates) if (c.isDirectory()) { dir = c; break; }
        if (dir == null) return Result.missing("plugins/CMI/userdata (or data/userdata, or players)");

        EconomyProvider eco = plugin.getEconomyProvider();
        int imported = 0;
        int skipped = 0;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return Result.ok(0, 0);

        for (File f : files) {
            try {
                UUID uuid = UUID.fromString(f.getName().replace(".yml", ""));
                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
                Object rawMoney = firstNonNull(cfg, "Balance", "money", "Money", "balance");
                if (rawMoney == null) { skipped++; continue; }
                double balance = parseDouble(rawMoney);
                OfflinePlayer p = plugin.getServer().getOfflinePlayer(uuid);
                eco.setBalance(p, balance);
                imported++;
            } catch (Exception e) {
                skipped++;
            }
        }

        plugin.getStorageManager().save();
        return Result.ok(imported, skipped);
    }

    private Object firstNonNull(YamlConfiguration cfg, String... keys) {
        for (String k : keys) {
            Object v = cfg.get(k);
            if (v != null) return v;
        }
        return null;
    }

    private double parseDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); }
        catch (NumberFormatException e) { return 0.0; }
    }

    public static final class Result {
        public final boolean ok;
        public final String missingPath;
        public final int imported;
        public final int skipped;

        private Result(boolean ok, String missingPath, int imported, int skipped) {
            this.ok = ok;
            this.missingPath = missingPath;
            this.imported = imported;
            this.skipped = skipped;
        }

        public static Result ok(int imported, int skipped) {
            return new Result(true, null, imported, skipped);
        }
        public static Result missing(String path) {
            return new Result(false, path, 0, 0);
        }
    }
}
