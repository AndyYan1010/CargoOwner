package com.bt.smart.cargo_owner.servicefile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.RemoteViews;

import com.bt.smart.cargo_owner.MainActivity;
import com.bt.smart.cargo_owner.R;
import com.bt.smart.cargo_owner.utils.ThreadUtils;
import com.bt.smart.cargo_owner.utils.ToastUtils;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;

public class GeTuiIntentService extends GTIntentService {
    private String content = "";
    private int markExamine;

    public GeTuiIntentService() {

    }

    @Override
    public void onReceiveServicePid(Context context, int i) {

    }

    @Override
    public void onReceiveClientId(Context context, String s) {

    }

    @Override
    public void onReceiveMessageData(final Context context, GTTransmitMessage gtTransmitMessage) {
        // 透传消息的处理，详看SDK demo
        byte[] payload = gtTransmitMessage.getPayload();
        if (null != payload) {
            content = new String(payload);
        }
        if ("".equals(content))
            return;

        ThreadUtils.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showToast(context, content);
                //自定义推送通知
                //显示通知
//                sendNotification(context, content);
                showNotification(context, content);
            }
        });
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean b) {

    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage gtCmdMessage) {

    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage gtNotificationMessage) {

    }

    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage gtNotificationMessage) {

    }

    private void showNotification(Context context, String content) {
        markExamine++;
        //1.获取系统通知的管理者
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //2.初始化一个notification的对象
        Notification.Builder mBuilder = new Notification.Builder(this);
        //android 8.0 适配     需要配置 通知渠道NotificationChannel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel b = new NotificationChannel("1", "新的消息", NotificationManager.IMPORTANCE_MIN);
            mNotificationManager.createNotificationChannel(b);
            mBuilder.setChannelId("" + markExamine);
        }
        //添加自定义视图
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.notif_gt_layout);
        mRemoteViews.setTextViewText(R.id.tv_cont, content);

        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Intent[] intents = {mainIntent};
        PendingIntent pendingIntent = PendingIntent.getActivities(context, markExamine, intents, PendingIntent.FLAG_UPDATE_CURRENT);

        //最大音量
//        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0); //tempVolume:音量绝对值
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        //主要设置setContent参数  其他参数 按需设置
        mBuilder.setContent(mRemoteViews);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setOngoing(true);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSound(uri);
        mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        mBuilder.setFullScreenIntent(pendingIntent, true);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        //更新Notification
        mNotificationManager.notify(markExamine, notification);
    }
}
