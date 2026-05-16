package com.quickeco.command;

import com.quickeco.QuickEco;
import com.quickeco.economy.EconomyProvider;
import com.quickeco.util.Msg;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PayCommand implements CommandExecutor, TabCompleter {

    private final QuickEco plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public PayCommand(QuickEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c,
                             @NotNull String l, @NotNull String[] a) {
        if (!(s instanceof Player sender)) { Msg.send(s, "player-only"); return true; }
        if (!s.hasPermission("qe.pay") && !s.hasPermission("quickeco.use")) {
            Msg.send(s, "no-permission");
            return true;
        }

        if (a.length < 2) {
            Msg.plain(s, "&7Usage: &e/pay <player> <amount>");
            return true;
        }

        OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(a[0]);
        if (target == null || target.getName() == null) { Msg.send(s, "player-not-found"); return true; }
        if (target.getUniqueId().equals(sender.getUniqueId())) { Msg.send(s, "pay-self"); return true; }

        double amount;
        try { amount = Double.parseDouble(a[1]); }
        catch (NumberFormatException e) { Msg.send(s, "invalid-amount"); return true; }

        EconomyProvider eco = plugin.getEconomyProvider();
        double minPay = plugin.getConfig().getDouble("settings.min-pay", 0.01);
        double maxPay = plugin.getConfig().getDouble("settings.max-pay", 10000.0);

        if (amount < minPay) { Msg.send(s, "pay-min", "amount", eco.format(minPay)); return true; }
        if (amount > maxPay) { Msg.send(s, "pay-max", "amount", eco.format(maxPay)); return true; }

        int cooldownSec = plugin.getConfig().getInt("settings.pay-cooldown", 3);
        if (cooldownSec > 0) {
            long last = cooldowns.getOrDefault(sender.getUniqueId(), 0L);
            long now = System.currentTimeMillis();
            if (now - last < cooldownSec * 1000L) { Msg.send(s, "pay-cooldown"); return true; }
            cooldowns.put(sender.getUniqueId(), now);
        }

        if (!eco.hasAccount(sender)) eco.createAccount(sender);
        if (!eco.hasAccount(target)) eco.createAccount(target);

        String feeType = plugin.getConfig().getString("settings.pay-fee-type", "flat");
        double fee = plugin.getConfig().getDouble("settings.pay-fee", 0.0);
        double totalCost = amount + ("percent".equalsIgnoreCase(feeType) ? amount * fee / 100.0 : fee);

        if (!eco.has(sender, totalCost)) { Msg.send(s, "insufficient-funds"); return true; }

        eco.withdrawPlayer(sender, totalCost);
        eco.depositPlayer(target, amount);

        Msg.send(s, "paid", "amount", eco.format(amount), "target", target.getName());
        if (target.isOnline()) {
            Msg.send(target.getPlayer(), "received",
                    "amount", eco.format(amount), "sender", sender.getName());
        }

        if (plugin.getConfig().getBoolean("settings.log-transactions", true)) {
            plugin.getLogger().info(sender.getName() + " paid " + eco.format(amount) + " to " + target.getName());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c,
                                      @NotNull String l, @NotNull String[] a) {
        if (a.length == 1) {
            List<String> out = new ArrayList<>();
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (!p.equals(s)) out.add(p.getName());
            }
            return out;
        }
        return List.of();
    }
}
