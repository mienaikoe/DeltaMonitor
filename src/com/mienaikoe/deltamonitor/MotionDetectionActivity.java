package com.mienaikoe.deltamonitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import com.mienaikoe.deltamonitor.CameraWatcherService.CameraWatcherServiceBinder;
import java.io.IOException;


/**
 * This class extends Activity to handle a picture preview, process the frame
 * for motion, and then save the file to the SD card.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
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
            
            try{
                camera = watcherService.getCamera();
                camera.setPreviewDisplay(previewHolder);
                camera.startPreview();
            } catch(IOException ex) {
                Log.e(TAG, "Could not set preview display: "+ex.getMessage());
                ex.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            watcherService = null;
        }
    };
    
    
    
    

    @Override
    protected void onDestroy() {
        if(bound){
            camera.stopPreview();
            try{
                camera.setPreviewDisplay(null);
            } catch( IOException ex ){
                Log.e(TAG, "Unable to unset preview display");
                ex.printStackTrace();
            }
            camera = null;
            unbindService(connection);
        }
        super.onDestroy();
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
    }


}