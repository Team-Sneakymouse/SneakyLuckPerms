package net.sneakymouse.sneakyluckperms;

import org.bukkit.plugin.java.JavaPlugin;

import net.sneakymouse.sneakyluckperms.commands.*;

public class SneakyLuckPerms extends JavaPlugin {

    private static final String IDENTIFIER = "sneakyluckperms";
    
    @Override
    public void onEnable() {
        getServer().getCommandMap().register(IDENTIFIER, new CommandSneakyLuckPerms());
    }

}
