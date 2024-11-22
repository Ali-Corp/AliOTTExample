package vn.ali.ott.example.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import vn.ali.ott.ALIOTT;
import vn.ali.ott.core.object.ALIOTTUser;
import vn.ali.ott.example.MainActivity;
import vn.ali.ott.example.MainApplication;
import vn.ali.ott.example.R;
import vn.ali.ott.example.app.Constants;
import vn.ali.ott.example.data.local.shared.AppPreferences;
import vn.ali.ott.example.data.remote.RemoteApi;

public class FirebaseMessingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessingService";

    private boolean needsToBeScheduled() {
        return true;
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (!remoteMessage.getData().isEmpty()) {
            if(needsToBeScheduled()){
                scheduleJob(remoteMessage);
            }else{
                handleNow();
            }
        }

        if (remoteMessage.getNotification() !=null){
            Log.d(TAG, "Message Notification Body: ${it.body}");
        }

    }

    public void handleNow(){}

    private void scheduleJob(RemoteMessage remoteMessage) {
        String callerName = remoteMessage.getData().get("caller_name");
        if(callerName == null || callerName.isEmpty())
            callerName = "Unknown Name";

        Data data = new Data.Builder()
                .putString("type", remoteMessage.getData().get("type"))
                .putString("alert", remoteMessage.getData().get("alert"))
                .putString("caller_name", callerName)
                .putString("callee_id", AppPreferences.get().getClientId())
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(this)
                .enqueue(work);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        AppPreferences.get().saveFcmToken(token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        if (!AppPreferences.get().getClientId().isEmpty()){
            new RemoteApi().sendFcmToken(AppPreferences.get().getClientId(), token, s -> {
                        Log.d(TAG, "Result: " + s);
                        return null;
                    });
        }
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        int notificationId = 0;
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public static class MyWorker extends Worker {
        protected MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            Log.d(TAG, "Worker started");

            String type = getInputData().getString("type");

            if (type!=null && type.equals("9")){
                String callerName = getInputData().getString("caller_name");
                String alert = getInputData().getString("alert");
                Map metadata = new HashMap(){{
                   put("check_sum", Constants.checksume);
                }};
                ALIOTT.getInstance().notifyOnReceiveIncomingCallFCM(callerName, alert,metadata);
            }

            return Result.success();
        }
    }
}
