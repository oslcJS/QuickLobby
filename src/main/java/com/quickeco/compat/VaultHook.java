package com.quickeco.compat;

import com.quickeco.QuickEco;
import com.quickeco.economy.EconomyProvider;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

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
            plugin.getServer().getServicesManager().unregister(Economy.class, this);
        } catch (Throwable ignored) {}
    }

    @Override public boolean isEnabled() { return plugin.isEnabled(); }
    @Override public String getName() { return "QuickEco"; }
    @Override public boolean hasBankSupport() { return false; }
    @Override public int fractionalDigits() {
        return plugin.getConfig().getInt("settings.fractional-digits", 2);
    }

    @Override public String format(double amount) { return provider.format(amount); }
    @Override public String currencyNamePlural() {
        return plugin.getConfig().getString("settings.currency-plural", "coins");
    }
    @Override public String currencyNameSingular() {
        return plugin.getConfig().getString("settings.currency-singular", "coin");
    }

    @Override public boolean hasAccount(OfflinePlayer player) { return provider.hasAccount(player); }
    @Override public boolean hasAccount(OfflinePlayer player, String worldName) { return hasAccount(player); }

    @Override public boolean createPlayerAccount(OfflinePlayer player) {
        if (provider.hasAccount(player)) return false;
        provider.createAccount(player);
        return true;
    }
    @Override public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    @Override public double getBalance(OfflinePlayer player) { return provider.getBalance(player); }
    @Override public double getBalance(OfflinePlayer player, String world) { return getBalance(player); }
    @Override public boolean has(OfflinePlayer player, double amount) { return provider.has(player, amount); }
    @Override public boolean has(OfflinePlayer player, String worldName, double amount) { return has(player, amount); }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return fail("Cannot withdraw negative amounts");
        if (!provider.has(player, amount)) return fail("Insufficient funds");
        provider.withdrawPlayer(player, amount);
        return ok(amount, provider.getBalance(player));
    }
    @Override public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return fail("Cannot deposit negative amounts");
        if (!provider.hasAccount(player)) provider.createAccount(player);
        provider.depositPlayer(player, amount);
        return ok(amount, provider.getBalance(player));
    }
    @Override public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    private EconomyResponse ok(double amt, double bal) {
        return new EconomyResponse(amt, bal, EconomyResponse.ResponseType.SUCCESS, null);
    }
    private EconomyResponse fail(String why) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, why);
    }
    private EconomyResponse notImpl() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "QuickEco does not support banks");
    }

    @Override @Deprecated public EconomyResponse createBank(String name, String playerName) { return notImpl(); }
    @Override public EconomyResponse createBank(String name, OfflinePlayer player) { return notImpl(); }
    @Override public EconomyResponse deleteBank(String name) { return notImpl(); }
    @Override public EconomyResponse bankHas(String name, double amount) { return notImpl(); }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return notImpl(); }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return notImpl(); }
    @Override @Deprecated public EconomyResponse isBankOwner(String name, String playerName) { return notImpl(); }
    @Override public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return notImpl(); }
    @Override @Deprecated public EconomyResponse isBankMember(String name, String playerName) { return notImpl(); }
    @Override public EconomyResponse isBankMember(String name, OfflinePlayer player) { return notImpl(); }
    @Override public EconomyResponse bankBalance(String name) { return notImpl(); }
    @Override public List<String> getBanks() { return List.of(); }

    @Override @Deprecated public boolean hasAccount(String playerName) { return false; }
    @Override @Deprecated public boolean hasAccount(String playerName, String worldName) { return false; }
    @Override @Deprecated public double getBalance(String playerName) { return 0; }
    @Override @Deprecated public double getBalance(String playerName, String world) { return 0; }
    @Override @Deprecated public boolean has(String playerName, double amount) { return false; }
    @Override @Deprecated public boolean has(String playerName, String worldName, double amount) { return false; }
    @Override @Deprecated public EconomyResponse withdrawPlayer(String playerName, double amount) { return fail("Use OfflinePlayer overload"); }
    @Override @Deprecated public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) { return withdrawPlayer(playerName, amount); }
    @Override @Deprecated public EconomyResponse depositPlayer(String playerName, double amount) { return fail("Use OfflinePlayer overload"); }
    @Override @Deprecated public EconomyResponse depositPlayer(String playerName, String worldName, double amount) { return depositPlayer(playerName, amount); }
    @Override @Deprecated public boolean createPlayerAccount(String playerName) { return false; }
    @Override @Deprecated public boolean createPlayerAccount(String playerName, String worldName) { return false; }
}
