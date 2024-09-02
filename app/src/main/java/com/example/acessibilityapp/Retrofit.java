package com.example.acessibilityapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface Retrofit {
    @Headers("Content-Type: application/json")
    @GET(".")
    Call<Void> sendSmsBody();
}
