package com.quickeco.listener;

import com.quickeco.QuickEco;
import com.quickeco.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;

/**
 * Placeholder connection to QuickCrates.
 * Will be expanded when QuickCrates API is finalized.
 *I GOTTA EXPAND THIS 1000%
 * This class detects when QuickCrates is available and listens for
 * crate reward events (planned) to reward money from crate openings.
 */
public class QuickCratesConnection implements Listener {

    private final QuickEco plugin;

    public QuickCratesConnection(QuickEco plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        if (Bukkit.getPluginManager().isPluginEnabled("QuickCrates")) {
            plugin.getLogger().info("QuickCrates connection established.");
        }
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        // Placeholder: will hook into QuickCratesAPI when crate rewards are given
    }

    @EventHandler
    public void onServiceUnregister(ServiceUnregisterEvent event) {
        // Placeholder: cleanup when QuickCrates unloads
    }
}
