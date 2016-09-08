package com.ahmedadeltito.retrofitmultiplerequests.service;

import com.ahmedadeltito.retrofitmultiplerequests.model.Model;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Ahmed Adel on 9/7/16.
 */
public interface WeatherService {

    @GET("weather")
    Observable<Model> getWeatherData(@Query("q") String countryName, @Query("appid") String appId);

}
