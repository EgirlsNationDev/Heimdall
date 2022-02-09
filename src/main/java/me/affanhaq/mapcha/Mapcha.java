package me.affanhaq.mapcha;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.xephi.authme.api.v3.AuthMeApi;
import me.affanhaq.keeper.Keeper;
import me.affanhaq.keeper.data.ConfigFile;
import me.affanhaq.keeper.data.ConfigValue;
import me.affanhaq.mapcha.events.AuthMeListener;
import me.affanhaq.mapcha.handlers.*;
import me.affanhaq.mapcha.hooks.AuthMeHook;
import me.affanhaq.mapcha.player.CaptchaPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static me.affanhaq.mapcha.Mapcha.Config.*;
import static org.bukkit.ChatColor.*;

public class Mapcha extends JavaPlugin {

    private final CaptchaPlayerManager playerManager = new CaptchaPlayerManager();
    private final Set<UUID> completedCache = new HashSet<>();

    private Listener authMeListener;
    private AuthMeHook authMeHook;
    private String secret;

    private final AuthMeApi authmeApi = AuthMeApi.getInstance();

    @Override
    public void onEnable() {
        new Keeper(this)
                .register(new Config())
                .load();

        prefix = prefix + " ";
        if(lenght > 8){
            lenght = 8;
        }
        if(lenght < 2){
            lenght = 2;
        }
        if(secretLenght > 248){
            secretLenght = 248;
        }
        if(secretLenght < 16){
            secretLenght = 16;
        }

        authMeHook = new AuthMeHook();
        PluginManager pluginManager = Bukkit.getPluginManager();

        if(pluginManager.isPluginEnabled("AuthMe")){
            registerAuthMeComponents();
        }


        // registering events
        pluginManager.registerEvents(new PlayerHandler(this), this);
        pluginManager.registerEvents(new MapHandler(this), this);
        pluginManager.registerEvents(new CaptchaHandler(this), this);

        this.getCommand("captcha").setExecutor(new commandHandler(this));
        this.getCommand("join").setExecutor(new joinCommand(this));
        this.getCommand("regenSecret").setExecutor(new regenSecretCommand(this));

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        if (secretEnabled) {
            Bukkit.getScheduler().runTaskTimer(this, () -> {

                genSecret();

                if(webhookName.isEmpty()){
                    return;
                }
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    try {
                        postSecret();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }, 20, getTicksFromHours(refreshTime));
        }
    }

    @Override
    public void onDisable(){
        Bukkit.getLogger().info("Shutting down Mapcha");
    }

    public CaptchaPlayerManager getPlayerManager() {
        return playerManager;
    }

    public Set<UUID> getCompletedCache() {
        return completedCache;
    }

    /**
     * Sends a player to a connected server after the captcha is completed.
     *
     * @param player the player to send
     */
    public static void sendPlayerToMain(JavaPlugin javaPlugin, Player player) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(mainServerName);
            player.sendPluginMessage(javaPlugin, "BungeeCord", out.toByteArray());
            Bukkit.getLogger().info(prefix + ChatColor.GREEN + "Connecting " + player.getName() + " to anarchy");
    }

    public static void sendPlayerToTest(JavaPlugin javaPlugin, Player player) {
        if(testServerName.isEmpty()){
            Bukkit.getLogger().info(prefix + RED + "Test server isn't configured. The player won't be sent there!");
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(testServerName);
        player.sendPluginMessage(javaPlugin, "BungeeCord", out.toByteArray());
        Bukkit.getLogger().info(prefix + ChatColor.GREEN + "Connecting " + player.getName() + " to temp");
    }

    public void registerAuthMeComponents(){
        Bukkit.getLogger().info(prefix + ChatColor.GREEN + "Hooking into authme.");
        authMeHook.initializeHook();
        if(authMeListener == null){
            authMeListener = new AuthMeListener(this);
            getServer().getPluginManager().registerEvents(authMeListener, this);
        }
    }

    private void genSecret(){
        String charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String charsetSpec = "*-+@$#&%!.";
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < secretLenght; i++) {
            if(new Random().nextBoolean()){
                random.append(charsetSpec.charAt(new Random().nextInt(charsetSpec.length() - 1)));
            }else{
                random.append(charset.charAt(new Random().nextInt(charset.length() - 1)));
            }

        }
        secret = random.toString();
        Bukkit.getConsoleSender().sendMessage(prefix + GREEN + "New secret command is: /" + secret);
    }

    private void postSecret() throws IOException {
        DiscordWebhook webhook = new DiscordWebhook(webhookURL);
        if(!webhookName.isEmpty()) {
            webhook.setUsername(webhookName);
        } else{
            webhook.setUsername(webhookName);
        }
        if(!avatarURL.isEmpty()){
            webhook.setAvatarUrl(avatarURL);
        }

        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                .setTitle(":lock: New secret command")
                .setDescription("Do `/" + secret + "` to bypass the captcha")
                .setColor(Color.GREEN);
        webhook.addEmbed(embed);
        webhook.execute();
    }

    public void regenSecretAndPost() throws IOException {
        genSecret();
        if(webhookName.isEmpty()){
            return;
        }
        postSecret();
    }

    public boolean isValidSecretCommand(Player player, String message){
        if(message.equals("/" + secret) && authmeApi.isAuthenticated(player)){
            return true;
        }
        return false;
    }


    public void removeAuthMeHook(){
        authMeHook.removeAuthMeHook();
    }

    public boolean isAuthMeHookActive(){
        return authMeHook.isAuthMeHookActive();
    }

    public int getTicksFromHours(int hours){
        int ticks = hours * 20;
        ticks = ticks * 60;
        ticks = ticks * 60;
        return ticks;
    }

    @ConfigFile("config.yml")
    public static class Config {
        public static String permission = "heimdall.bypass";

        @ConfigValue("dont-save-playerdata")
        public static boolean removePlayerData = false;

        @ConfigValue("prefix")
        public static String prefix = GOLD + "[" + GREEN + "Heimdall" + GOLD + "]"+RESET;

        @ConfigValue("commands")
        public static List<String> commands = Arrays.asList("/register", "/login", "/2fa", "/recover");

        @ConfigValue("captcha.lenght")
        public static int lenght = 4;

        @ConfigValue("captcha.cache")
        public static boolean useCompletedCache = true;

        @ConfigValue("captcha.tries")
        public static int tries = 3;

        @ConfigValue("captcha.time")
        public static int timeLimit = 30;

        @ConfigValue("secretCommand.enabled")
        public static boolean secretEnabled = false;

        @ConfigValue("secretCommand.lenght")
        public static int secretLenght = 32;

        @ConfigValue("secretCommand.refreshPeriodHours")
        public static int refreshTime = 6;

        @ConfigValue("secretCommand.webhookURL")
        public static String webhookURL = "";

        @ConfigValue("secretCommand.webhookName")
        public static String webhookName = "Heimdall Secret";

        @ConfigValue("secretCommand.webhookAvatarURL")
        public static String avatarURL = "";

        @ConfigValue("misc.hidePlayers")
        public static boolean hidePlayers = true;

        @ConfigValue("server.main.name")
        public static String mainServerName = "anarchy";

        @ConfigValue("server.main.maintenance-enabled")
        public static boolean maintenanceMode = false;

        @ConfigValue("server.main.maintenance-msg")
        public static String maintenanceMsg = prefix + " " + RED + "The main server is currently under maintenance. You can join the test server with "
                + GOLD + "/join test" + RED + " or by doing " + GOLD + "/join" + RED + " again.\nFor more info check Discord announcements by doing " + GOLD + UNDERLINE + "/discord" ;

        @ConfigValue("server.test.enabled")
        public static boolean testServerEnabled = false;

        @ConfigValue("server.test.name")
        public static String testServerName = "temp";

        @ConfigValue("messages.success")
        public static String successMessage = "Captcha " + GREEN + "solved!";

        @ConfigValue("messages.retry")
        public static String retryMessage = "Captcha " + YELLOW + "failed, " + RESET + "please try again. ({CURRENT}/{MAX})";

        @ConfigValue("messages.fail")
        public static String failMessage = "Captcha " + RED + "failed!";
    }

}