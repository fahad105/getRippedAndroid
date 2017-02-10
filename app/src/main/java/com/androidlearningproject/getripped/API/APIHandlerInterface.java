package com.androidlearningproject.getripped.API;

/**
 * Created by FahadAli on 06-01-2017.
 */
import com.androidlearningproject.getripped.API.ResponseEntities.WeightEntry;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface APIHandlerInterface {
    @Headers({"Content-Type: application/json"})
    @GET("weight")
    Call<WeightEntry[]> getWeightEntries();

    @Headers({"Content-Type: application/json"})
    @GET("weight/{weightEntryId}")
    Call<WeightEntry> getWeightEntry(@Path("weightEntryId") int weightEntryId);

    @Headers({"Content-Type: application/json"})
    @POST("weight")
    Call<WeightEntry> createWeightEntry(@Body WeightEntry entry);

    @Headers({"Content-Type: application/json"})
    @DELETE("weight/{weightEntryId}")
    Call<ResponseBody> deleteWeightEntry(@Path("weightEntryId") int weightEntryId );

    @Headers({"Content-Type: application/json"})
    @PUT("weight/{weightEntryId}")
    Call<ResponseBody> editWeightEntry(@Body WeightEntry entry, @Path("weightEntryId") int weightEntryId);

}
