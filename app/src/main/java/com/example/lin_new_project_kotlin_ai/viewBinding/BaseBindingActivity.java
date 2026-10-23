package com.example.lin_new_project_kotlin_ai.viewBinding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

public abstract class BaseBindingActivity<VB extends ViewBinding> extends AppCompatActivity {

    private VB binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = onCreateViewBinding(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    protected abstract VB onCreateViewBinding(@NonNull LayoutInflater layoutInflater);

    public VB getBinding() {
        return binding;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        fullScreenImmersive(binding.getRoot());
    }
    public void fullScreenImmersive(View view) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility(uiOptions);
    }

}