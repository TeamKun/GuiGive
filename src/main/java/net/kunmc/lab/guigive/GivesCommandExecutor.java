package net.kunmc.lab.guigive;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GivesCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;

        String subcmd = args[0];
        switch (subcmd) {
            case "add":
            case "apply":
            case "item":
        }
        return true;
    }
}
