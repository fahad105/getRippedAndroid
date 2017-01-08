package com.androidlearningproject.getripped.API;

/**
 * Created by FahadAli on 06-01-2017.
 */
import com.androidlearningproject.getripped.API.ResponseEntities.WeightEntry;
import com.androidlearningproject.getripped.API.ResponseEntities.WeightEntryResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIHandlerInterface {
    @Headers({"Content-Type: application/json", "Host: localhost"})
    @GET("weight")
    Call<WeightEntryResponse> getWeightEntries();

    @Headers({"Content-Type: application/json", "Host: localhost"})
    @GET("weight/2")
    Call<WeightEntry> getWeightEntry();

}
