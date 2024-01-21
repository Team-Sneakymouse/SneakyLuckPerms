package net.sneakymouse.sneakyluckperms.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.Context;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.sneakymouse.sneakyluckperms.SneakyLuckPerms;
import net.sneakymouse.sneakyluckperms.util.ChatUtility;

public class CommandSneakyLuckPerms extends Command {

    private LuckPerms luckPerms;

    public CommandSneakyLuckPerms() {
        super("sneakylp");
        this.usageMessage = "> /" + this.getName() + " user <user>";
        this.description = "Describe your actions in a holographic message on your body.";
        this.setPermission(SneakyLuckPerms.IDENTIFIER + ".command." + this.getName());

        luckPerms = LuckPermsProvider.get();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "user" :
                    return caseUser(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        sender.sendMessage(ChatUtility.convertToComponent("&4Invalid Usage: " + this.usageMessage));
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) {
        switch(args.length) {
            case 1:
                return List.of("user");
            case 2:
                switch (args[0]) {
                    case "user":
                        List<String> playerNames = new ArrayList<>();

                        for (@NotNull OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                            if (player.getName().toLowerCase().startsWith(args[1].toLowerCase()) && !player.getName().equals("CMI-Fake-Operator")) playerNames.add(player.getName());
                        }

                        return playerNames;
                }
            case 3:
                switch (args[0]) {
                    case "user":
                        return List.of("permission");
                }
            case 4:
                switch (args[2]) {
                    case "permission":
                        return List.of("unsettempregex");
                }
        }
        return new ArrayList<>();
    }

    private boolean caseUser(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length > 0) {
            @Nullable User user = null;

            String name = args[0];
            for (@NotNull OfflinePlayer opl : Bukkit.getOfflinePlayers()) {
                if (opl.getName().equalsIgnoreCase(name)); user = luckPerms.getUserManager().getUser(opl.getUniqueId());
            }

            if (user == null) {
                sender.sendMessage(ChatUtility.convertToComponent("&4Unknown player: " + name));
            }

            if (args.length > 1) {
                switch(args[1]) {
                    case "permission" :
                        return caseUserPermission(sender, user, Arrays.copyOfRange(args, 2, args.length));
                }
            }
        }

        return false;
    }

    private boolean caseUserPermission(@NotNull CommandSender sender, User user, @NotNull String[] args) {
        if (args.length > 0) {
            switch(args[0]) {
                case "unsettempregex" :
                    if (args.length > 1) {
                        return caseUserPermissionUnsettempregex(sender, user, args[1], Arrays.copyOfRange(args, 2, args.length));
                    }
            }
        }

        return false;
    }
    
    private boolean caseUserPermissionUnsettempregex(@NotNull CommandSender sender, User user, String regex, @NotNull String[] args) {
        @NonNull NodeMap nodeMap = user.data();
        Pattern pattern = Pattern.compile(regex);

        List<String> contextMessages = new ArrayList<>();
        Map<String, String> contexts = new HashMap<>();

        for (String arg : args) {
            String[] split = arg.split("=");

            if (split.length != 2) {
                sender.sendMessage(ChatUtility.convertToComponent("&4Invalid context: " + arg));
                return false;
            }

            contexts.put(split[0], split[1]);
            contextMessages.add("&3" + split[0] + "=&b" + split[1]);
        }

        String contextMessage;

        if (contextMessages.isEmpty()) {
            contextMessage = "&eglobal";
        } else {
            contextMessage = String.join("&4 ", contextMessages);
        }

        boolean removed = false;

        for (Node node : user.getNodes()) {
            if (node.hasExpiry()) {
                Matcher matcher = pattern.matcher(node.getKey());
                if (matcher.matches()) {
                    Set<Context> nodeContexts = node.getContexts().toSet();

                    if (contexts.size() != nodeContexts.size()) continue;

                    for (Context context : nodeContexts) {
                        if (contexts.get(context.getKey()) == null || !contexts.get(context.getKey()).equals(context.getValue())) continue;
                    }

                    nodeMap.remove(node);
                    removed = true;

                    sender.sendMessage(ChatUtility.convertToComponent("&7[&bSneakyL&3P&7] &aUnset temporary permission &b" + node.getKey() + " &afor &b" + user.getUsername() + " &ain context " + contextMessage + "&a."));
                }
            }
        }

        if (removed) {
            luckPerms.getUserManager().saveUser(user);
        } else {
            sender.sendMessage(ChatUtility.convertToComponent("&7[&bSneakyL&3P&7] &b" + user.getUsername() +" &adoes not have &b" + regex + " &aset temporarily in context " + contextMessage + "&a."));
        }

        return removed;
    }

}