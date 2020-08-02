package com.devhch.mirai.moviesapp_stage1.api;

import com.devhch.mirai.moviesapp_stage1.model.MoviesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created By Hamza Chaouki [MiraiDev].
 * On 7/5/2020
 */
public interface Service {

    @GET("movie/popular")
    Call<MoviesResponse> getPopularMovies(
            @Query("api_key") String apiKey);

    @GET("movie/top_rated")
    Call<MoviesResponse> getTopRatedMovies(
            @Query("api_key") String apiKey);
}

