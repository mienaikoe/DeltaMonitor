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
import android.view.View;
import android.widget.SeekBar;
import com.mienaikoe.deltamonitor.CameraWatcherService.CameraWatcherServiceBinder;
import java.io.IOException;






public class MotionDetectionActivity extends Activity{

    private static final String TAG = "MotionDetectionActivity";

    private Camera camera;
    private CameraWatcherService watcherService;
    private boolean bound = false;
    private SurfaceHolder previewHolder;
    private SeekBar sensitivity;


    
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
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        this.sensitivity = (SeekBar)findViewById(R.id.sensitivity);
        this.sensitivity.setOnSeekBarChangeListener(new SensitivityChanger());

        Intent bindingIntent = new Intent(this, CameraWatcherService.class);        
        bindService(bindingIntent, connection, Context.BIND_ABOVE_CLIENT);
    }

    private class SensitivityChanger implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar sb, int i, boolean bln) {
            watcherService.setSensitivity(i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar sb) {
            // do nothing?
        }

        @Override
        public void onStopTrackingTouch(SeekBar sb) {
            // do nothing?
        }
        
    }
  
    
    
    

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binderGen) {
            Log.i(TAG,"====================================== Service Connected");

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CameraWatcherServiceBinder binder = (CameraWatcherServiceBinder) binderGen;
            watcherService = binder.getService();
            bound = true;
            
            sensitivity.setProgress(watcherService.getSensitivity());
            
            watcherService.stopRecording();
            camera = watcherService.getCamera();
            previewHolder.addCallback(holderCallback);
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
        Log.i(TAG, "===================================== Destroying MD Activity");
        if( bound ){
            relinquishCamera();
            watcherService.startRecording();
            unbindService(connection);
        }
        super.onDestroy();
    }
    
    
    
    public void stopWatching(View view){
        if(bound){
            relinquishCamera();
            Intent watcherIntent = new Intent(MotionDetectionActivity.this, CameraWatcherService.class);
            stopService(watcherIntent);
            unbindService(connection);
            bound = false; // Race Condition was happening.
        }
        finish();
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    
    
    
    private SurfaceHolder.Callback holderCallback = new SurfaceHolder.Callback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
            } catch (Exception ex) {
                Log.e(TAG, "Exception in setPreviewDisplay()", ex);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };


}