package me.affanhaq.mapcha.hooks;

import fr.xephi.authme.api.v3.AuthMeApi;

public class AuthMeHook {
    private static AuthMeApi authMeApi = null;

    public void initializeHook(){
        authMeApi = AuthMeApi.getInstance();
    }

    public void removeAuthMeHook(){
        authMeApi = null;
    }

    public boolean isAuthMeHookActive(){
        return authMeApi != null;
    }

    public static AuthMeApi getAuthmeApi(){
        return authMeApi;
    }
}
