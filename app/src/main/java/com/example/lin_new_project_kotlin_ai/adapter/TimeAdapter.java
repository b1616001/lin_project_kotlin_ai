package com.example.lin_new_project_kotlin_ai.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.core.content.ContextCompat;

import com.example.lin_new_project_kotlin_ai.R;
import com.example.lin_new_project_kotlin_ai.databinding.TimeItemBinding;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 日曆天數適配器 (TimeAdapter)
 *
 * 優化重點：
 * 1. 採用 ViewHolder 與 ViewBinding 複用機制，避免每次 getView 重新 inflate 佈局造成卡頓。
 * 2. 使用 ContextCompat 安全載入 Drawable 與 Color，向下相容且避免記憶體與資源問題。
 * 3. 清楚區分「選取日期 (Selected)」、「活動/標記日期 (Event)」、「一般日期」與「空白格子」。
 */
public class TimeAdapter extends BaseAdapter {

    protected Context context;
    private ArrayList<String> arrayList;
    private String str_time;
    private String str_years;
    private HashMap<String, Boolean> hashMap;

    public TimeAdapter(Context ctx, ArrayList<String> arrayList, String str_time, String str_years, HashMap<String, Boolean> hashMap) {
        super();
        this.context = ctx;
        this.hashMap = hashMap != null ? hashMap : new HashMap<>();
        this.str_time = str_time != null ? str_time : "";
        this.str_years = str_years != null ? str_years : "";
        this.arrayList = arrayList != null ? arrayList : new ArrayList<>();
    }

    @Override
    public int getCount() {
        return arrayList != null ? arrayList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return arrayList != null && position < arrayList.size() ? arrayList.get(position) : "";
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        final TimeItemBinding binding;

        ViewHolder(TimeItemBinding binding) {
            this.binding = binding;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            TimeItemBinding binding = TimeItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            holder = new ViewHolder(binding);
            convertView = binding.getRoot();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String dayStr = arrayList.get(position);
        holder.binding.tvT1.setText(dayStr);

        if (dayStr == null || dayStr.trim().isEmpty()) {
            // 空白格子
            holder.binding.imageT1.setVisibility(View.INVISIBLE);
            holder.binding.tvT1.setText("");
            return convertView;
        }

        holder.binding.imageT1.setVisibility(View.VISIBLE);
        String fullDate = str_years + dayStr + "日";

        if (fullDate.equals(str_time)) {
            // 目前選取之日期：高亮顯著圓形背景與白字
            holder.binding.imageT1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.time1));
            holder.binding.tvT1.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            Boolean hasEvent = hashMap != null ? hashMap.get(fullDate) : null;
            if (Boolean.TRUE.equals(hasEvent)) {
                // 有排定行程或標記之日期
                holder.binding.imageT1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.time2));
                holder.binding.tvT1.setTextColor(ContextCompat.getColor(context, R.color.black));
            } else {
                // 一般日期
                holder.binding.imageT1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.time0));
                holder.binding.tvT1.setTextColor(ContextCompat.getColor(context, R.color.black));
            }
        }

        return convertView;
    }

    public String getStr_time() {
        return str_time;
    }

    public void setStr_time(String str_time) {
        this.str_time = str_time != null ? str_time : "";
    }

    public String getStr_years() {
        return str_years;
    }

    public void setStr_years(String str_years) {
        this.str_years = str_years != null ? str_years : "";
    }

    public HashMap<String, Boolean> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<String, Boolean> hashMap) {
        this.hashMap = hashMap != null ? hashMap : new HashMap<>();
    }

    public void updateData(ArrayList<String> newArrayList, String newStrTime, String newStrYears, HashMap<String, Boolean> newHashMap) {
        this.arrayList = newArrayList != null ? newArrayList : new ArrayList<>();
        this.str_time = newStrTime != null ? newStrTime : "";
        this.str_years = newStrYears != null ? newStrYears : "";
        this.hashMap = newHashMap != null ? newHashMap : new HashMap<>();
        notifyDataSetChanged();
    }
}
