package me.affanhaq.mapcha;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
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

import java.util.*;

import static me.affanhaq.mapcha.Mapcha.Config.*;
import static org.bukkit.ChatColor.*;

public class Mapcha extends JavaPlugin {

    private final CaptchaPlayerManager playerManager = new CaptchaPlayerManager();
    private final Set<UUID> completedCache = new HashSet<>();

    private Listener authMeListener;
    private AuthMeHook authMeHook;

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

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
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

    public void removeAuthMeHook(){
        authMeHook.removeAuthMeHook();
    }

    public boolean isAuthMeHookActive(){
        return authMeHook.isAuthMeHookActive();
    }

    @ConfigFile("config.yml")
    public static class Config {
        public static String permission = "heimdall.bypass";

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

        @ConfigValue("misc.hidePlayers")
        public static boolean hidePlayers = true;

        @ConfigValue("server.mainName")
        public static String mainServerName = "anarchy";

        @ConfigValue("server.testName")
        public static String testServerName = "temp";

        @ConfigValue("messages.success")
        public static String successMessage = "Captcha " + GREEN + "solved!";

        @ConfigValue("messages.retry")
        public static String retryMessage = "Captcha " + YELLOW + "failed, " + RESET + "please try again. ({CURRENT}/{MAX})";

        @ConfigValue("messages.fail")
        public static String failMessage = "Captcha " + RED + "failed!";
    }

}