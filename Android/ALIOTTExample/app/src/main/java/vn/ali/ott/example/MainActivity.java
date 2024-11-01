package vn.ali.ott.example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import vn.ali.ott.core.object.ALIOTTCallConfig;
import vn.ali.ott.core.object.ALIOTTCallData;
import vn.ali.ott.core.object.ALIOTTHotlineConfig;
import vn.ali.ott.example.data.local.shared.AppPreferences;
import vn.ali.ott.example.data.remote.RemoteApi;
import vn.ali.ott.example.databinding.ActivityMainBinding;
import vn.ali.ott.ALIOTTDelegate;
import vn.ali.ott.ALIOTT;
import vn.ali.ott.example.fcm.FirebaseMessingService;
import vn.ali.ott.example.presenter.login.LoginActivity;
import vn.ali.ott.object.ALIOTTCall;

public class MainActivity extends AppCompatActivity  {
    private ActivityMainBinding binding;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            saveConfig();
            return true;
        }

        if (item.getItemId() == R.id.action_logout) {
            AppPreferences.get().clear();
            MainApplication.get().ottLogOut();
            startLoginActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void saveConfig() {
        String fcmToken = AppPreferences.get().getCachedFcmToken();
        String clientId = binding.callerId.getText().toString();
        AppPreferences.get().saveClientId(clientId);

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            Log.d("MainActivity", "FCM Token: " + token);
            AppPreferences.get().saveFcmToken(token);
            new RemoteApi()
                    .sendFcmToken(clientId, fcmToken).thenAccept(result -> {
                        Log.d("MainActivity", "Result: " + result);
                        Toast.makeText(this, "Result: " + result, Toast.LENGTH_SHORT).show();
                    });
        });



        AppPreferences.get().saveCalleeId(binding.calleeId.getText().toString());
    }

    private void startLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //LocaleHelper.updateLocale(this, new Locale("vi", "VN"));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);


        binding.btnCall.setOnClickListener(v->{
            startOutgoingCall();
        });

        binding.fab.setOnClickListener(view -> {
            startOutgoingCall();
        });

        binding.btnHotlineCall.setOnClickListener(view->{
            String SERVICE_ID="";
            String SERVICE_KEY="";
            startHotlineCall(SERVICE_ID, SERVICE_KEY);
        });

        bindDataUI();
    }

    private void startHotlineCall(String serviceId, String serviceKey) {
        ALIOTTHotlineConfig hotlineConfig = new ALIOTTHotlineConfig(serviceId, serviceKey);
        String callerId = AppPreferences.get().getClientId();
        String callerName = "Cuoc goi den";
        ALIOTT.getInstance().startHotlineCall(hotlineConfig,
                callerId,
                callerName,
                null, null, null );
    }

    private void bindDataUI() {
        binding.callerId.setText(AppPreferences.get().getClientId());
        binding.calleeId.setText(AppPreferences.get().getCalleeId());
    }

    private void startOutgoingCall() {
        ALIOTTCallConfig callConfig = null; // Cấu hình cuộc gọi (NULL is set default config for P2P Calling)
        String callerId = binding.callerId.getText().toString(); // ID của người gọi
        String callerAvatar = "https://cdn-icons-png.flaticon.com/256/4825/4825015.png"; // Avatar của người gọi
        String callerName = "Nguyễn Thi Nữ"; // Tên của người gọi
        String calleeId = binding.calleeId.getText().toString(); // ID của người nhận
        String calleeAvatar = "https://cdn-icons-png.flaticon.com/256/147/147144.png"; // Avatar của người nhận
        String calleeName = "Nguyễn Văn Nam"; // Tên của người nhận

        int type = 1; // must set value is 1 for P2P Calling

        String displayName = calleeName; // Tên thể hiện on UI của cuộc gọi

        HashMap<String, String> metadata = new HashMap<>(); // Metadata của cuộc gọi


        ALIOTTCallData callData = new ALIOTTCallData(
                callerId,
                callerAvatar,
                callerName,
                calleeId,
                calleeAvatar,
                calleeName,
                type, metadata); // Dữ liệu cuộc gọi

        ALIOTT.getInstance().startOutgoingCall(displayName, callConfig, callData);
    }
}