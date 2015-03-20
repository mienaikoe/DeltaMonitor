package com.jwetherell.motion_detection.detection;

import android.graphics.Color;
import android.util.Log;


//import android.util.Log;

/**
 * This class is used to process integer arrays containing RGB data and detects
 * motion.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class RgbMotionDetection implements IMotionDetection {

    // private static final String TAG = "RgbMotionDetection";

    // Specific settings
    private int mPixelThreshold = 50; // Difference in each individual pixel (RGB)
    private int mThreshold = 5; // Percentage of different pixels
                                              
    private static int[] mPrevious = null;
    private static int mPreviousWidth = 0;
    private static int mPreviousHeight = 0;

    
    
    @Override
    public void reset(){
        mPrevious = null;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getPrevious() {
        return ((mPrevious != null) ? mPrevious.clone() : null);
    }

    protected boolean isDifferent(int[] first, int width, int height) {
        if (first == null) throw new NullPointerException();

        if (mPrevious == null) return false;
        if (first.length != mPrevious.length) return true;
        if (mPreviousWidth != width || mPreviousHeight != height) return true;

        int pixelThreshold = (int)((width * height) * (mThreshold/100.0)); // mThresh is a percentage!
        int totDifferentPixels = 0;
        for (int i = 0, ij = 0; i < height; i++) {
            for (int j = 0; j < width; j++, ij++) {
                int pix = (0xff & (first[ij]));
                int otherPix = (0xff & (mPrevious[ij]));

                // Catch any pixels that are out of range
                if (pix < 0) pix = 0;
                if (pix > 255) pix = 255;
                if (otherPix < 0) otherPix = 0;
                if (otherPix > 255) otherPix = 255;

                if (Math.abs(pix - otherPix) >= mPixelThreshold) {
                    totDifferentPixels++;
                    if( totDifferentPixels >= pixelThreshold ){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Detect motion comparing RGB pixel values. {@inheritDoc}
     */
    @Override
    public boolean detect(int[] rgb, int width, int height) {
        if (rgb == null) throw new NullPointerException();

        int[] original = rgb.clone();

        // Create the "mPrevious" picture, the one that will be used to check
        // the next frame against.
        if (mPrevious == null) {
            mPrevious = original;
            mPreviousWidth = width;
            mPreviousHeight = height;
            // Log.i(TAG, "Creating background image");
            return false;
        }

        // long bDetection = System.currentTimeMillis();
        boolean motionDetected = isDifferent(rgb, width, height);
        // long aDetection = System.currentTimeMillis();
        // Log.d(TAG, "Detection "+(aDetection-bDetection));

        // Replace the current image with the previous.
        mPrevious = original;
        mPreviousWidth = width;
        mPreviousHeight = height;

        return motionDetected;
    }

    public void setmPixelThreshold(int mPixelThreshold) {
        this.mPixelThreshold = mPixelThreshold;
    }

    public void setmThreshold(int mThreshold) {
        Log.i("Threshold: ", String.valueOf(mThreshold));
        this.mThreshold = mThreshold;
    }

    public int getmThreshold() {
        return mThreshold;
    }
    
    
    
}
