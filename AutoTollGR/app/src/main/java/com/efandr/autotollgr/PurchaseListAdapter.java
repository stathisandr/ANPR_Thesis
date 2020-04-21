package com.efandr.autotollgr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PurchaseListAdapter extends ArrayAdapter<Purchase> {

    private static final String TAG = "PurchaseListAdapter";

    private Context mContext;
    private int mResource;
    private int lastPosition = -1;

    private static class ViewHolder{
        TextView day;
        TextView cost;
        TextView time;
    }

    public PurchaseListAdapter(Context context, int resource, ArrayList<Purchase> objects){
        super(context,resource,objects);
        mContext = context;
        mResource = resource;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        String day = getItem(position).getDay();
        float cost = getItem(position).getCost();
        String time = getItem(position).getTime();

        Purchase purchase = new Purchase(day,cost,time);

        final  View result;

        ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource,parent,false);
            holder = new ViewHolder();
            holder.day = (TextView) convertView.findViewById(R.id.purchaseday);
            holder.cost = (TextView) convertView.findViewById(R.id.purchaseprice);
            holder.time = (TextView) convertView.findViewById(R.id.purchasetime);

            result = convertView;

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext,(position>lastPosition)? R.anim.load_down_anim:R.anim.load_up_anim);
        result.startAnimation(animation);
        lastPosition = position;

        holder.day.setText(" "+purchase.getDay());
        holder.cost.setText(" "+String.valueOf(purchase.getCost())+"€");
        holder.time.setText(" "+purchase.getTime());

        return convertView;
    }
}
