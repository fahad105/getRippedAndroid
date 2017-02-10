package com.androidlearningproject.getripped.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.androidlearningproject.getripped.API.ResponseEntities.WeightEntry;
import com.androidlearningproject.getripped.R;

import java.util.ArrayList;

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

        TextView tvId = (TextView) convertView.findViewById(R.id.tvId);
        TextView tvValue = (TextView) convertView.findViewById(R.id.tvValue);
        TextView tvTimestamp = (TextView) convertView.findViewById(R.id.tvTimestamp);
        TextView tvRemark = (TextView) convertView.findViewById(R.id.tvRemark);

        tvId.setText("ID: "+entry.id);
        tvValue.setText("Weight: "+entry.value+"");
        tvTimestamp.setText("Timestamp: "+entry.timestamp);
        tvRemark.setText("Remark: "+entry.remark);

        return convertView;

    }


}
