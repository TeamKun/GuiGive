package net.kunmc.lab.guigive;

import org.bukkit.ChatColor;

public class Message {
    public static String Success(String msg) {
        return ChatColor.GREEN + msg;
    }

    public static String Failure(String msg) {
        return ChatColor.RED + msg;
    }
}
