package me.affanhaq.mapcha.handlers;

import fr.xephi.authme.api.v3.AuthMeApi;
import me.affanhaq.mapcha.Mapcha;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.affanhaq.mapcha.Mapcha.Config.testServerName;

public class joinCommand implements CommandExecutor {

    private final Mapcha mapcha;

    public joinCommand(Mapcha mapcha){ this.mapcha = mapcha; }

    AuthMeApi authmeApi = AuthMeApi.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args){

        if(!(sender instanceof Player)){
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }else{
            Player player = (Player) sender;
            if(args.length == 0){
                if(authmeApi.isAuthenticated(player)){
                    player.sendMessage(ChatColor.GREEN+"Connecting you to the server...");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(mapcha, () -> Mapcha.sendPlayerToMain(mapcha, player), 15);
                }else{
                    player.sendMessage(ChatColor.RED + "You need to authenticate first!");
                }
                return true;
            }

            String server = args[0];
            if(server == null){
                sender.sendMessage("Error occured while processing the command");
                return true;
            }
            if(server.equalsIgnoreCase("main") || server.equalsIgnoreCase("anarchy")){
                player.sendMessage(ChatColor.GREEN+"Connecting to the main server...");
                Bukkit.getScheduler().scheduleSyncDelayedTask(mapcha, () -> Mapcha.sendPlayerToMain(mapcha, player), 15);
            }
            if(server.equalsIgnoreCase("test") || server.equalsIgnoreCase("temp")){
                if(testServerName.isEmpty()){
                    player.sendMessage(ChatColor.RED + "The test server is disabled. Try next time.");
                    return false;
                }
                player.sendMessage(ChatColor.GREEN+"Connecting to the test server...");
                Bukkit.getScheduler().scheduleSyncDelayedTask(mapcha, () -> Mapcha.sendPlayerToTest(mapcha, player), 15);
            }
        }
        return true;
    }
}
