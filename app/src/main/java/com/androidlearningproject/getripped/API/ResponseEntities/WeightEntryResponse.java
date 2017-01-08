package com.androidlearningproject.getripped.API.ResponseEntities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FahadAli on 06-01-2017.
 */

public class WeightEntryResponse {
    List<WeightEntry> entries;

    public WeightEntryResponse() {
        entries = new ArrayList<WeightEntry>();
    }

    public static WeightEntryResponse parseJSON(String response) {
        Gson gson = new GsonBuilder().create();
        WeightEntryResponse weightEntryResponse = gson.fromJson(response, WeightEntryResponse.class);
        return weightEntryResponse;
    }
}
