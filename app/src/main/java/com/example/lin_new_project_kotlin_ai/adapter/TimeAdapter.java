package com.example.lin_new_project_kotlin_ai.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.lin_new_project_kotlin_ai.R;
import com.example.lin_new_project_kotlin_ai.databinding.TimeItemBinding;
import com.example.lin_new_project_kotlin_ai.view.TimeView;

import java.util.ArrayList;
import java.util.HashMap;

public class TimeAdapter extends BaseAdapter {

    protected Context context;

    private ArrayList<String> arrayList;
    private String str_time,str_years;
    private TimeView timeView;
    private HashMap<String, Boolean> hashMap;
    public TimeAdapter(Context ctx, ArrayList<String> arrayList, String str_time, String str_years, HashMap<String, Boolean> hashMap) {
        super();
        this.context = ctx;
        this.hashMap=hashMap;

        this.str_time = str_time;
        this.str_years = str_years;
        this.arrayList = arrayList;

    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        TimeItemBinding binding=TimeItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,
                false);
        binding.tvT1.setText(arrayList.get(position));
        String mm=str_years+arrayList.get(position)+"日";
        if (mm.equals(str_time)){
            binding. imageT1.setImageDrawable(getDrawableByVERSION(R.drawable.time1));
            binding.tvT1.setTextColor(context.getResources().getColor(R.color.white));
        }else {
            try {
                if (hashMap.get(mm)){
                    binding.tvT1.setTextColor(context.getResources().getColor(R.color.black));
                    binding. imageT1.setImageDrawable(getDrawableByVERSION(R.drawable.time2));
                }
            }catch (Exception e){
                binding.tvT1.setTextColor(context.getResources().getColor(R.color.black));
                binding. imageT1.setImageDrawable(getDrawableByVERSION(R.drawable.time0));
                Log.d("日期X","Exception->"+e);
            }
        }
        return binding.getRoot();



    }
    private Drawable getDrawableByVERSION(int id) {
//會根據Android 版本來抓取圖片-----------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(id, context.getTheme());
        } else {
            return context.getResources().getDrawable(id);
        }
    }

    public String getStr_time() {
        return str_time;
    }

    public void setStr_time(String str_time) {
        this.str_time = str_time;
    }

    public String getStr_years() {
        return str_years;
    }

    public void setStr_years(String str_years) {
        this.str_years = str_years;
    }

    public HashMap<String, Boolean> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<String, Boolean> hashMap) {
        this.hashMap = hashMap;
    }
}
