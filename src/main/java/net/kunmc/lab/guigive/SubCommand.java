package net.kunmc.lab.guigive;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface SubCommand {
    void execute(CommandSender sender, Command command, String[] args);
}
