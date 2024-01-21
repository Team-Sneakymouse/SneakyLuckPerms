package net.sneakymouse.sneakyluckperms.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CommandSneakyLuckPerms extends Command {

    private LuckPerms luckPerms;

    public CommandSneakyLuckPerms() {
        super("sneakylp");
        this.usageMessage = "/" + this.getName() + " [user]";
        this.description = "Describe your actions in a holographic message on your body.";

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

        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid Usage: " + this.usageMessage));
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
                            if (player.getName().toLowerCase().startsWith(args[0].toLowerCase()) && !player.getName().equals("CMI-Fake-Operator")) playerNames.add(player.getName());
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
                sender.sendMessage("<red>Unknown player: " + name);
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

        boolean removed = false;

        for (Node node : user.getNodes()) {
            if (node.hasExpiry()) {
                Matcher matcher = pattern.matcher(node.getKey());
                if (matcher.matches()) {
                    nodeMap.remove(node);
                    removed = true;
                    sender.sendMessage("<green>The temporary permission node '" + node.getKey() + "' has been unset from user '" + user.getUsername() + "'");
                }
            }
        }

        if (removed) {
            luckPerms.getUserManager().saveUser(user);
        } else {
            sender.sendMessage("<red>The user " + user.getUsername() + " did not have any temporary permission nodes the matched the regex pattern '" + regex + "'");
        }

        return true;
    }

}