package vn.ali.ott.example.presenter.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.CompletableFuture;

import vn.ali.ott.core.utils.Function;
import vn.ali.ott.example.MainActivity;
import vn.ali.ott.example.MainApplication;
import vn.ali.ott.example.data.local.shared.AppPreferences;
import vn.ali.ott.example.data.remote.RemoteApi;
import vn.ali.ott.example.databinding.LoginActivityBinding;
import vn.ali.ott.example.presenter.entry.EntryActivity;

public class LoginActivity extends AppCompatActivity {

    private LoginActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LoginActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> {
            // Login
            String clientId = binding.inputClientId.getText().toString();
            if (clientId.isEmpty()) {
                binding.inputClientId.setError("Client ID is required");
                return;
            }

            saveConfig(clientId);
        });
    }

    private void saveConfig(String clientId) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                AppPreferences.get().saveFcmToken(task.getResult());
                String fcmToken = AppPreferences.get().getCachedFcmToken();
                AppPreferences.get().saveClientId(clientId);
                Log.d("MainActivity", "FCM Token: " + fcmToken);
                new RemoteApi()
                        .sendFcmToken(clientId, fcmToken, s -> {
                            runOnUiThread(() -> {
                                Log.d("MainActivity", "Result: " + s);
                                Toast.makeText(this, "Result: " + s, Toast.LENGTH_SHORT).show();
                            });

                            return null;
                        });

                resetApp();
            }else{
                Toast.makeText(this, "Result: FCM loi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetApp() {
        Intent intent = new Intent(this, EntryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
