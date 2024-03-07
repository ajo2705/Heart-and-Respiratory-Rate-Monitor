package com.example.ajohearresp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ajohearresp.task.AsyncTaskResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MaplocationActivity extends AppCompatActivity implements SecondCallback{
    private static final String API_KEY = "API_KEY";
    public static final String HTTPS_MAPS_GOOGLEAPIS_COM_MAPS_API_DIRECTIONS_JSON = "https://maps.googleapis.com/maps/api/distancematrix/json";
    public static final String COGNITIVE_LOAD_VIEW = "COGNITIVE LOAD : ";
    public static final String AVG_TIME_VIEW = "AVERAGE TIME : ";
    public static final String CURR_TIME_VIEW = "CURRENT TIME : ";
    public static final String MAP_TAG = "GOOGLE_MAP";
    public static final String MAP_CONTENT = "DISPLAYING MAP CONTENT";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maplocation);

        configurePlanning();


    }

    private void findCongnitiveLoad(String start, String end){
        String cognitiveResponse = String.valueOf(new planRouteTask(start, end, this).execute(start, end));
        Log.d(MAP_TAG, cognitiveResponse);
    }

    private void configurePlanning() {
        Button planButton = findViewById(R.id.PlanRouteButton);

        final EditText startAddress = findViewById(R.id.startLocationText);
        final EditText endAddress = findViewById(R.id.endLocationText);


        planButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(MAP_TAG, "Button pressed");
//                mapPlanRunnable.run();
                String start = startAddress.getText().toString();
                String end = endAddress.getText().toString();
                findCongnitiveLoad(start, end);
            }
        });
    }

    @Override
    public void onCallback(AsyncTaskResult result) {
        TextView cognitiveLoadView = findViewById(R.id.CognitiveLoadView);
        TextView currentTimeView = findViewById(R.id.CurrentTimeView);
        TextView averageTimeView = findViewById(R.id.AvgTimeView);

        cognitiveLoadView.setText(COGNITIVE_LOAD_VIEW.concat(result.getResultString()));
        currentTimeView.setText(CURR_TIME_VIEW.concat(String.valueOf(result.getValue1())));
        averageTimeView.setText(AVG_TIME_VIEW.concat(String.valueOf(result.getValue2())));
    }

    private class planRouteTask extends AsyncTask<String, String, AsyncTaskResult>
    {
        private final String startAdress;
        private final String destAdress;
        private SecondCallback callback;
        planRouteTask(String startAdress, String destAdress, SecondCallback callback){
            this.startAdress = startAdress;
            this.destAdress = destAdress;
            this.callback = callback;
        }
        @Override
        protected AsyncTaskResult doInBackground(String... params) {
        String urlString = HTTPS_MAPS_GOOGLEAPIS_COM_MAPS_API_DIRECTIONS_JSON +
                "?departure_time=now"+
                "&origins=" + this.startAdress +
                "&destinations=" + this.destAdress +
                "&key=" + API_KEY;

        Log.d(MAP_TAG, "Invoking URL " + urlString);
        AsyncTaskResult result = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                result = processOKConnection(connection);
                Log.d(MAP_TAG, result.getResultString());
            } else {
                Log.e(MAP_TAG, "HTTP REQUEST FAILED --> TRY AGAIN");
            }
        }
        catch (IOException | JSONException e){
            Log.e(MAP_TAG, e.toString());
        }
        //TODO: Set cognitiveLoad in UI

        return result;

    }

        @Override
        protected void onPostExecute(AsyncTaskResult result) {
            // This method runs on the UI thread before the background task starts.
            // You can perform setup or UI updates here.
            super.onPostExecute(result);
            if (callback != null) {
                callback.onCallback(result);
            }
        }

    }

    private static AsyncTaskResult processOKConnection(HttpURLConnection connection) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        final String jsonResponse = response.toString();
        //process JSONRESPONSE
        Log.d(MAP_TAG, MAP_CONTENT);
        Log.d(MAP_TAG, jsonResponse);

        long currentTime =0L, normalTime = 0L;
        JSONObject mapContent = new JSONObject(jsonResponse);

        JSONArray mapElement = mapContent.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
        for(int i=0; i < mapElement.length(); i++){
            JSONObject obj = mapElement.getJSONObject(i);

             currentTime += obj.getJSONObject("duration").getLong("value");
             normalTime += obj.getJSONObject("duration_in_traffic").getLong("value");
        }
        Log.d(MAP_TAG, "Current Time : " + String.valueOf(currentTime));
        Log.d(MAP_TAG, "Normal Time : " + String.valueOf(normalTime));

        String cogLoad = "HCW";
        if (currentTime <= normalTime)
            cogLoad = "LCW";

        return new AsyncTaskResult(cogLoad, currentTime, normalTime);

    }

}
