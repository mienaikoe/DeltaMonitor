package com.mienaikoe.deltamonitor;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;



public class StartupActivity extends Activity {


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
        
        finish();
    }
    
    
   
        
}