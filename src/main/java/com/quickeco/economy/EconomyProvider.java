package com.quickeco.economy;

import com.quickeco.QuickEco;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyProvider {

    private final QuickEco plugin;
    private final Map<UUID, Double> balances = new ConcurrentHashMap<>();

    public EconomyProvider(QuickEco plugin) {
        this.plugin = plugin;
    }

    public boolean hasAccount(OfflinePlayer player) {
        return balances.containsKey(player.getUniqueId());
    }

    public void createAccount(OfflinePlayer player) {
        balances.putIfAbsent(player.getUniqueId(), startingBalanceFor(player));
    }

    private double startingBalanceFor(OfflinePlayer player) {
        if (!plugin.getConfig().getBoolean("starting-balance-by-group.enabled", false)) {
            return getStartingBalance();
        }
        if (!(player instanceof Player p)) return getStartingBalance();
        ConfigurationSection sec = plugin.getConfig()
                .getConfigurationSection("starting-balance-by-group.groups");
        if (sec == null) return getStartingBalance();
        for (String group : sec.getKeys(false)) {
            if (p.hasPermission("quickeco.group." + group)) {
                return sec.getDouble(group, getStartingBalance());
            }
        }
        return getStartingBalance();
    }

    public void removeAccount(OfflinePlayer player) {
        balances.remove(player.getUniqueId());
    }

    public double getBalance(OfflinePlayer player) {
        return balances.getOrDefault(player.getUniqueId(), 0.0);
    }

    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    public boolean withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return false;
        double current = getBalance(player);
        if (current < amount) return false;
        balances.put(player.getUniqueId(), current - amount);
        return true;
    }

    public boolean depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return false;
        balances.merge(player.getUniqueId(), amount, Double::sum);
        return true;
    }

    public boolean setBalance(OfflinePlayer player, double amount) {
        if (amount < 0) return false;
        balances.put(player.getUniqueId(), amount);
        return true;
    }

    public boolean transfer(OfflinePlayer from, OfflinePlayer to, double amount) {
        if (amount < 0) return false;
        if (!has(from, amount)) return false;
        if (!hasAccount(to)) createAccount(to);
        withdrawPlayer(from, amount);
        depositPlayer(to, amount);
        return true;
    }

    public Map<UUID, Double> getBalances() {
        return Collections.unmodifiableMap(balances);
    }

    public List<Map.Entry<UUID, Double>> getTopBalances(int limit) {
        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(balances.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    public void applyToAll(java.util.function.BiConsumer<UUID, Double> consumer) {
        balances.forEach(consumer);
    }

    public void setBalances(Map<UUID, Double> data) {
        balances.clear();
        balances.putAll(data);
    }

    public double getStartingBalance() {
        return plugin.getConfig().getDouble("settings.starting-balance", 100.0);
    }

    public String format(double amount) {
        boolean before = plugin.getConfig().getBoolean("settings.symbol-before", true);
        String symbol = plugin.getConfig().getString("settings.currency-symbol", "$");
        int digits = plugin.getConfig().getInt("settings.fractional-digits", 2);
        String formatted = String.format("%." + digits + "f", amount);
        return before ? symbol + formatted : formatted + symbol;
    }
}
