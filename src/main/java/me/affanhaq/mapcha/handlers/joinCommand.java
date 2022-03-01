package me.affanhaq.mapcha.handlers;

import fr.xephi.authme.api.v3.AuthMeApi;
import me.affanhaq.mapcha.Mapcha;
import me.affanhaq.mapcha.hooks.AuthMeHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.affanhaq.mapcha.Mapcha.Config.*;

public class joinCommand implements CommandExecutor {

    private final Mapcha mapcha;

    public joinCommand(Mapcha mapcha){ this.mapcha = mapcha; }

    private final AuthMeApi authmeApi = AuthMeHook.getAuthmeApi();
    public static final List<UUID> didCommandList = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args){

        if(!(sender instanceof Player)){
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }else{
            Player player = (Player) sender;
            if(!authmeApi.isAuthenticated(player)){
                player.sendMessage(prefix + " " + ChatColor.RED + "You must be authenticated to do this command!");
            }

            if(args.length == 0){
                sendToMain(player);
            }

            String server = null;
            if(args.length == 1){
                server = args[0];
            }
            if(server == null){
                return true;
            }
            if(server.equalsIgnoreCase("main") || server.equalsIgnoreCase("anarchy")){
                sendToMain(player);
            }else if(server.equalsIgnoreCase("test") || server.equalsIgnoreCase("temp")){
                sendToTest(player);
            }else{
                sender.sendMessage(prefix + " " + ChatColor.RED + "This server doesn't exist. Only servers are main and test");
            }
        }
        return true;
    }

    private void sendToMain(Player player){
        if(maintenanceMode){
            if(!didCommandList.contains(player.getUniqueId())){
                player.sendMessage(maintenanceMsg);
                didCommandList.add(player.getUniqueId());
            }else{
                sendToTest(player);
            }
        }else{
            player.sendMessage(prefix + " " + ChatColor.GREEN + "Connecting you to the main server...");
            Bukkit.getScheduler().scheduleSyncDelayedTask(mapcha, () -> Mapcha.sendPlayerToMain(mapcha, player), 20);
        }
    }

    private void sendToTest(Player player){
        if(!testServerEnabled){
            player.sendMessage(prefix + " " + ChatColor.RED + "The test server is currently disabled. Try next time.");
            return;
        }
        player.sendMessage(prefix + " " + ChatColor.GREEN +"Connecting to the test server...");
        Bukkit.getScheduler().scheduleSyncDelayedTask(mapcha, () -> Mapcha.sendPlayerToTest(mapcha, player), 15);
    }
}
