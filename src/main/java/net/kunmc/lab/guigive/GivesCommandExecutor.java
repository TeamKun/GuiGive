package net.kunmc.lab.guigive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
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
                String filename = getBasenamse(args[1]); //DirectoryTraversal対策
                Inventory inv = plugin.inventories.computeIfAbsent(filename, name -> {
                    Inventory tmp = Bukkit.createInventory(((HumanEntity) sender), 36, name);
                    tmp.all(new ItemStack(Material.AIR));
                    return tmp;
                });
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
                if (!plugin.inventories.containsKey(invName)) {
                    sender.sendMessage(Message.Failure(invName + "は存在しません."));
                    return true;
                }

                int cnt = plugin.give(plugin.inventories.get(invName), p);
                sender.sendMessage(Message.Success(p.getName() + "に" + cnt + "個のアイテムを配りました."));
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
                String invName = args[1];
                if (plugin.inventories.containsKey(invName)) {
                    plugin.respawnInventory = plugin.inventories.get(invName);
                    plugin.getConfig().set("respawnInventory", invName);
                    plugin.saveConfig();
                    sender.sendMessage(Message.Success(invName + "はリスポーン時のインベントリに設定されました."));
                } else {
                    sender.sendMessage(Message.Failure(invName + "は存在しません."));
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

    private String getBasenamse(String pathname) {
        return new File(pathname).getName();
    }
}
