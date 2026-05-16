package com.quickeco;

import com.quickeco.api.QuickEcoAPI;
import com.quickeco.command.BalanceCommand;
import com.quickeco.command.BaltopCommand;
import com.quickeco.command.PayCommand;
import com.quickeco.command.QuickEcoCommand;
import com.quickeco.compat.PapiHook;
import com.quickeco.compat.VaultHook;
import com.quickeco.economy.EconomyProvider;
import com.quickeco.listener.GuiListener;
import com.quickeco.quicklink.QuickLink;
import com.quickeco.storage.StorageManager;
import com.quickeco.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class QuickEco extends JavaPlugin implements QuickEcoAPI {

    private static QuickEco instance;

    private EconomyProvider economyProvider;
    private StorageManager storageManager;
    private VaultHook vaultHook;
    private int autoSaveTask = -1;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Msg.init(this);

        this.storageManager = new StorageManager(this);
        this.economyProvider = new EconomyProvider(this);
        storageManager.load();

        bind("quickeco", new QuickEcoCommand(this));
        bind("balance", new BalanceCommand(this));
        bind("pay", new PayCommand(this));
        bind("baltop", new BaltopCommand(this));

        Bukkit.getPluginManager().registerEvents(new GuiListener(), this);

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            this.vaultHook = new VaultHook(this);
            try {
                Bukkit.getServicesManager().register(
                        net.milkbowl.vault.economy.Economy.class,
                        this.vaultHook, this, ServicePriority.Normal);
                getLogger().info("Hooked into Vault.");
            } catch (Throwable t) {
                getLogger().warning("Failed to register Vault economy: " + t.getMessage());
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PapiHook(this).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }

        QuickLink.register(this);

        Bukkit.getServicesManager().register(QuickEcoAPI.class, this, this, ServicePriority.Normal);

        scheduleAutoSave();

        getLogger().info("QuickEco " + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (autoSaveTask != -1) Bukkit.getScheduler().cancelTask(autoSaveTask);
        if (storageManager != null) storageManager.save();
        if (vaultHook != null) vaultHook.unregister();
        QuickLink.unregister();
        getLogger().info("QuickEco disabled.");
    }

    private void scheduleAutoSave() {
        int seconds = getConfig().getInt("settings.auto-save-interval", 300);
        if (seconds <= 0) return;
        long ticks = seconds * 20L;
        autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                () -> storageManager.save(), ticks, ticks).getTaskId();
    }

    private void bind(String name, CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().warning("Command not registered in plugin.yml: " + name);
            return;
        }
        cmd.setExecutor(executor);
        if (executor instanceof TabCompleter tc) cmd.setTabCompleter(tc);
    }

    public static QuickEco get() { return instance; }

    @Override
    public EconomyProvider getEconomyProvider() { return economyProvider; }

    public StorageManager getStorageManager() { return storageManager; }
}
