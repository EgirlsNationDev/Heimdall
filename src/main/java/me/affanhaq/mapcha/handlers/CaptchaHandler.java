package me.affanhaq.mapcha.handlers;

import me.affanhaq.mapcha.Mapcha;
import me.affanhaq.mapcha.events.CaptchaFailedEvent;
import me.affanhaq.mapcha.events.CaptchaSuccessEvent;
import me.affanhaq.mapcha.player.CaptchaPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static me.affanhaq.mapcha.Mapcha.Config.*;

public class CaptchaHandler implements Listener {

    private final Mapcha mapcha;

    public CaptchaHandler(Mapcha mapcha) {
        this.mapcha = mapcha;
    }

    @EventHandler
    public void onCaptchaSuccess(CaptchaSuccessEvent event) {
        CaptchaPlayer player = event.getPlayer();

        player.getPlayer().sendMessage(prefix + " " + successMessage);

        // adding the player to set so when he logs back in he won't have to complete the captcha again
        if (useCompletedCache) {
            mapcha.getCompletedCache().add(player.getPlayer().getUniqueId());
        }

        if(removePlayerData){
            player.getPlayer().getInventory().clear();
            player.getPlayer().getInventory().setArmorContents(null);
            player.getPlayer().updateInventory();
        }

        mapcha.getPlayerManager().removePlayer(player);
        String msg = ChatColor.GOLD + "[" + ChatColor.RED + "Captcha" + ChatColor.GOLD + "] " + ChatColor.GREEN + "Now do /join to join the main server";
        player.getPlayer().sendMessage(msg);
    }

    @EventHandler
    public void onCaptchaFail(CaptchaFailedEvent event) {
        CaptchaPlayer player = event.getPlayer();

        // kicking the player because he's out of tries
        if (player.getTries() >= (tries - 1)) {
            Bukkit.getScheduler().runTask(mapcha, () -> player.getPlayer().kickPlayer(prefix + " " + failMessage));
        } else { // telling the player to try again
            player.setTries(player.getTries() + 1);
            player.getPlayer().sendMessage(
                    prefix + " " + retryMessage.replace("{CURRENT}", String.valueOf(player.getTries())).replace("{MAX}", String.valueOf(tries))
            );
            player.getPlayer().sendMessage(prefix + " " + ChatColor.RED + "Make sure you're not using fancy chat or anything that alters the way you chat");
        }
    }

}
