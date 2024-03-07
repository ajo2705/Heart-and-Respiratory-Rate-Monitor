/*
Author: Akhil Jose
UID: ajose12
 */
package com.example.ajohearresp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.util.Log;

import com.example.ajohearresp.db.SymptomsDatabase;
import com.example.ajohearresp.schema.HeartRespData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Symptoms extends AppCompatActivity {
    public static final String DATA_TAG = "SYMPTOM_DATA";
    public static final String RESP_RATE = "respRate";
    public static final String HEART_RATE = "heartRate";
    public static final String DIALOG_OK = "OK";
    public static final String DIALOG_CANCEL = "Cancel";
    public static final String DISPLAY_ALL_ENTRIES_TEXT = "ALL DATA ENTRIES";
    protected static String DIALOG_TITLE = "SUBMISSION";
    protected static String DIALOG_MESSAGE = "To save changes press OK; press Cancel to keep editing";
    private static final String KEY_ID = "keyID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        Intent intent = getIntent();
        long keyID = intent.getLongExtra(KEY_ID, 0);
        double respRate = intent.getDoubleExtra(RESP_RATE, 0);
        double heartRate = intent.getDoubleExtra(HEART_RATE, 0);

        Map<String,Float> itemToRatingMap = new HashMap<>();
        initRatingMap(itemToRatingMap);

        Spinner symptomSpinner = findViewById(R.id.SymptomsSpinner);
        RatingBar symptomRating = findViewById(R.id.symptomRating);
        Button submitButton = findViewById(R.id.SubmitButton);

        handleSymptomSpinner(itemToRatingMap, symptomSpinner, symptomRating);
        handleSymptomRatingChange(itemToRatingMap, symptomSpinner, symptomRating);
        handleSubmitButton(submitButton, itemToRatingMap, keyID, respRate, heartRate);
    }

    private void handleSubmitButton(Button submitButton, Map<String,Float> itemToRatingMap, long keyID,
                                    double respRate, double heartRate) {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = getAlertDialog(itemToRatingMap, keyID, respRate, heartRate);
                dialog.show();
            }
        });
    }

    @NonNull
    private AlertDialog getAlertDialog(Map<String,Float> itemToRatingMap, long keyID,
                                       double respRate, double heartRate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Symptoms.this);
        builder.setTitle(Symptoms.DIALOG_TITLE);
        builder.setMessage(Symptoms.DIALOG_MESSAGE);
        builder.setPositiveButton(DIALOG_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                try {
                    saveDB(itemToRatingMap, keyID, respRate, heartRate);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                }
                finish();
            }
        });
        builder.setNegativeButton(DIALOG_CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        return dialog;
    }

    private void saveDB(Map<String,Float> itemToRatingMap, long keyID, double respRate,
                        double heartRate) throws IllegalAccessException, InstantiationException {
        SymptomsDatabase db = SymptomsDatabase.getInstance(this);

        HeartRespData data = new HeartRespData(itemToRatingMap);
        data.setHeartRate((float) heartRate);
        data.setRespRate((float) respRate);
        data.uid = (int)keyID;

        Log.d(DATA_TAG, data.toString());
        insertCompleteData(data, db);

    }

    private static void handleSymptomRatingChange(Map<String,Float> itemToRatingMap, Spinner symptomSpinner, RatingBar symptomRating) {
        symptomRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float updatedRating, boolean b) {
                String selectedSpinnerItem = symptomSpinner.getSelectedItem().toString();
                itemToRatingMap.put(selectedSpinnerItem, updatedRating);
            }
        });
    }

    private static void handleSymptomSpinner(Map<String,Float> itemToRatingMap, Spinner symptomSpinner, RatingBar symptomRating) {
        symptomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedSpinnerItem = symptomSpinner.getSelectedItem().toString();
                symptomRating.setRating((Float)itemToRatingMap.get(selectedSpinnerItem));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Nothing to DO
            }
        });
    }

    private void initRatingMap(Map<String,Float> itemToRatingMap) {
        for (String item : getResources().getStringArray(R.array.symptoms_array)) {
            itemToRatingMap.put(item, 0.0f); // Initialize with a default rating of 0.0
        }
    }

    private void insertCompleteData(HeartRespData data, SymptomsDatabase appDatabase) {
        new Thread(() -> {
            appDatabase.heartRespDao().insertHeartRespData(data);
            List<HeartRespData> datas = appDatabase.heartRespDao().getAllHeartRespData();
            Log.d(DATA_TAG, DISPLAY_ALL_ENTRIES_TEXT);
            for (HeartRespData d: datas){
                Log.d(DATA_TAG, d.toString());
            }
            // Handle the data insertion as needed
        }).start();
    }
}