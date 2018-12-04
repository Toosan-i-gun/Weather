package com.weather.oneplus_w;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.weather.oneplus_w.gson.Weather;
import com.weather.oneplus_w.util.HttpUtil;
import com.weather.oneplus_w.util.NetWorkHelper;
import com.weather.oneplus_w.util.NetWorkRequestService;
import com.weather.oneplus_w.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity,titleUpdataTime,degreeText,weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText,pm25Text,comfortText,carWashText,sportText;
    private Weather weathers;
    private ImageView bingPic;
    private String imageUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        if (Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        initView();
    }

    private void initView() {
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdataTime = findViewById(R.id.title_updata_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm2_5_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        bingPic = findViewById(R.id.bing_pic_img);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        imageUrl = preferences.getString("bing_pic",null);
        if (imageUrl!=null) {
            Log.e("321","123");
            Glide.with(WeatherActivity.this).load(imageUrl).into(bingPic);
        }else {
            getImage();
        }

        if (Utility.getWeather(this)!=null) {
            showWeatherInfo(Utility.getWeather(this));
        }else {
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

    }
    public void getImage() {
        NetWorkRequestService netWorkRequestService;
        netWorkRequestService = NetWorkHelper.getInstance().getSersver();
        netWorkRequestService.getImage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                                (WeatherActivity.this).edit();
                        editor.putString("bing_pic",imageUrl);
                        editor.apply();
                        Glide.with(WeatherActivity.this).load(imageUrl).into(bingPic);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            imageUrl = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                });
    }

    private void requestWeather(final String weatherId) {
        NetWorkRequestService netWorkRequestService;
        netWorkRequestService = NetWorkHelper.getInstance().getSersver();
        netWorkRequestService.getWeather(weatherId,"11ee6829d65b45cfb864deffb53f8c81")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Weather>() {
                    @Override
                    public void onCompleted() {
                        Utility.putWeather(WeatherActivity.this,weathers);
                        showWeatherInfo(weathers);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Weather weather) {
                        weathers = weather;
                    }
                });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.getHeWeather().get(0).getBasic().getLocation();
        String updateTime = weather.getHeWeather().get(0).getUpdate().getUtc();
        String degree = weather.getHeWeather().get(0).getNow().getTmp()+"℃";
        String weatherInfo = weather.getHeWeather().get(0).getNow().getCond_txt();
        titleCity.setText(cityName);
        titleUpdataTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (int i = 0; i < weather.getHeWeather().get(0).getDaily_forecast().size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dataText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dataText.setText(weather.getHeWeather().get(0).getDaily_forecast().get(i).getDate());
            infoText.setText(weather.getHeWeather().get(0).getDaily_forecast().get(i).getCond().getTxt_d());
            maxText.setText(weather.getHeWeather().get(0).getDaily_forecast().get(i).getTmp().getMax());
            minText.setText(weather.getHeWeather().get(0).getDaily_forecast().get(i).getTmp().getMin());
            forecastLayout.addView(view);
        }
        if (weather.getHeWeather().get(0).getAqi()!=null) {
            aqiText.setText(weather.getHeWeather().get(0).getAqi().getCity().getAqi());
            pm25Text.setText(weather.getHeWeather().get(0).getAqi().getCity().getPm25());
        }
        String comfort = "舒适度："+weather.getHeWeather().get(0).getSuggestion().getComf().getTxt();
        String carWash = "洗车指数："+weather.getHeWeather().get(0).getSuggestion().getCw().getTxt();
        String sport = "运动指数："+weather.getHeWeather().get(0).getSuggestion().getSport().getTxt();
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }


}
