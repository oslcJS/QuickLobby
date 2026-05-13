package com.quickeco.listener;

import com.quickeco.QuickEco;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;

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
    }

    @EventHandler
    public void onServiceUnregister(ServiceUnregisterEvent event) {
    }
}
