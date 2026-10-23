package com.example.lin_new_project_kotlin_ai.fun;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewMethed {
    public static void init_VERTICAL(Context context, RecyclerView recyclerView, boolean hasFixedSize, int dividerHeight, int dividerColor, boolean enabled,boolean canScroll) {
        LinearLayoutManager ms = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false){
            @Override
            public boolean canScrollVertically() {
                return canScroll;//true:recyclerView本身可以滑動,false:recyclerView本身不可以滑動
            }
        };
        ms.setOrientation(LinearLayoutManager.VERTICAL);// 设置 recyclerview 布局方式为直
        recyclerView.setLayoutManager(ms);
        recyclerView.setHasFixedSize(hasFixedSize);
        recyclerView.addItemDecoration(new RecycleViewDivider(
                context, LinearLayoutManager.VERTICAL, dividerHeight,dividerColor));
        recyclerView.setNestedScrollingEnabled(enabled);//開關 RecyclerView的嵌套滑動特性
    }
    public static void init_VERTICAL2(Context context, RecyclerView recyclerView, boolean hasFixedSize, int dividerHeight, int dividerColor, boolean enabled,boolean canScroll) {
        LinearLayoutManager ms = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,true){//boolean reverseLayout  true為倒序
            @Override
            public boolean canScrollVertically() {
                return canScroll;//true:recyclerView本身可以滑動,false:recyclerView本身不可以滑動
            }
        };
        ms.setOrientation(LinearLayoutManager.VERTICAL);// 设置 recyclerview 布局方式为直
        recyclerView.setLayoutManager(ms);
        recyclerView.setHasFixedSize(hasFixedSize);
        recyclerView.addItemDecoration(new RecycleViewDivider(
                context, LinearLayoutManager.VERTICAL, dividerHeight,dividerColor));
        recyclerView.setNestedScrollingEnabled(enabled);//開關 RecyclerView的嵌套滑動特性
    }

    public static void init_HORIZONTAL(Context context, RecyclerView recyclerView, boolean hasFixedSize, int dividerHeight, int dividerColor, boolean enabled,boolean canScroll) {
        LinearLayoutManager ms = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false){
            @Override
            public boolean canScrollHorizontally() {
                return canScroll;//true:recyclerView本身可以滑動,false:recyclerView本身不可以滑動
            }
        };
        ms.setOrientation(LinearLayoutManager.HORIZONTAL);// 设置 recyclerview 布局方式为横
        recyclerView.setLayoutManager(ms);
        recyclerView.setHasFixedSize(hasFixedSize);
        recyclerView.addItemDecoration(new RecycleViewDivider(
                context, LinearLayoutManager.HORIZONTAL, dividerHeight,dividerColor));
        recyclerView.setNestedScrollingEnabled(enabled);//開關 RecyclerView的嵌套滑動特性
    }
    public static void init_GridLayoutManager(Context context, RecyclerView recyclerView, boolean hasFixedSize, int dividerHeight, int dividerColor,boolean enabled, int spanCount,boolean canScroll) {
        GridLayoutManager ms = new GridLayoutManager(context,spanCount){
            @Override
            public boolean canScrollHorizontally() {
                return canScroll;//true:recyclerView本身可以滑動,false:recyclerView本身不可以滑動
            }
        };

        recyclerView.setLayoutManager(ms);
//        recyclerView.setLayoutManager(new GridLayoutManager(context, spanCount));
        recyclerView.setHasFixedSize(hasFixedSize);
        recyclerView.addItemDecoration(new RecycleViewDivider(
                context, LinearLayoutManager.HORIZONTAL, dividerHeight,dividerColor));
        recyclerView.setNestedScrollingEnabled(enabled);//開關 RecyclerView的嵌套滑動特性
    }
}
