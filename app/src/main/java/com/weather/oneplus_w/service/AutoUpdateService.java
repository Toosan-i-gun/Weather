package com.weather.oneplus_w.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.weather.oneplus_w.WeatherActivity;
import com.weather.oneplus_w.gson.Weather;
import com.weather.oneplus_w.util.NetWorkHelper;
import com.weather.oneplus_w.util.NetWorkRequestService;
import com.weather.oneplus_w.util.Utility;

import java.io.IOException;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AutoUpdateService extends Service {

    private Weather weathers;
    private String imageUrl;

    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 1*60*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
        Intent intent1 = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,intent1,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }



    private void updateWeather() {
        if (Utility.getWeather(this)!=null) {
            String  weatherId = Utility.getWeather(this).getHeWeather().get(0).getBasic().getId();
            NetWorkRequestService netWorkRequestService;
            netWorkRequestService = NetWorkHelper.getInstance().getSersver();
            netWorkRequestService.getWeather(weatherId,"11ee6829d65b45cfb864deffb53f8c81")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Weather>() {
                        @Override
                        public void onCompleted() {
                            Utility.putWeather(AutoUpdateService.this,weathers);
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

    }

    private void updateBingPic() {
        NetWorkRequestService netWorkRequestService;
        netWorkRequestService = NetWorkHelper.getInstance().getSersver();
        netWorkRequestService.getImage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                                (AutoUpdateService.this).edit();
                        editor.putString("bing_pic",imageUrl);
                        editor.apply();
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
}
