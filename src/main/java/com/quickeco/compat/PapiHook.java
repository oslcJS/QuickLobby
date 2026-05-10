package com.quickeco.compat;

import com.quickeco.QuickEco;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PapiHook extends PlaceholderExpansion {

    private final QuickEco plugin;

    public PapiHook(QuickEco p) { this.plugin = p; }

    @Override
    public @NotNull String getIdentifier() { return "quickeco"; }

    @Override
    public @NotNull String getAuthor() { return "QuickPlugins"; }

    @Override
    public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer p, @NotNull String params) {
        if (p == null) return "";

        if (params.equalsIgnoreCase("balance")) {
            return String.valueOf(plugin.getEconomyProvider().getBalance(p));
        }

        if (params.equalsIgnoreCase("balance_formatted")) {
            double bal = plugin.getEconomyProvider().getBalance(p);
            return format(bal);
        }

        if (params.startsWith("balance_")) {
            String uuidStr = params.substring("balance_".length());
            try {
                UUID uuid = UUID.fromString(uuidStr);
                OfflinePlayer target = plugin.getServer().getOfflinePlayer(uuid);
                return String.valueOf(plugin.getEconomyProvider().getBalance(target));
            } catch (Exception e) {
                return "0";
            }
        }

        if (params.equalsIgnoreCase("currency")) {
            return plugin.getConfig().getString("settings.currency-plural", "coins");
        }

        if (params.equalsIgnoreCase("currency_singular")) {
            return plugin.getConfig().getString("settings.currency-singular", "coin");
        }

        return null;
    }

    private String format(double amount) {
        boolean before = plugin.getConfig().getBoolean("settings.symbol-before", true);
        String symbol = plugin.getConfig().getString("settings.currency-symbol", "$");
        int digits = plugin.getConfig().getInt("settings.fractional-digits", 2);
        String formatted = String.format("%." + digits + "f", amount);
        return before ? symbol + formatted : formatted + symbol;
    }
}
