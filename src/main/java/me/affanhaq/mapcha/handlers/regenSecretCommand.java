package me.affanhaq.mapcha.handlers;

import me.affanhaq.mapcha.Mapcha;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static me.affanhaq.mapcha.Mapcha.Config.secretEnabled;

public class regenSecretCommand implements CommandExecutor {

    private final Mapcha plugin;

    public regenSecretCommand(Mapcha plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof ConsoleCommandSender)){
            commandSender.sendMessage(ChatColor.RED + "This command is console only!");
            return true;
        }

        if(!secretEnabled){
            commandSender.sendMessage("This feature was disabled. You can enable it in the config.");
            return true;
        }
        try {
            plugin.regenSecretAndPost();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
