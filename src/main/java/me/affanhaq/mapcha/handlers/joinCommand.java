package me.affanhaq.mapcha.handlers;

import fr.xephi.authme.api.v3.AuthMeApi;
import me.affanhaq.mapcha.Mapcha;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class joinCommand implements CommandExecutor {

    private final Mapcha mapcha;

    public joinCommand(Mapcha mapcha){ this.mapcha = mapcha; }

    AuthMeApi authmeApi = AuthMeApi.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

        if(!(sender instanceof Player)){
            sender.sendMessage("You must be a player to use this command.");
        }else{
            Player player = (Player) sender;
            if(authmeApi.isAuthenticated(player)){
                player.sendMessage(ChatColor.GREEN+"Connecting to the main server...");
                Bukkit.getScheduler().scheduleSyncDelayedTask(mapcha, () -> Mapcha.sendPlayerToServer(mapcha, player), 15);
            }else{
                player.sendMessage(ChatColor.RED + "You need to authenticate first!");
            }
        }
        return true;
    }
}
