package vn.ali.ott.example.hotline;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;
import androidx.core.app.ActivityCompat;
import androidx.navigation.ui.AppBarConfiguration;

import vn.ali.ott.example.hotline.databinding.ActivityMainBinding;
import vn.ali.ott.core.Constants;
import vn.ali.ott.core.object.ALIOTTEnv;
import vn.ali.ott.core.object.ALIOTTHotlineConfig;
import vn.ali.ott.hotline.ALIOTTDelegate;
import vn.ali.ott.hotline.ALIOTT;
import vn.ali.ott.hotline.object.ALIOTTCall;

public class MainActivity extends AppCompatActivity implements ALIOTTDelegate {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private Function<Boolean, Void> onPermissionRequestResultCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        ALIOTT.getInstance().config(this,
                ALIOTTEnv.PRODUCTION,
                new ALIOTTHotlineConfig(
                        BuildConfig.SERVICE_ID,
                        BuildConfig.SERVICE_KEY,
                        BuildConfig.SERVICE_NAME,
                        BuildConfig.SERVICE_AVATAR
                ),
                R.raw.onhold,
                60000L);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ALIOTT.getInstance().startHotlineCall(
                        BuildConfig.CALLER_ID,
                        BuildConfig.CALLER_NAME,
                        BuildConfig.CALLER_AVATAR
                );
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        ALIOTT.getInstance().setDelegate(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ALIOTT.getInstance().setDelegate(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.AUDIO_REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && permissions.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
                        this.onPermissionRequestResultCallback.apply(grantResults[i] == PackageManager.PERMISSION_GRANTED);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void aliottOnInitSuccess() {

    }

    @Override
    public void aliottOnInitFail(String message) {

    }

    @Override
    public void aliottOnRequestRequiredPermission(@NonNull Function<Boolean, Void> callback) {
        onPermissionRequestResultCallback = callback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, Constants.AUDIO_REQUEST_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void aliottOnRequestShowCall(@NonNull ALIOTTCall call) {
        Intent intent = CallActivity.newIntent(this, call);
        startActivity(intent);
    }
}