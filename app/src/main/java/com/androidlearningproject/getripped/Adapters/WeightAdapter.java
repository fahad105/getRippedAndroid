package com.androidlearningproject.getripped.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.androidlearningproject.getripped.API.ResponseEntities.WeightEntry;
import com.androidlearningproject.getripped.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by FahadAli on 13-01-2017.
 */

public class WeightAdapter extends ArrayAdapter<WeightEntry> {
    public WeightAdapter(Context context, ArrayList<WeightEntry> entries) {
        super(context, 0,entries);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        WeightEntry entry = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_weight_entry, parent, false);
        }



//        TextView tvId = (TextView) convertView.findViewById(R.id.tvId);
        TextView tvValue = (TextView) convertView.findViewById(R.id.tvValue);
        TextView tvTimestamp = (TextView) convertView.findViewById(R.id.tvTimestamp);
        TextView tvRemark = (TextView) convertView.findViewById(R.id.tvRemark);

//        tvId.setText("ID: "+entry.id);
        tvValue.setText(entry.value+" kg");
        tvTimestamp.setText(DateFormat.format("MMMM dd, yyyy", entry.timestamp));
        tvRemark.setText(entry.remark != "" ? entry.remark : "No comment");

        return convertView;

    }


}
