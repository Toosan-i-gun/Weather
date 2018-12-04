package com.weather.oneplus_w;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.weather.oneplus_w.util.Utility;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Utility.getWeather(this)!=null) {
            startActivity(new Intent(MainActivity.this,WeatherActivity.class));
            finish();
        }
    }
}
