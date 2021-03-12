package net.kunmc.lab.guigive;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GivesCommandExecutor implements CommandExecutor, TabCompleter {
    GuiGive plugin;
    List<String> subCmdList = Arrays.asList("add", "apply", "item", "default-item");

    GivesCommandExecutor(GuiGive plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) return false;

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "add": {
                if (!(sender instanceof HumanEntity)) {
                    sender.sendMessage(Message.Failure("このコマンドはプレイヤーから実行してください."));
                    return true;
                }
                Inventory inv = plugin.inventories.computeIfAbsent(args[1], name -> Bukkit.createInventory(null, InventoryType.PLAYER, name));
                ((HumanEntity) sender).openInventory(inv);
                return true;
            }
            case "apply": {
                if (args.length < 3) {
                    sender.sendMessage(Message.Failure("/gives apply <name> <player>"));
                    return true;
                }
                String invName = args[1];
                Player p = Bukkit.getPlayer(args[2]);
                if (p == null) {
                    sender.sendMessage(Message.Failure(args[2] + "は存在しません."));
                    return true;
                }
                plugin.give(plugin.inventories.get(invName), p);
                return true;
            }
            case "item": {
                if (!(sender instanceof HumanEntity)) {
                    sender.sendMessage(Message.Failure("このコマンドはプレイヤーから実行してください."));
                    return true;
                }

                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) {
                    sender.sendMessage(Message.Failure(args[1] + "は存在しません."));
                    return true;
                }
                Inventory inv = Bukkit.createInventory(p, InventoryType.PLAYER, "閉じるとGiveします");
                ((HumanEntity) sender).openInventory(inv);
                plugin.temporaryInventories.add(inv);
                return true;
            }
            case "default-item":
                if (plugin.inventories.containsKey(args[1])) {
                    plugin.respawnInventory = plugin.inventories.get(args[1]);
                    sender.sendMessage(Message.Success(args[1] + "はリスポーン時のインベントリに設定されました."));
                } else {
                    sender.sendMessage(Message.Failure(args[1] + "は存在しません."));
                }
                return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return subCmdList.stream()
                    .filter(x -> x.startsWith(args[0]))
                    .collect(Collectors.toList());

        String subCmd = args[0].toLowerCase();
        if (args.length == 2) {
            switch (subCmd) {
                case "add":
                case "apply":
                case "default-item":
                    return new ArrayList<>(plugin.inventories.keySet()).stream()
                            .filter(x -> x.startsWith(args[1]))
                            .collect(Collectors.toList());
                case "item":
                    return Bukkit.getOnlinePlayers().stream()
                            .map(HumanEntity::getName)
                            .filter(x -> x.startsWith(args[1]))
                            .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && subCmd.equals("apply")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(HumanEntity::getName)
                    .filter(x -> x.startsWith(args[2]))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
