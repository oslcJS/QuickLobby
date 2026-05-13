package com.quickeco.command;

import com.quickeco.QuickEco;
import com.quickeco.economy.EconomyProvider;
import com.quickeco.util.Msg;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PayCommand implements CommandExecutor, TabCompleter {

    private final QuickEco plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public PayCommand(QuickEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (!(s instanceof Player sender)) {
            s.sendMessage(Msg.color("&cPlayer only."));
            return true;
        }

        if (!s.hasPermission("quickeco.pay")) { Msg.send(s, "no-permission"); return true; }

        if (a.length < 2) {
            s.sendMessage(Msg.color("&cUsage: /pay <player> <amount>"));
            return true;
        }

        OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(a[0]);
        if (target == null || target.getName() == null) { Msg.send(s, "player-not-found"); return true; }

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            s.sendMessage(Msg.color("&cYou can't pay yourself."));
            return true;
        }

        double amount;
        try { amount = Double.parseDouble(a[1]); } catch (Exception e) { Msg.send(s, "invalid-amount"); return true; }

        double minPay = plugin.getConfig().getDouble("settings.min-pay", 0.01);
        double maxPay = plugin.getConfig().getDouble("settings.max-pay", 10000.0);

        if (amount < minPay) { s.sendMessage(Msg.color("&cMinimum pay amount is " + minPay)); return true; }
        if (amount > maxPay) { s.sendMessage(Msg.color("&cMaximum pay amount is " + maxPay)); return true; }

        int cooldownSec = plugin.getConfig().getInt("settings.pay-cooldown", 3);
        if (cooldownSec > 0) {
            long last = cooldowns.getOrDefault(sender.getUniqueId(), 0L);
            long now = System.currentTimeMillis();
            if (now - last < cooldownSec * 1000L) {
                Msg.send(s, "pay-cooldown");
                return true;
            }
            cooldowns.put(sender.getUniqueId(), now);
        }

        EconomyProvider eco = plugin.getEconomyProvider();

        if (!eco.hasAccount(sender)) eco.createAccount(sender);
        if (!eco.hasAccount(target)) eco.createAccount(target);

        String feeType = plugin.getConfig().getString("settings.pay-fee-type", "flat");
        double fee = plugin.getConfig().getDouble("settings.pay-fee", 0.0);
        double totalCost = amount + ("percent".equalsIgnoreCase(feeType) ? amount * fee / 100.0 : fee);

        if (!eco.has(sender, totalCost)) { Msg.send(s, "insufficient-funds"); return true; }

        eco.withdrawPlayer(sender, totalCost);
        eco.depositPlayer(target, amount);

        EconomyProvider finalEco = eco;
        Msg.send(s, "paid", "amount", eco.format(amount), "target", target.getName());
        if (target.isOnline()) {
            Msg.send((CommandSender) target.getPlayer(), "received",
                    "amount", finalEco.format(amount), "sender", sender.getName());
        }

        if (plugin.getConfig().getBoolean("settings.log-transactions", true))
            plugin.getLogger().info(sender.getName() + " paid " + eco.format(amount) + " to " + target.getName());

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (a.length == 1) {
            List<String> out = new ArrayList<>();
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (!p.equals(s)) out.add(p.getName());
            }
            return out;
        }
        return Collections.emptyList();
    }
}
