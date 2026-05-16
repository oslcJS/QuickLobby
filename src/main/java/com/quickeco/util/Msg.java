package com.quickeco.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Msg {
    private static JavaPlugin plugin;

    private Msg() {}

    public static void init(JavaPlugin p) { plugin = p; }

    public static String color(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String raw(String key) {
        return plugin.getConfig().getString("messages." + key, key);
    }

    public static String prefix() {
        return color(raw("prefix"));
    }

    public static void send(CommandSender to, String key, Object... repl) {
        String msg = raw(key);
        for (int i = 0; i + 1 < repl.length; i += 2) {
            msg = msg.replace("{" + repl[i] + "}", String.valueOf(repl[i + 1]));
        }
        to.sendMessage(prefix() + color(msg));
    }

    public static void plain(CommandSender to, String text) {
        to.sendMessage(prefix() + color(text));
    }

    public static boolean debugEnabled() {
        return plugin != null && plugin.getConfig().getBoolean("settings.debug", false);
    }

    public static void debug(CommandSender to, String key, Object... repl) {
        if (!debugEnabled()) return;
        send(to, key, repl);
    }
}
