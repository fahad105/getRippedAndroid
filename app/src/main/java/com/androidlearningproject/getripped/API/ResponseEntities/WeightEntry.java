package com.androidlearningproject.getripped.API.ResponseEntities;

import java.util.Date;

/**
 * Created by FahadAli on 06-01-2017.
 */

public class WeightEntry {
    int id;
    double value;
    Date timestamp;
    String remark;

    public int getId() {
        return id;
    }

    public double getValue() {
        return value;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getRemark() {
        return remark;
    }
}
