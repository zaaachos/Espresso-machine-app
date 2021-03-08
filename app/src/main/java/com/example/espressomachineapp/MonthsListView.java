package com.example.espressomachineapp;

import android.content.Context;
import android.media.Image;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.ArrayList;

public class MonthsListView extends BaseAdapter {

    private ArrayList<Months> monthsList;
    private Context context;

    public MonthsListView(Context contex, ArrayList<Months> monthsList) {
        this.monthsList = monthsList;
        this.context = contex;
    }

    @Override
    public int getCount() {
        return monthsList.size();
    }

    @Override
    public Object getItem(int position) {
        return monthsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.months_list_items, null);
        }

        ImageView icon = convertView.findViewById(R.id.coffee_mug);
        TextView name = convertView.findViewById(R.id.monthName);
        TextView amount = convertView.findViewById(R.id.janAmountId);

        Months monthItem = monthsList.get(position);

        icon.setImageResource(monthItem.getIcon());
        name.setText(monthItem.getName());
        amount.setText(monthItem.getAmount_consumed()+ " x");




        return convertView;
    }
}
