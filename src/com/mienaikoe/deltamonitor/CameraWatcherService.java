/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mienaikoe.deltamonitor;

import java.io.IOException;
import android.app.Service;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import android.opengl.GLES20;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.jwetherell.motion_detection.data.Preferences;
import com.jwetherell.motion_detection.detection.AggregateLumaMotionDetection;
import com.jwetherell.motion_detection.detection.IMotionDetection;
import com.jwetherell.motion_detection.detection.LumaMotionDetection;
import com.jwetherell.motion_detection.detection.RgbMotionDetection;
import com.jwetherell.motion_detection.image.ImageProcessing;

public class CameraWatcherService extends Service {

    private static final String TAG = "CameraWatcherService";


    private Camera camera;
    private Camera.Size size;
    private byte[] buffer;
    private SurfaceTexture texture;
    private boolean isRecording = false;
    private boolean toastPopped = false;
    private IMotionDetection detector = null;

    public Camera getCamera() {
        return camera;
    }


    
    

    private static SurfaceTexture getTexture(){
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textures[0]);
        return new SurfaceTexture(textures[0]);
    }
    
    
    
    @Override
    public void onCreate() {
        if (Preferences.USE_RGB) {
            detector = new RgbMotionDetection();
        } else if (Preferences.USE_LUMA) {
            detector = new LumaMotionDetection();
        } else {
            // Using State based (aggregate map)
            detector = new AggregateLumaMotionDetection();
        }

        super.onCreate();
        
        startRecording();
    }
    

    
    


 
    


    @Override
    public void onDestroy() {
        Log.w(TAG, "============Destroying CameraWatcherService");
        stopRecording();
        isRecording = false;
        camera.release();
        super.onDestroy();
    }

    
    
    public void startRecording() {
        if( camera == null ){
            try{
                camera = Camera.open();
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                ex.printStackTrace();
                return;
            }
        }
        
        try{
            Log.i(TAG, "==================Beginning to Record");
            
            if( buffer == null ){
                Camera.Parameters parameters = CameraSizer.sizeUp(camera);
                size = parameters.getPreviewSize();
                buffer = new byte[size.height*size.width*ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8];
            }
            camera.addCallbackBuffer(buffer);
            
            if( texture == null ){
                texture = getTexture();
            }
            camera.setPreviewTexture(texture);
            
            camera.setPreviewCallbackWithBuffer(previewCallback);
            camera.startPreview();
            
        } catch(IOException ex){
            Log.e(TAG, "IOException during recording setup "+ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void stopRecording() {        
        try{
            camera.stopPreview();
            camera.setPreviewCallbackWithBuffer(null);
            camera.setPreviewCallback(null);
            camera.setPreviewTexture(null);
            texture = null; //TODO: This is a patch for a bug (SurfaceTexture has been abandoned)
        } catch(IOException ex){
            Log.e(TAG, "IOException during recording setup "+ex.getMessage());
            ex.printStackTrace();
        }
        
        toastPopped = false;
        isRecording = false;
    }
    
    
    
    private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if( !isRecording ){
                isRecording = true;
            } 
          
            if (data == null) return;
            if (size == null) return;
            
            int[] img = ImageProcessing.decodeYUV420SPtoRGB(buffer, size.width, size.height);
            if (img != null && detector.detect(img, size.width, size.height)) {
                Log.i(TAG, "========== Motion Detected");
                stopRecording();
                activityStarter.sendEmptyMessage(1);
            } else {
                camera.addCallbackBuffer(buffer);
                if(!toastPopped){
                    toastPopped = true;
                    Toast.makeText(getBaseContext(), "DeltaMonitor is recording in the background. Large changes in the camera view will wake up the application.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    
    
    
    
    public class CameraWatcherServiceBinder extends Binder {
        CameraWatcherService getService(){
            return CameraWatcherService.this;
        }
    }
    
    private final IBinder binder = new CameraWatcherServiceBinder();
   
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {            
        startRecording();
        return super.onUnbind(intent); 
    }
    
    
    
    private final Handler activityStarter = new Handler(){
        @Override
        public synchronized void handleMessage(Message m){
            Intent intent = new Intent (CameraWatcherService.this, MotionDetectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };
    

}
