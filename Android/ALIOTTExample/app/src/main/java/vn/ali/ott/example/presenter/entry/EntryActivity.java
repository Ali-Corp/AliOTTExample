package vn.ali.ott.example.presenter.entry;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import vn.ali.ott.example.MainActivity;
import vn.ali.ott.example.MainApplication;
import vn.ali.ott.example.data.local.shared.AppPreferences;
import vn.ali.ott.example.databinding.ActivityEntryBinding;

import vn.ali.ott.example.presenter.login.LoginActivity;

public class EntryActivity extends AppCompatActivity {

    private ActivityEntryBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        checkIsLogin();
    }

    private void checkIsLogin() {
        if (AppPreferences.get().getClientId().isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            MainApplication.get().ottInitialize();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}