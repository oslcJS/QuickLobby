package com.quickeco.compat;

import com.quickeco.QuickEco;
import com.quickeco.economy.EconomyProvider;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PapiHook extends PlaceholderExpansion {

    private final QuickEco plugin;

    public PapiHook(QuickEco p) { this.plugin = p; }

    @Override public @NotNull String getIdentifier() { return "quickeco"; }
    @Override public @NotNull String getAuthor() { return "QuickPlugins"; }
    @Override public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer p, @NotNull String params) {
        EconomyProvider eco = plugin.getEconomyProvider();
        String q = params.toLowerCase();

        if (q.equals("balance")) {
            return p == null ? "0" : String.valueOf(eco.getBalance(p));
        }
        if (q.equals("balance_formatted") || q.equals("balance_fmt")) {
            return p == null ? eco.format(0) : eco.format(eco.getBalance(p));
        }
        if (q.equals("currency")) {
            return plugin.getConfig().getString("settings.currency-plural", "coins");
        }
        if (q.equals("currency_singular")) {
            return plugin.getConfig().getString("settings.currency-singular", "coin");
        }
        if (q.equals("currency_symbol")) {
            return plugin.getConfig().getString("settings.currency-symbol", "$");
        }
        if (q.equals("rank")) {
            if (p == null) return "0";
            int idx = rankOf(eco, p.getUniqueId());
            return idx < 0 ? "0" : String.valueOf(idx + 1);
        }

        if (q.startsWith("balance_")) {
            String tail = params.substring("balance_".length());
            OfflinePlayer target = resolve(tail);
            if (target == null) return "0";
            return String.valueOf(eco.getBalance(target));
        }

        if (q.startsWith("balance_formatted_") || q.startsWith("balance_fmt_")) {
            String tail = params.substring(params.indexOf('_', "balance_".length()) + 1);
            OfflinePlayer target = resolve(tail);
            if (target == null) return eco.format(0);
            return eco.format(eco.getBalance(target));
        }

        if (q.startsWith("top_")) {
            String[] parts = q.split("_");
            if (parts.length < 3) return "";
            int n;
            try { n = Integer.parseInt(parts[1]); }
            catch (NumberFormatException e) { return ""; }
            if (n < 1) return "";
            List<Map.Entry<UUID, Double>> top = eco.getTopBalances(n);
            if (top.size() < n) return "";
            Map.Entry<UUID, Double> entry = top.get(n - 1);
            String field = parts[2];
            switch (field) {
                case "name" -> {
                    OfflinePlayer pl = plugin.getServer().getOfflinePlayer(entry.getKey());
                    return pl.getName() == null ? "Unknown" : pl.getName();
                }
                case "balance" -> { return String.valueOf(entry.getValue()); }
                case "formatted", "fmt" -> { return eco.format(entry.getValue()); }
            }
        }

        if (q.startsWith("has_")) {
            String tail = params.substring("has_".length());
            try {
                double amt = Double.parseDouble(tail);
                if (p == null) return "false";
                return eco.has(p, amt) ? "true" : "false";
            } catch (NumberFormatException e) { return "false"; }
        }

        return null;
    }

    private OfflinePlayer resolve(String idOrName) {
        try {
            UUID uuid = UUID.fromString(idOrName);
            return plugin.getServer().getOfflinePlayer(uuid);
        } catch (IllegalArgumentException ignored) {}
        return plugin.getServer().getOfflinePlayerIfCached(idOrName);
    }

    private int rankOf(EconomyProvider eco, UUID uuid) {
        List<Map.Entry<UUID, Double>> sorted = eco.getTopBalances(Integer.MAX_VALUE);
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(uuid)) return i;
        }
        return -1;
    }
}
