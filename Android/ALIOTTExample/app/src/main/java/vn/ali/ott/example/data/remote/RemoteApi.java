package vn.ali.ott.example.data.remote;

import android.content.Context;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import vn.ali.ott.ALIOTT;
import vn.ali.ott.core.object.ALIOTTEnv;
import vn.ali.ott.example.MainActivity;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemoteApi {

    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public RemoteApi() {
    }

    public CompletableFuture<String> sendFcmToken(String userId, String token) {
        CompletableFuture<String> future = new CompletableFuture<>();

        executor.submit(() -> {
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

            try(Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    future.completeExceptionally(new IOException("Unexpected code " + response));
                }
                // Handle the response
                System.out.println(response.body().string());
                future.complete(response.body().string());
            }catch (Exception e){
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public CompletableFuture<String> sendPushNotifyToCallee(String alert, String calleeId) {
        CompletableFuture<String> future = new CompletableFuture<>();

        executor.submit(() -> {
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

            try(Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    future.completeExceptionally(new IOException("Unexpected code " + response));
                }
                // Handle the response
                System.out.println(response.body().string());
                future.complete(response.body().string());
            }catch (Exception e){
                future.completeExceptionally(e);
            }
        });

        return future;
    }
}
