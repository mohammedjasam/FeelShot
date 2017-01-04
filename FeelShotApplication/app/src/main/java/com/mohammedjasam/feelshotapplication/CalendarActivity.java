package com.mohsinhaider.feelshotapplication;

import android.content.Intent;
import android.graphics.LightingColorFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import java.util.Date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.text.SimpleDateFormat;

public class CalendarActivity extends AppCompatActivity {

    private String myUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Intent myIntent = getIntent();
        String userID = myIntent.getStringExtra("GoogleID");
        myUserID = userID;


        Button feelShotButton = (Button) findViewById(R.id.button2);
        feelShotButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));


        TextView myView = (TextView) findViewById(R.id.textView4);
//        myView.setText("11/05");

        DateFormat myFormat = new SimpleDateFormat("MM/dd");
        Date theDate = new Date();
        myView.setText(myFormat.format(theDate));
    }

    public void startImageAnalysis(View v) {
        Intent myIntent = new Intent(this, RecognizeActivity.class);
        myIntent.putExtra("googleID", myUserID);
        startActivity(myIntent);

    }
}
