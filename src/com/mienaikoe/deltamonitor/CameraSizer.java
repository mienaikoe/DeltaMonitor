/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mienaikoe.deltamonitor;

import android.hardware.Camera;
import android.util.Log;

/**
 *
 * @author Jesse
 */
public class CameraSizer {
    
    
    public static Camera.Parameters sizeUp(Camera camera){
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = getBestPreviewSize(1200, 900 , camera);
        parameters.setPreviewSize(size.width, size.height);
        //parameters.setPreviewFormat(ImageFormat.RGB_565);
        camera.setParameters(parameters);
        return parameters;
    }
    
    
    private static final String TAG = "CameraSizer";
    
    
    private static final int SCALING_FACTOR = 1;

    private static Camera.Size getBestPreviewSize(int width, int height, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size result = null;
        int resultArea = 0;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    Log.i(TAG, "Setting Size to "+size.toString());
                    result = size;
                    resultArea = result.width * result.height;
                } else {
                    int newArea = size.width * size.height;
                    if (newArea > resultArea) {
                        result = size;
                        resultArea = result.width * result.height;
                    }
                }
            }
        }

        if (result != null) {
            return camera.new Size(result.width / SCALING_FACTOR, result.height / SCALING_FACTOR);
        } else {
            return result;
        }
    }
    
}
