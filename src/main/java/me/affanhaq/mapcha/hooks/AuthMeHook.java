package me.affanhaq.mapcha.hooks;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;

public class AuthMeHook {
    private AuthMeApi authMeApi = null;

    public void initializeHook(){
        authMeApi = AuthMeApi.getInstance();
    }

    public void removeAuthMeHook(){
        authMeApi = null;
    }

    public boolean isAuthMeHookActive(){
        return authMeApi != null;
    }
}
