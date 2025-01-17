package com.wayvi.wfly.wflyV2.commands;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.impl.SimpleCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import util.ConfigUtil;

public class ReloadCommand extends SimpleCommand {

    ConfigUtil configUtil;


    public ReloadCommand(JavaPlugin plugin, String name) {
        super(plugin, "wfly");
        setDescription("Reload file of the plugin.");
        setUsage("/wfly reload");
        addArgs("reload:string");
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {


        configUtil.save();
        configUtil.reload();
        commandSender.sendMessage("Plugin has been reloaded!");

    }
}
