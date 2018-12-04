package com.weather.oneplus_w.util;


import com.weather.oneplus_w.gson.Weather;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * @author:Victory
 * @time:2017/12/26
 * @Email:949021037@qq.com
 * @QQ:949021037
 * @explain;
 */

public interface NetWorkRequestService {
    @GET("weather")
    Observable<Weather> getWeather(@Query("cityid") String cityid ,@Query("key") String key);

    @GET("bing_pic")
    Observable<ResponseBody> getImage();
}
