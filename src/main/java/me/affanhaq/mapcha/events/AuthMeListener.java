package me.affanhaq.mapcha.events;

import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.api.v3.AuthMePlayer;
import fr.xephi.authme.events.LoginEvent;
import me.affanhaq.mapcha.Mapcha;
import me.affanhaq.mapcha.hooks.AuthMeHook;
import me.affanhaq.mapcha.player.CaptchaPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Optional;

import static me.affanhaq.mapcha.Mapcha.Config.*;
import static me.affanhaq.mapcha.handlers.PlayerHandler.genCaptcha;

public class AuthMeListener implements Listener {

    private final Mapcha mapcha;

    public AuthMeListener(Mapcha mapcha) {
        this.mapcha = mapcha;
    }

    private final AuthMeApi authmeApi = AuthMeHook.getAuthmeApi();

    @EventHandler
    public void onLogin(LoginEvent event){
        //AuthMe event. Gets triggered when player logs in successfully.
        if(!mapcha.isAuthMeHookActive()){
            return;
        }

        Player player = event.getPlayer();

        if(player.hasPermission(permission) || (useCompletedCache && mapcha.getCompletedCache().contains(player.getUniqueId()))){
            player.sendMessage(prefix + ChatColor.GREEN + "Do /join to join the server." );
            Optional<AuthMePlayer> playerInfo = authmeApi.getPlayerInfo(player.getName());
            if(playerInfo.isPresent()){
                AuthMePlayer authMePlayer = playerInfo.get();
                if(!authMePlayer.getEmail().isPresent()){
                    player.sendMessage(prefix + ChatColor.RED + "You don't have email linked to your account. This will make it difficult to recover your password if you loose it.\n" +
                            "It is recommended to add one using /email add <email> <emailAgain>");
                }
            }
            return;
        }

        // creating a captcha player
        CaptchaPlayer captchaPlayer = new CaptchaPlayer(player, genCaptcha(), mapcha);

        // making a map for the player
        String version = Bukkit.getVersion();
        ItemStack itemStack;
        if (version.contains("1.13") || version.contains("1.14") || version.contains("1.15") || version.contains("1.16")) {
            itemStack = new ItemStack(Material.valueOf("LEGACY_EMPTY_MAP"));
        } else {
            itemStack = new ItemStack(Material.valueOf("EMPTY_MAP"));
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("Capcha");
        itemMeta.setLore(Collections.singletonList("Open the map to see the captcha."));
        itemStack.setItemMeta(itemMeta);

        // giving the player the map and adding them to the captcha array
        captchaPlayer.getPlayer().getInventory().setItemInHand(itemStack);
        mapcha.getPlayerManager().addPlayer(captchaPlayer);

        //Sending message to player with instructions because they are pepegas
        player.sendMessage(
                prefix + ChatColor.GREEN + "Right click with the map and do /captcha <captcha>" );
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEnable(PluginEnableEvent e){
        if("AuthMe".equals(e.getPlugin().getName())){
            mapcha.registerAuthMeComponents();
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPluginDisable(PluginDisableEvent e){
        if("AuthMe".equals(e.getPlugin().getName())){
            mapcha.removeAuthMeHook();
        }
    }
}
