/*
Author: Akhil Jose
UID: ajose12
 */
package com.example.ajohearresp.task;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ajohearresp.MainCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class SlowTask extends AsyncTask<String, String, String> {
    public static final String TAG = "VIDEO PROCESSING";
    private Context callerContext;
    private MainCallback callback;
    private MediaMetadataRetriever retriever;

    public SlowTask(Context context, MainCallback callback, MediaMetadataRetriever retriever){
        this.callerContext = context;
        this.callback = callback;
        this.retriever = retriever;
    }

    @Override
    protected void onPreExecute() {
        // This method runs on the UI thread before the background task starts.
        // You can perform setup or UI updates here.
    }

    @Override
    protected String doInBackground(String... strings) {
        ArrayList<Bitmap> frameList = new ArrayList<>();

        try {
            int frameCount = Integer.parseInt(Objects.requireNonNull(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)));

            Log.d(SlowTask.TAG, Integer.toString(frameCount) + " frames of video");

            /* USING getFrameAtIndex instead of getFrameAtTime due to resource constraints and
                time consumption.
            */
            for(int i=10; i < frameCount; i+=5){
                Bitmap bitmap = retriever.getFrameAtIndex(i);
                frameList.add(bitmap);
            }

            Log.d(SlowTask.TAG, Integer.toString(frameList.size()) + "  frames");
            retriever.release();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long redBucket = 0, pixelCount = 0;
        ArrayList<Long> a = new ArrayList<>();

        int xStart = 0, yStart = 0;

        for (Bitmap frame: frameList) {
            redBucket = 0;
            int width = frame.getWidth(), height = frame.getHeight();
            for (int y = yStart+ height/4; y < yStart + (3 * height/4); y++) {
                for (int x = xStart+ width/4; x < xStart + (3 * width/ 4); x++) {
                    int color = frame.getPixel(x, y);
                    pixelCount++;
                    // Why we need all colors to compute variation in Red
                    redBucket += Color.red(color) + Color.blue(color) + Color.green(color);
                }
            }
            a.add(redBucket);
        }
        Log.d(SlowTask.TAG, Integer.toString(a.size()) + "  A bucket size");
        ArrayList<Long> b = new ArrayList<>();
        for(int i=0; i< a.size() - 5; i++){
            long temp = (a.get(i) + a.get(i+1) + a.get(i+2) + a.get(i+3)+ a.get(i+4)) / 4;
            b.add(temp);
        }

        Log.d(TAG, "B BUCKET SIZE : " + b.size());

        long x = b.get(0);
        int count = 0;
        for(int i=1; i < b.size(); i++){
            long p = b.get(i);
            if((p-x) > 3500)
                count++;
            x = p;
        }

        double rate = ((double) count/45.0)*60;
        String strRate = Double.toString(rate/2);

        Log.d(TAG, "HEART RATE : " + strRate);
        return strRate;
    }

    @Override
    protected void onPostExecute(String result) {
        // This method runs on the UI thread before the background task starts.
        // You can perform setup or UI updates here.
        super.onPostExecute(result);
        if (callback != null) {
            callback.onCallback(result);
        }
    }

}
