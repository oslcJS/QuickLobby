package com.quickeco.economy;

import com.quickeco.QuickEco;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;

public class VaultHook implements Economy {

    private final QuickEco plugin;
    private final EconomyProvider provider;

    public VaultHook(QuickEco plugin) {
        this.plugin = plugin;
        this.provider = plugin.getEconomyProvider();
    }

    public void unregister() {
        try {
            plugin.getServer().getServicesManager().unregister(
                    net.milkbowl.vault.economy.Economy.class, this
            );
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean isEnabled() { return plugin.isEnabled(); }

    @Override
    public String getName() { return "QuickEco"; }

    @Override
    public boolean hasBankSupport() { return false; }

    @Override
    public int fractionalDigits() {
        return plugin.getConfig().getInt("settings.fractional-digits", 2);
    }

    @Override
    public String format(double amount) {
        boolean before = plugin.getConfig().getBoolean("settings.symbol-before", true);
        String symbol = plugin.getConfig().getString("settings.currency-symbol", "$");
        int digits = fractionalDigits();
        String formatted = String.format("%." + digits + "f", amount);
        return before ? symbol + formatted : formatted + symbol;
    }

    @Override
    public String currencyNamePlural() {
        return plugin.getConfig().getString("settings.currency-plural", "coins");
    }

    @Override
    public String currencyNameSingular() {
        return plugin.getConfig().getString("settings.currency-singular", "coin");
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return provider.hasAccount(player);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (provider.hasAccount(player)) return false;
        provider.createAccount(player);
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return provider.getBalance(player);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return provider.has(player, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amounts");
        if (!provider.has(player, amount))
            return new EconomyResponse(0, provider.getBalance(player), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        provider.withdrawPlayer(player, amount);
        return new EconomyResponse(amount, provider.getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amounts");
        provider.depositPlayer(player, amount);
        return new EconomyResponse(amount, provider.getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override @Deprecated
    public EconomyResponse createBank(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "QuickEco does not support bank accounts via Vault");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "QuickEco does not support bank accounts via Vault");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override @Deprecated
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override @Deprecated
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override @Deprecated
    public boolean hasAccount(String playerName) { return false; }

    @Override @Deprecated
    public boolean hasAccount(String playerName, String worldName) { return false; }

    @Override @Deprecated
    public double getBalance(String playerName) { return 0; }

    @Override @Deprecated
    public double getBalance(String playerName, String world) { return 0; }

    @Override @Deprecated
    public boolean has(String playerName, double amount) { return false; }

    @Override @Deprecated
    public boolean has(String playerName, String worldName, double amount) { return false; }

    @Override @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Use OfflinePlayer overload");
    }

    @Override @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override @Deprecated
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Use OfflinePlayer overload");
    }

    @Override @Deprecated
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override @Deprecated
    public boolean createPlayerAccount(String playerName) { return false; }

    @Override @Deprecated
    public boolean createPlayerAccount(String playerName, String worldName) { return false; }
}
