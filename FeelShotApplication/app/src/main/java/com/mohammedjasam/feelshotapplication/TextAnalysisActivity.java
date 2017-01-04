package com.mohsinhaider.feelshotapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class TextAnalysisActivity extends AppCompatActivity  {

    private String textToAnalyze;
    private String woo;
    private String confidence;
    private String feelingVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_analysis);

        Intent theIntent = getIntent();
        String toAnalyze = theIntent.getStringExtra("to_analyze");
        textToAnalyze = toAnalyze;
        Log.d("to_analyze", toAnalyze);

        new RetrieveFeedTask().execute("http://www.google.com");


        try {
            Thread.sleep(4000);
            TextView myView = (TextView) findViewById(R.id.textView11);
            myView.setText(confidence);

            TextView myNewView = (TextView) findViewById(R.id.textView10);
            myNewView.setText(feelingVal);
        }
        catch (Exception e) {

        }
    }

    class RetrieveFeedTask extends AsyncTask<String, URL, HttpURLConnection> {

        private Exception exception;

        @Override
        protected HttpURLConnection doInBackground(String... strings) {

            try {
                URL url = new URL("http://sentiment.vivekn.com/api/batch/");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);//5 secs
                connection.setReadTimeout(5000);//5 secs

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
//	    out.write(
//	            "[ " +
//	            "\"the fox jumps over the lazy dog\"," +
//	            "\"another thing here\" " +
//	            "]");

                out.write("[ " + " \" " + textToAnalyze + " \" " + "]");
                out.flush();
                out.close();

                int res = connection.getResponseCode();

                System.out.println(res);


                InputStream is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while((line = br.readLine() ) != null) {
                    Log.d("YYYYY", line);
                    woo = line;
                    woo = woo.replace("[", "");
                    woo = woo.replace("]", "");
                    Log.d("VAL", woo);

                    JsonElement jelement = new JsonParser().parse(woo);
                    JsonObject jobject = jelement.getAsJsonObject();
                    String result = jobject.get("confidence").toString();
                    Log.d("RESULT", result);
                    confidence = result;

                    JsonElement jelement2 = new JsonParser().parse(woo);
                    JsonObject jobject2 = jelement2.getAsJsonObject();
                    String result2 = jobject2.get("result").toString();
                    feelingVal = result2;
                }
                connection.disconnect();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
