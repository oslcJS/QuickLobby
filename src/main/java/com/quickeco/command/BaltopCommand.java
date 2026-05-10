package com.quickeco.command;

import com.quickeco.QuickEco;
import com.quickeco.economy.EconomyProvider;
import com.quickeco.util.Msg;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BaltopCommand implements CommandExecutor {

    private final QuickEco plugin;

    public BaltopCommand(QuickEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (!s.hasPermission("quickeco.baltop")) { Msg.send(s, "no-permission"); return true; }

        int count = 10;
        if (a.length >= 1) {
            try { count = Math.max(1, Math.min(100, Integer.parseInt(a[0]))); }
            catch (Exception ignored) {}
        }

        EconomyProvider eco = plugin.getEconomyProvider();
        List<Map.Entry<UUID, Double>> top = eco.getTopBalances(count);

        if (top.isEmpty()) {
            s.sendMessage(Msg.color("&cNo balances found."));
            return true;
        }

        s.sendMessage(Msg.color(plugin.getConfig().getString("messages.baltop-header",
                "&6&lBalance Top &7(Top {count})").replace("{count}", String.valueOf(top.size()))));

        int rank = 1;
        for (Map.Entry<UUID, Double> entry : top) {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(entry.getKey());
            String name = player.getName() != null ? player.getName() : "Unknown";
            String formatted = eco.format(entry.getValue());
            String line = plugin.getConfig().getString("messages.baltop-entry",
                    "&e#{rank} &7{player} &f- &e{balance}")
                    .replace("{rank}", String.valueOf(rank))
                    .replace("{player}", name)
                    .replace("{balance}", formatted);
            s.sendMessage(Msg.color(line));
            rank++;
        }

        return true;
    }
}
