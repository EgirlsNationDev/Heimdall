package me.affanhaq.mapcha.handlers;

import me.affanhaq.mapcha.Mapcha;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static me.affanhaq.mapcha.Mapcha.Config.prefix;

public class discordCommand implements CommandExecutor {
    private final Mapcha mapcha;

    public discordCommand(Mapcha mapcha){ this.mapcha = mapcha; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args){
        sender.sendMessage(prefix + ChatColor.AQUA + "You can check our latest news and announcements on our Discord server: " + ChatColor.GOLD + ChatColor.UNDERLINE +  "https://egirlsnation.com/announcements");
        return true;
    }
}
