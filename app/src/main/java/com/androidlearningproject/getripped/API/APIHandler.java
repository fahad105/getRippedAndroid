package com.androidlearningproject.getripped.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Created by FahadAli on 06-01-2017.
 */

public class APIHandler {

    public static final String BASE_URL = "http://10.0.2.2:54303/api/"; //"https://jsonplaceholder.typicode.com/";
    public APIHandlerInterface apiService;

    public Retrofit retrofit;
    public APIHandler() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

//        Interceptor interceptor = new Interceptor() {
//            @Override
//            public okhttp3.Response intercept(Chain chain) throws IOException {
//
//                Request original = chain.request();
//
//                Request newRequest = chain.request().newBuilder()
//                        .addHeader("Content-Type", "application/json")
//                        .addHeader("Host", "localhost") // TODO: remove this when api is not on localhost anymore
//                        .build();
//
//                return chain.proceed(newRequest);
//            }
//        };
//
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        builder.interceptors().add(interceptor);
//        OkHttpClient client = builder.build();


        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
//                .client(client)
                .build();

        }


    public Retrofit getClient() {
        if (this.retrofit==null) {
            this.retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
