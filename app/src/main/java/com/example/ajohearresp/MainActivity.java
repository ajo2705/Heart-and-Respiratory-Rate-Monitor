/*
Author: Akhil Jose
UID: ajose12
 */
package com.example.ajohearresp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.video.impl.VideoCaptureConfig;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ajohearresp.db.SymptomsDatabase;
import com.example.ajohearresp.schema.HeartRespData;
import com.example.ajohearresp.task.SlowTask;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements SensorEventListener, SurfaceHolder.Callback, MainCallback{
    public static final String PERMISSION_REQ_MESSAGE = "This app requires camera and storage permissions to function. Please grant the required permissions.";
    public static final String PERMISSION_REQ_TITLE = "Permission Required";
    public static final String DIALOG_OK = "OK";
    public static final String DIALOG_EXIT = "Exit";
    public static final String RECORDING_STARTED_MESSAGE = "Started Recording";
    public static final String RECORDING_FINISH_MESSAGE = "Finished Recording";
    public static final String VIDEO_PROCESSING_MESSAGE_WITH_FILE_NAME = "  is being processed and being done by background activity";
    public static final String HEART_RATE_CALC_TOAST_MESSAGE = "Video processing started";
    public static final String VIDEO_PROCESSING_TAG = "VIDEO PROCESSING";
    public static final String HEART_RATE_SUCC_TOAST_MSG = "Video processed and heart rate computed";
    public static final String CAMERA_TAG = "CAMERA_AJO";
    public static final String HEART_RATE_VIDEO_NAME = "heartRateVideo";
    public static final String VIDEO_MIME_TYPE = "video/mp4";
    public static final String VIDEO_PATH = "DCIM/cameraX/HeartRate";
    public static final String KEY_ID = "keyID";
    public static final String RESP_RATE = "respRate";
    public static final String HEART_RATE = "heartRate";
    // PERMISSION CONSTANTS
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    private static final int RECORD_DURATION_MS = 45000;
    public static final String DB_UPLOAD_MESSAGE = "Uploaded the monitored values into DB";
    public static final String VIDEO_TAG = "VIDEO";
    private ArrayList<Float> accelValuesX;
    private ArrayList<Float> accelValuesY;
    private ArrayList<Float> accelValuesZ;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isMonitoring;
    private Handler respHandler;
    private Runnable stopMonitoringRunnable;
    private double resp_Rate;
    // CAMERA VARIABLES
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private boolean isRecording;
    private Handler recHandler;
    private Runnable stopRecordingRunnable;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView cameraView;
    VideoCapture<Recorder> videoCapture;
    private Camera camera;
    private Recording recording;
    private double heart_Rate;

    protected long keyId;
    private String CAMERA_START_MESSAGE = "STARTED CAMERA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRespRateData(0.0);
        setHeartRateData(String.format("%.6f", 0.0));

        checkPermissions();
        configureSymptomsMapper();
        configureRespRateSensors();
        configureHeartRateRecorder();
        configureUpload();
        configureMapMapper();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED){
            // Permission is not granted; request the permission.
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            // Check if all requested permissions were granted
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                configureSymptomsMapper();
                configureRespRateSensors();
                configureHeartRateRecorder();
                configureUpload();
            } else {
            }
        }
    }

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(PERMISSION_REQ_MESSAGE)
                .setTitle(PERMISSION_REQ_TITLE)
                .setPositiveButton(DIALOG_OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE );
                    }
                })
                .setNegativeButton(DIALOG_EXIT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); // Close the activity
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Upload Button and its associated usages
    private void configureUpload() {
        Button uploadButton = findViewById(R.id.UPLOAD);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDB();
            }
        });
    }

    // Save the respiration and heart rate that were computed to DB
    private void saveDB() {
        HeartRespData heartRespData = new HeartRespData();
        heartRespData.setRespRate((float) resp_Rate);
        heartRespData.setHeartRate((float) heart_Rate);

        SymptomsDatabase db = SymptomsDatabase.getInstance(this);

        insertIncompleteData(heartRespData, db);

        Log.i(VIDEO_TAG, DB_UPLOAD_MESSAGE);
        Toast.makeText(this, DB_UPLOAD_MESSAGE, Toast.LENGTH_LONG).show();
    }

    private void configureHeartRateRecorder() {
        Button heartButton = findViewById(R.id.heartRateButton);
        isRecording = false;
        recHandler = new Handler();
        stopRecordingRunnable = new Runnable() {
            @Override
            public void run() {
                stopRecording();
            }
        };

        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setHeartRateData("COMPUTING....");
                startRecording();
            }
        });

        cameraView = findViewById(R.id.cameraView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try{
                ProcessCameraProvider provider = cameraProviderFuture.get();
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    startCameraX(provider);
                }
                else{
                    startCameraX(provider);
                }
            }
            catch (ExecutionException e){
                e.printStackTrace();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }, getExecutor());
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void startRecording(){
        Log.d(VIDEO_TAG, RECORDING_STARTED_MESSAGE);
        recHandler.postDelayed(stopRecordingRunnable, RECORD_DURATION_MS);
        recordVideo();
    }

    private void stopRecording(){
        isRecording = false;
        Log.d(VIDEO_TAG, RECORDING_FINISH_MESSAGE);
        recording.stop();
        recording = null;
        toggleFlash(false);
        recHandler.removeCallbacks(stopRecordingRunnable);
    }

    private void heartRateProcessing(Uri contentUri) throws IOException {
        String filePath = getRealPathFromUri(contentUri);
        Log.d(VIDEO_TAG, filePath + VIDEO_PROCESSING_MESSAGE_WITH_FILE_NAME);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this,  contentUri);

        new SlowTask(this, this, retriever).execute(filePath);

        Toast.makeText(this, HEART_RATE_CALC_TOAST_MESSAGE, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCallback(String result) {
        heart_Rate = Double.parseDouble(result);

        Log.i(VIDEO_PROCESSING_TAG, "Called Heart Rate Calc");
        Log.i(VIDEO_PROCESSING_TAG, result);

        setHeartRateData(String.format("%.6f", heart_Rate));
        Toast.makeText(this, HEART_RATE_SUCC_TOAST_MSG, Toast.LENGTH_LONG);
    }

    private void setHeartRateData(String heartRate) {
        TextView heartRateText = findViewById(R.id.HeartRateData);
        heartRateText.setText(heartRate);
    }

    private String getRealPathFromUri(Uri videoUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = this.getContentResolver().query(videoUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }


    private void startCameraX(ProcessCameraProvider provider) {
        provider.unbindAll();
        Log.d(CAMERA_TAG, CAMERA_START_MESSAGE);

        //Camera selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraView.getSurfaceProvider());

        //Video capture use case
        Recorder.Builder builder = new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.LOWEST));
        videoCapture = VideoCapture.withOutput(builder.build());

        camera = provider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, videoCapture);
    }

    private void recordVideo() {
        if (videoCapture != null && !isRecording){
            isRecording = true;
            ContentValues contentValues = getContentVideoFile();

            MediaStoreOutputOptions mediaStoreOutput = new MediaStoreOutputOptions.Builder(
                    getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    .setContentValues(contentValues)
                    .build();

            // Configure Recorder and Start recording to the mediaStoreOutput
            toggleFlash(true);
            recording = videoCapture.getOutput().prepareRecording(MainActivity.this, mediaStoreOutput).start(getExecutor(), videoRecordEvent -> {
                if(videoRecordEvent instanceof VideoRecordEvent.Start){
                    //TODO: If time
                }
                else if(videoRecordEvent instanceof VideoRecordEvent.Finalize){
                    if(!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()){
                        String msg = "Video capture succeeded : " + ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                        Log.i(VIDEO_TAG, msg);
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        try {
                            // Calling processing of heart rate when Finalised event in recording is called
                            isRecording = false;
                            heartRateProcessing(((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        isRecording = false;
                        recording.close();
                        recording = null;
                        String msg = "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                        Log.e(VIDEO_TAG, msg);
                    }
                }
            });
        }
    }

    @NonNull
    private static ContentValues getContentVideoFile() {
        String fileName = HEART_RATE_VIDEO_NAME; // Assuming only 1 file is needed all the time
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Video.Media.MIME_TYPE, VIDEO_MIME_TYPE);
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, VIDEO_PATH);
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
        return contentValues;
    }

    private void toggleFlash(boolean setValue) {
        if(camera.getCameraInfo().hasFlashUnit())
            camera.getCameraControl().enableTorch(setValue);
        else
            runOnUiThread(()-> Toast.makeText(this, "Flash unavailable",
                    Toast.LENGTH_SHORT).show());
    }

    private void configureRespRateSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Button respRateButton = findViewById(R.id.respRateButton);

        accelValuesX = new ArrayList<>();
        accelValuesY = new ArrayList<>();
        accelValuesZ = new ArrayList<>();
        isMonitoring = false;
        respHandler = new Handler();
        stopMonitoringRunnable = new Runnable() {
            @Override
            public void run() {
                stopMonitoring();
            }
        };


        respRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isMonitoring) {
                    setRespRateData(0.0);
                    Log.d("KEY", "Started Monitor");
                    startMonitoring();
                }
            }
        });
    }

    private void configureSymptomsMapper() {
        Button goToSymptomsButton = findViewById(R.id.SymptomsButton);
        goToSymptomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Create an intent to start the SymptomsActivity
                Intent intent = new Intent(MainActivity.this, Symptoms.class);
                intent.putExtra(KEY_ID, keyId);
                intent.putExtra(RESP_RATE, resp_Rate);
                intent.putExtra(HEART_RATE, heart_Rate);
                startActivity(intent);
            }
        });
    }

    private void configureMapMapper(){
        Button goToMap = findViewById(R.id.MapButton);
        goToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Create an intent to start the MapActivity
                Intent intent = new Intent(MainActivity.this, MaplocationActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onSensorChanged(SensorEvent event){
        if(isMonitoring && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            accelValuesX.add(event.values[0]);
            accelValuesY.add(event.values[0]);
            accelValuesZ.add(event.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // DO NOTHING
    }

    private void startMonitoring(){
        isMonitoring = true;
        Log.d("KEY", "Registered");
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        respHandler.postDelayed(stopMonitoringRunnable, RECORD_DURATION_MS);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(accelerometer != null){
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void stopMonitoring(){
        isMonitoring = false;
        Log.d("KEY", "Stopped Monitor");
        respHandler.removeCallbacks(stopMonitoringRunnable);
        callRespiratoryCalculator();
        accelValuesX.clear();
        accelValuesY.clear();
        accelValuesZ.clear();
    }

    private void callRespiratoryCalculator(){
        float previousValue = 0.0f;
        float currentValue = 0.0f;
        int k = 0;
        int dataPoints = accelValuesX.size();
        for(int i=11; i <=450 && i < dataPoints; i++){
            currentValue = (float) Math.sqrt(accelValuesX.get(i) * accelValuesX.get(i) +
                    accelValuesY.get(i) * accelValuesY.get(i) +
                    accelValuesZ.get(i) * accelValuesZ.get(i) );
            if(Math.abs(previousValue - currentValue) > 0.15){
                k++;
            }
            previousValue = currentValue;
        }
        double ret = (k/45.00);
        double respiratoryRate = (ret * 30);

        setRespRateData(respiratoryRate);

        resp_Rate = respiratoryRate;
    }

    private void setRespRateData(double respiratoryRate) {
        TextView respText = findViewById(R.id.RespRateData);
        respText.setText(String.format("%.6f", respiratoryRate));
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    private void insertIncompleteData(HeartRespData data, SymptomsDatabase appDatabase) {
        new Thread(() -> {
            keyId = appDatabase.heartRespDao().insertHeartRespData(data);
        }).start();
    }

}