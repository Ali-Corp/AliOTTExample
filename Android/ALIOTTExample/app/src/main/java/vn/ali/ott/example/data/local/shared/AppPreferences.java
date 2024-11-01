package vn.ali.ott.example.data.local.shared;

import android.content.Context;
import android.content.SharedPreferences;

import vn.ali.ott.example.MainApplication;

public class AppPreferences {
    private static AppPreferences instance;
    private final Context mContext;

    private final String PREF_NAME = "ott_example";
    private final String PREF_KEY_CLIENT_ID = "client_id";
    private final String PREF_KEY_CALLEE_ID = "callee_id";
    private final String PREF_TOKEN = "fcm_token";

    private SharedPreferences getPrefs() {
        return mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public AppPreferences(Context context) {
        this.mContext = context;
    }

    public static AppPreferences get(){
        return instance;
    }

    public static void initial(Context context) {
        instance = new AppPreferences(context);
    }

    public String getClientId() {
        return getPrefs().getString(PREF_KEY_CLIENT_ID, "");
    }

    public void saveFcmToken(String token) {
        getPrefs().edit().putString(PREF_TOKEN, token).apply();
    }

    public void saveClientId(String clientId) {
        getPrefs().edit().putString(PREF_KEY_CLIENT_ID, clientId).apply();
    }

    public String getCachedFcmToken() {
        return getPrefs().getString(PREF_TOKEN, "");
    }


    public void clear() {
        getPrefs().edit().clear().apply();
    }

    public void saveCalleeId(String calleeId) {
        getPrefs().edit().putString(PREF_KEY_CALLEE_ID, calleeId).apply();
    }

    public String getCalleeId() {
        return getPrefs().getString(PREF_KEY_CALLEE_ID, "");
    }
}
