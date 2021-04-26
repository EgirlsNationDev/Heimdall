package me.affanhaq.mapcha.events;

import fr.xephi.authme.events.LoginEvent;
import me.affanhaq.mapcha.Mapcha;
import me.affanhaq.mapcha.handlers.PlayerHandler;
import me.affanhaq.mapcha.player.CaptchaPlayer;
import org.bukkit.Bukkit;
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

import static me.affanhaq.mapcha.Mapcha.Config.permission;
import static me.affanhaq.mapcha.Mapcha.Config.useCompletedCache;
import static me.affanhaq.mapcha.handlers.PlayerHandler.genCaptcha;

public class AuthMeListener implements Listener {

    private final Mapcha mapcha;

    public AuthMeListener(Mapcha mapcha) {
        this.mapcha = mapcha;
    }

    @EventHandler
    public void onLogin(LoginEvent event){
        //AuthMe event. Gets triggered when player logs in successfully.
        if(!mapcha.isAuthMeHookActive()){
            return;
        }

        Player player = event.getPlayer();

        // checking if player has permission to bypass the captcha or player has already completed the captcha before
        // by default OPs have the '*' permission so this method will return true
        if (player.hasPermission(permission) || (useCompletedCache && mapcha.getCompletedCache().contains(player.getUniqueId()))) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(mapcha, () -> Mapcha.sendPlayerToServer(mapcha, player), 15);
            return;
        }

        // creating a captcha player
        CaptchaPlayer captchaPlayer = new CaptchaPlayer(player, genCaptcha(), mapcha)
                .cleanPlayer();

        // making a map for the player
        String version = Bukkit.getVersion();
        ItemStack itemStack;
        if (version.contains("1.13") || version.contains("1.14") || version.contains("1.15") || version.contains("1.16")) {
            itemStack = new ItemStack(Material.valueOf("LEGACY_EMPTY_MAP"));
        } else {
            itemStack = new ItemStack(Material.valueOf("EMPTY_MAP"));
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("Mapcha");
        itemMeta.setLore(Collections.singletonList("Open the map to see the captcha."));
        itemStack.setItemMeta(itemMeta);

        // giving the player the map and adding them to the captcha array
        captchaPlayer.getPlayer().getInventory().setItemInHand(itemStack);
        mapcha.getPlayerManager().addPlayer(captchaPlayer);
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
