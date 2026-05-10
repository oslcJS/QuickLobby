package com.quickeco;

import com.quickeco.api.QuickEcoAPI;
import com.quickeco.command.BalanceCommand;
import com.quickeco.command.BaltopCommand;
import com.quickeco.command.EcoCommand;
import com.quickeco.command.PayCommand;
import com.quickeco.compat.PapiHook;
import com.quickeco.economy.EconomyProvider;
import com.quickeco.economy.VaultHook;
import com.quickeco.listener.QuickCratesConnection;
import com.quickeco.quicklink.QuickLink;
import com.quickeco.storage.StorageManager;
import com.quickeco.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class QuickEco extends JavaPlugin implements QuickEcoAPI {

    private static QuickEco instance;

    private EconomyProvider economyProvider;
    private StorageManager storageManager;
    private VaultHook vaultHook;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        Msg.init(this);
        this.storageManager = new StorageManager(this);
        this.economyProvider = new EconomyProvider(this);
        storageManager.load();

        registerCommand("quickeco", new EcoCommand(this));
        registerCommand("balance", new BalanceCommand(this));
        registerCommand("pay", new PayCommand(this));
        registerCommand("baltop", new BaltopCommand(this));

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            this.vaultHook = new VaultHook(this);
            try {
                Bukkit.getServicesManager().register(
                        net.milkbowl.vault.economy.Economy.class,
                        this.vaultHook, this, ServicePriority.Normal
                );
                getLogger().info("Hooked into Vault.");
            } catch (Throwable t) {
                getLogger().warning("Failed to register Vault economy: " + t.getMessage());
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PapiHook(this).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("QuickCrates")) {
            getLogger().info("QuickCrates detected — eco rewards from crates enabled.");
        }

        new QuickCratesConnection(this).register();
        QuickLink.register(this);

        if (QuickLink.isLinked("QuickCrates")) {
            getLogger().info("QuickCrates linked via QuickLink — eco rewards from crates enabled.");
        }

        Bukkit.getServicesManager().register(QuickEcoAPI.class, this, this, ServicePriority.Normal);

        getLogger().info("QuickEco enabled. Server: " + Bukkit.getBukkitVersion());
    }

    @Override
    public void onDisable() {
        if (storageManager != null) storageManager.save();
        if (vaultHook != null) vaultHook.unregister();
        QuickLink.unregister();
        getLogger().info("QuickEco disabled.");
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
            if (executor instanceof org.bukkit.command.TabCompleter tc) cmd.setTabCompleter(tc);
        }
    }

    public static QuickEco get() { return instance; }

    @Override
    public EconomyProvider getEconomyProvider() { return economyProvider; }

    public StorageManager getStorageManager() { return storageManager; }
}
