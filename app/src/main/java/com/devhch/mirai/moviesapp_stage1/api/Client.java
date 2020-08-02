package com.devhch.mirai.moviesapp_stage1.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created By Hamza Chaouki [MiraiDev].
 * On 7/5/2020
 */
public class Client {

    public static final String BASE_URL = "http://api.themoviedb.org/3/";
    public static Retrofit retrofit = null;

    public Retrofit getClient() {
        if ( retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

