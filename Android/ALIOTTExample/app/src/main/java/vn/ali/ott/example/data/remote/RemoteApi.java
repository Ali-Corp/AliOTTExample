package vn.ali.ott.example.data.remote;

import androidx.annotation.NonNull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import vn.ali.ott.core.utils.Function;
import vn.ali.ott.core.utils.Utils;

import java.io.IOException;

public class RemoteApi {

    public RemoteApi() {
    }

    public void sendFcmToken(String userId, String token, Function<String, Void> cb) {
        OkHttpClient client = Utils.getInstance().newOkHttpBuilder().build();

        String url = ApiEndpoint.getSaveFcm();

        RequestBody body = new FormBody.Builder()
                .add("user_id", userId)
                .add("token", token)
                .add("platform", "android")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                cb.apply(e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                cb.apply(response.body().string());
            }
        });
    }

    public void sendPushNotifyToCallee(String alert, String calleeId, Function<String, Void> cb) {

        OkHttpClient client = Utils.getInstance().newOkHttpBuilder().build();
        String url = ApiEndpoint.getPushNotify();

        RequestBody body = new FormBody.Builder()
                .add("alert", alert)
                .add("user_id", calleeId)
                .add("platform", "android")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                cb.apply(e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                cb.apply(response.body().string());
            }
        });
    }
}
