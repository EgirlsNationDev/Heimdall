package me.affanhaq.mapcha.handlers;

import me.affanhaq.mapcha.Mapcha;
import me.affanhaq.mapcha.events.CaptchaFailedEvent;
import me.affanhaq.mapcha.events.CaptchaSuccessEvent;
import me.affanhaq.mapcha.player.CaptchaPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Random;

import static me.affanhaq.mapcha.Mapcha.Config.*;

public class PlayerHandler implements Listener {

    private final Mapcha mapcha;

    public PlayerHandler(Mapcha mapcha) {
        this.mapcha = mapcha;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if(hidePlayers){
            hidePlayers(event.getPlayer());
        }

        if (mapcha.isAuthMeHookActive()) {
            return;
        }

        Player player = event.getPlayer();

        if(player.hasPermission(permission) || (useCompletedCache && mapcha.getCompletedCache().contains(player.getUniqueId()))){
            player.sendMessage(prefix + ChatColor.GREEN + "Do /join to join the server." );
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
        itemMeta.setDisplayName("Capcha");
        itemMeta.setLore(Collections.singletonList("Open the map to see the captcha."));
        itemStack.setItemMeta(itemMeta);

        // giving the player the map and adding them to the captcha array
        captchaPlayer.getPlayer().getInventory().setItemInHand(itemStack);
        mapcha.getPlayerManager().addPlayer(captchaPlayer);

        player.sendMessage(
                prefix + ChatColor.GREEN + "Right click with the map and do /captcha <captcha>" );
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeave(PlayerQuitEvent event) {

        CaptchaPlayer player = mapcha.getPlayerManager().getPlayer(event.getPlayer());

        if (player == null) {
            return;
        }

        // giving the player their items back
        player.resetInventory();
        mapcha.getPlayerManager().removePlayer(player);
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {

        // checking the the player is filling the captcha
        CaptchaPlayer player = mapcha.getPlayerManager().getPlayer(event.getPlayer());

        if (player == null) {
            return;
        }

        // captcha success
        if (event.getMessage().equals(player.getCaptcha())) {
            Bukkit.getScheduler().runTask(mapcha, () -> Bukkit.getPluginManager().callEvent(new CaptchaSuccessEvent(player)));
        } else {
            Bukkit.getScheduler().runTask(mapcha, () -> Bukkit.getPluginManager().callEvent(new CaptchaFailedEvent(player)));
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {

        CaptchaPlayer cPlayer = mapcha.getPlayerManager().getPlayer(event.getPlayer());

        if (cPlayer == null) {
            return;
        }

        if (event.getMessage().equals("/" + cPlayer.getCaptcha())) {
            Bukkit.getScheduler().runTask(mapcha, () -> Bukkit.getPluginManager().callEvent(new CaptchaSuccessEvent(cPlayer)));
        } else if (!validCommand(event.getMessage())) {
            event.setCancelled(true);
        }
        //event.setCancelled(mapcha.getPlayerManager().getPlayer(event.getPlayer()) != null && !validCommand(event.getMessage()));
    }

    /**
     * Checks if the message contains a command.
     *
     * @param message the message to check commands for
     * @return whether the message contains a command or not
     */
    private boolean validCommand(String message) {
        for (String command : commands) {
            if (message.contains(command) || message.contains("/captcha")) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return a random string with len 4
     */
    public static String genCaptcha() {
        String charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < lenght; i++) {
            random.append(charset.charAt(new Random().nextInt(charset.length() - 1)));
        }
        return random.toString();
    }

    public void hidePlayers(Player player){
        for(Player p : Bukkit.getOnlinePlayers()){
            player.hidePlayer(p);
        }
        for(Player p : Bukkit.getOnlinePlayers()){
            p.hidePlayer(player);
        }
    }
}