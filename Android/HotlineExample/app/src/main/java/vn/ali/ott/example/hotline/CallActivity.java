package vn.ali.ott.example.hotline;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Locale;

import vn.ali.ott.example.hotline.widget.RipplePulseLayout;
import vn.ali.ott.core.object.ALIOTTCallState;
import vn.ali.ott.hotline.ALIOTTCallDelegate;
import vn.ali.ott.hotline.ALIOTT;
import vn.ali.ott.hotline.object.ALIOTTCall;

public class CallActivity extends AppCompatActivity implements View.OnClickListener, ALIOTTCallDelegate {
    public static Intent newIntent(Context context,
                                   ALIOTTCall call) {
        Intent intent = new Intent(context, CallActivity.class);

        intent.putExtra("call", call);

        return intent;
    }

    private static final String TAG = "CallActivity";

    private TextView tvLoading;

    private View llSpeaker;

    private View llMic;

    private View llAccept;

    private View llEndCall;
    private ImageView btMic, btSpeaker;

    private RipplePulseLayout rpAvatar;

    ALIOTTCall call;
    boolean speakerOn = false;
    boolean muteCall = false;
    long connectedTime = 0;

    private final Handler mHandler = new Handler();
    Runnable callTimeUp = new Runnable() {
        @Override
        public void run() {
            if (connectedTime > 0) {
                tvLoading.setText(generateTime(System.currentTimeMillis() - connectedTime));
            }

            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "call " + "onCreate " + savedInstanceState);

        setContentView(R.layout.activity_call);

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ALIOTT.getInstance().setCallDelegate(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        ALIOTT.getInstance().setCallDelegate(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacks(callTimeUp);
    }

    private void init() {
        call = getIntent().getParcelableExtra("call");

        rpAvatar = findViewById(R.id.rpAvatar);
        rpAvatar.start();

        tvLoading = findViewById(R.id.tvLoading);

        llAccept = findViewById(R.id.llAccept);
        View btAccept = findViewById(R.id.btAccept);
        btAccept.setOnClickListener(this);

        llEndCall = findViewById(R.id.llEndCall);
        View btEndCall = findViewById(R.id.btEndCall);
        btEndCall.setOnClickListener(this);

        llSpeaker = findViewById(R.id.llSpeaker);
        btSpeaker = findViewById(R.id.btSpeaker);
        btSpeaker.setOnClickListener(this);

        llMic = findViewById(R.id.llMic);
        btMic = findViewById(R.id.btMic);
        btMic.setOnClickListener(this);

        View icBack = findViewById(R.id.icBack);
        icBack.setOnClickListener(this);

        TextView tvName = findViewById(R.id.tvName);

        tvName.setText(call.getHandle());
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        Glide.with(this)
                .load(call.getCalleeAvatar())
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(ivAvatar);
        enableOutGoingView();
    }

    private void enableOutGoingView() {
        tvLoading.setText(R.string.ott_lib_ringing_status);

        llAccept.setVisibility(View.GONE);
        llSpeaker.setVisibility(View.GONE);
        llMic.setVisibility(View.GONE);

        llEndCall.setVisibility(View.VISIBLE);
    }

    private void enableAcceptedView() {
        tvLoading.setText(R.string.ott_lib_connecting_status);

        llAccept.setVisibility(View.GONE);

        llEndCall.setVisibility(View.VISIBLE);
        llSpeaker.setVisibility(View.VISIBLE);
        llMic.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btEndCall) {
            Log.d(TAG, "call finish - " + "btEndCall");
            ALIOTT.getInstance().endCall();
        } else if (id == R.id.btSpeaker) {
            speakerOn = !speakerOn;
            ALIOTT.getInstance().setSpeakOn(speakerOn);
        } else if (id == R.id.btMic) {
            muteCall = !muteCall;
            ALIOTT.getInstance().setMuteCall(!muteCall);
        }
    }

    @Override
    public void aliottOnStartCallFail(String message) {
        finishActivity();
    }

    @Override
    public void aliottOnCallStateChange(@NonNull ALIOTTCallState callState) {
        switch (callState) {
            case ENDED:
                Log.d(TAG, "call finish - " + "updateViewWithCallStatus");
                finishActivity();
                break;
            case ACTIVE:
                rpAvatar.stop();
                enableAcceptedView();
                break;
            case CALLING:
                break;
        }
    }

    @Override
    public void aliottOnCallConnectStateChange(long connectedTime) {
        this.connectedTime = connectedTime;
        mHandler.post(callTimeUp);
    }

    private void finishActivity() {
        if (isTaskRoot()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.e(TAG, "finish and remove task");
                finishAndRemoveTask();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return hours > 0 ? String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes,
                seconds) : String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
    }
}