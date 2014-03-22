package com.mienaikoe.deltamonitor;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app. NotificationCompat;


/**
 * This class extends Activity to handle a picture preview, process the frame
 * for motion, and then save the file to the SD card.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class StartupActivity extends Activity {

    private static final String TAG = "StartupActivity";




    
    
    
    
    
    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(StartupActivity.this, CameraWatcherService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);
        
        notifyMessage("DeltaMonitor Started");
        finish();
    }
    
    
    private void notifyMessage(String message){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("DeltaMonitor")
                .setContentText(message);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MotionDetectionActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MotionDetectionActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(007, mBuilder.build());
    }
    
    
    
        
}