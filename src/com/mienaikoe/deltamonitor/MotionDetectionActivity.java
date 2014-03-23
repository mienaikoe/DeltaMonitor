package com.mienaikoe.deltamonitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.mienaikoe.deltamonitor.CameraWatcherService.CameraWatcherServiceBinder;
import java.io.IOException;






public class MotionDetectionActivity extends Activity {

    private static final String TAG = "MotionDetectionActivity";

    private Camera camera;
    private CameraWatcherService watcherService;
    private boolean bound = false;
    private SurfaceHolder previewHolder;


    
    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.i(TAG, "============================Starting MotionDetectionActivity");
        
        SurfaceView preview = (SurfaceView)findViewById(R.id.preview);
        previewHolder = preview.getHolder();

        Intent bindingIntent = new Intent(this, CameraWatcherService.class);        
        bindService(bindingIntent, connection, Context.BIND_AUTO_CREATE);
    }

    
  

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binderGen) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CameraWatcherServiceBinder binder = (CameraWatcherServiceBinder) binderGen;
            watcherService = binder.getService();
            bound = true;
            
            camera = watcherService.getCamera();
            takeOverCamera();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            watcherService = null;
        }
    };
    
    
    
    
    private void takeOverCamera(){
        try{
            camera.setPreviewDisplay(previewHolder);
        } catch( IOException ex ){
            Log.e(TAG, "Unable to unset preview display");
            ex.printStackTrace();
        }
        camera.startPreview();
    }
    
    
    private void relinquishCamera(){
        camera.stopPreview();
        try{
            camera.setPreviewDisplay(null);
        } catch( IOException ex ){
            Log.e(TAG, "Unable to unset preview display");
            ex.printStackTrace();
        }
    }
    
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        if(!isFinishing()){
            finish();
        }
    }
    
    
    
    @Override
    protected void onStop() {
        super.onStop();
        if(!isFinishing()){
            finish();
        }
    }
    
    

    @Override
    protected void onDestroy() {
        if( bound ){
            relinquishCamera();
            watcherService.startRecording();
            unbindService(connection);
        }
        super.onDestroy();
    }
    
    private void stopWatching(){
        if(bound){
            Intent watcherIntent = new Intent(MotionDetectionActivity.this, CameraWatcherService.class);
            stopService(watcherIntent);
        }
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


}