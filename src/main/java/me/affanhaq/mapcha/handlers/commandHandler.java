package me.affanhaq.mapcha.handlers;

import me.affanhaq.mapcha.Mapcha;
import me.affanhaq.mapcha.events.CaptchaFailedEvent;
import me.affanhaq.mapcha.events.CaptchaSuccessEvent;
import me.affanhaq.mapcha.player.CaptchaPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public class commandHandler implements CommandExecutor {

    private Mapcha mapcha;

    public commandHandler(Mapcha mapcha){
        this.mapcha = mapcha;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(!(sender instanceof Player)){
            sender.sendMessage("This command is player only!");
            return true;
        }

        if(args.length == 0){
            sender.sendMessage(ChatColor.RED + "You need to fill in the captcha. Look at the map and do the command again like this /ecaptcha <code>");
            Bukkit.getLogger().info(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames().toString());
        }else{
            Player player = (Player) sender;
            Bukkit.getLogger().info(player.getDisplayName());
            CaptchaPlayer cPlayer = mapcha.getPlayerManager().getPlayer(player);

            if (cPlayer == null) {
                player.sendMessage("Error occured");
                return true;
            }

            // captcha success
            if (args[0].equals(cPlayer.getCaptcha())) {
                Bukkit.getScheduler().runTask(mapcha, () -> Bukkit.getPluginManager().callEvent(new CaptchaSuccessEvent(cPlayer)));
            } else {
                Bukkit.getScheduler().runTask(mapcha, () -> Bukkit.getPluginManager().callEvent(new CaptchaFailedEvent(cPlayer)));
            }
        }
        return true;
    }
}
