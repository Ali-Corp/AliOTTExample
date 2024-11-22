package vn.ali.ott.example;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ProcessLifecycleOwner;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import vn.ali.ott.ALIOTT;
import vn.ali.ott.ALIOTTDelegate;
import vn.ali.ott.core.object.ALIOTTCallConfig;
import vn.ali.ott.core.object.ALIOTTEndCallReason;
import vn.ali.ott.core.object.ALIOTTEnv;
import vn.ali.ott.core.object.ALIOTTServerConfig;
import vn.ali.ott.core.object.ALIOTTUser;
import vn.ali.ott.example.data.local.shared.AppPreferences;
import vn.ali.ott.example.data.remote.RemoteApi;
import vn.ali.ott.object.ALIOTTCall;

public class MainApplication extends Application implements DefaultLifecycleObserver {

   private static MainApplication instance;

    public static MainApplication  get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        AppPreferences.initial(this);

        addLifecycleObserver();
        onOttDelegate();
        ottInitialize();
    }


    public void ottLogOut(){
        ALIOTT.getInstance().logout();
    }

    public void ottInitialize(){
        Log.d("OTT", "ottInitialize");
        if(AppPreferences.get().getClientId().isEmpty()) return;

        //Configure OTT Environment
        ottConfigEnv();

        //Login User, This let OTT-Service know who you are
        loginOTT();
    }

    private void loginOTT() {
        String clientId = AppPreferences.get().getClientId();
        // OTT Account credentials
        String username = getString(R.string.ott_app_id);
        String credential = getString(R.string.ott_app_password);
        String secret = getString(R.string.ott_app_secret);

        ALIOTTCallConfig config = new ALIOTTCallConfig(username, credential, secret);
        // Let OTT know who you are we must 2 parameters: ClientId and OTTCredential(username, credential, secret)
        ALIOTTUser user = new ALIOTTUser(clientId, config);
        ALIOTT.getInstance().login(user);
    }

    private void ottConfigEnv() {
        // Câu lệnh này sẽ gọi hàm config của ALIOTT,
        ALIOTTEnv enviroment  = ALIOTTEnv.SANDBOX;

        ALIOTTServerConfig serverConfig = null;
        boolean useUICallingDefault = true; // Sử dụng giao diện mặc định của ALIOTT, false nếu sử dụng self-custom UI
        @RawRes Integer incomingCallSoundResId = null; // Sử dụng âm thanh mặc định set value NULL
        @RawRes Integer outgoingCallSoundResId = null; // Sử dụng âm thanh mặc định set value NULL
        Long callOutgoingTimeout = 60000L; // Thời gian timeout khi gọi ra, mặc định 60s


        ALIOTT.getInstance().config(this,
                enviroment ,
                serverConfig,
                incomingCallSoundResId,
                outgoingCallSoundResId,
                callOutgoingTimeout,
                useUICallingDefault);

        Locale locale = new Locale("vi", "VN");
        ALIOTT.getInstance().setLocale(locale);
    }

    private void addLifecycleObserver() {
        //Add lifecycle observer to listen app status for OTT
        ProcessLifecycleOwner.get().getLifecycle().addObserver((LifecycleEventObserver) (lifecycleOwner, event) -> {
            if (event == Lifecycle.Event.ON_RESUME) {
                ALIOTT.getInstance().onResume();
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                ALIOTT.getInstance().onPause();
            }else if (event == Lifecycle.Event.ON_DESTROY) {
                ALIOTT.getInstance().setDelegate(null);
            }
        });
    }

    private void onOttDelegate() {
        ALIOTTDelegate ottDelegate = new ALIOTTDelegate() {
            @Override
            public void aliottOnNotifyOutgoingCall(@NonNull String calleeId, @NonNull String alert) {
                //Gọi api push notify to callee (Must be implemented)
                new RemoteApi().sendPushNotifyToCallee(alert, calleeId, s -> null);
            }

            @Override
            public void aliottRequestShowIncomingCallNotification(@NonNull ALIOTTCall aliottCall) {

            }

            @Override
            public void aliottOnRequestShowCall(@NonNull ALIOTTCall call) {

            }

            @Override
            public void aliottOnRequestHideCall(@NonNull ALIOTTCall call, ALIOTTEndCallReason reason) {
                // Implement UI when call end
                Log.d("OTT", "aliottOnRequestHideCall: " + reason);
            }

            @Override
            public HashMap<String, String> aliottOnRequestCustomMetadataForCall(@NonNull ALIOTTCall call) {
                return new HashMap<String, String>() {
                    {
                        put("message_deeplink", "https://deeplink.com"); // Deeplink của cuộc gọi
                        put("call_connecting" , "Connecting"); // Deeplink của cuộc gọi
                        put("call_ringing" , call.isOutgoing() ? "Ringing" : "Calling you"); // Deeplink của cuộc gọi
                        put("call_end","Call end"); // Deeplink của cuộc gọi
                        put("call_btn_speaker" , "Speaker"); // Deeplink của cuộc gọi
                        put("call_btn_mute" , "Mute");
                        put("call_refused", "The driver refused call");
                        put("call_lose_connection", "Lost connection");
                        put("free_call_title", "Free call123");
                        put("call_btn_message", "Message111");
                        put("end_call_delay", "2000");
                        put("title_head_notification", "Xanh SM");
                    }
                };
            }
        };

        ALIOTT.getInstance().setDelegate(ottDelegate);
    }
}
