package vn.ali.ott.example.data.remote;

import android.content.Context;

public class ApiEndpoint {

    static String getBaseUrl() {
        return "https://ottdev.taxidayroi.vn/api/ott";
    }

    public static String getSaveFcm() {

        return getBaseUrl() + "/fcm/save";
    }

    public static String getPushNotify() {
        return getBaseUrl() + "/ott/make_call";
    }
}
