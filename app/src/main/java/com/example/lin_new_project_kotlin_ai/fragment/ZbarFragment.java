package com.example.lin_new_project_kotlin_ai.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lin_new_project_kotlin_ai.databinding.FragmentZbarBinding;
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment;


public class ZbarFragment extends BaseBindingFragment<FragmentZbarBinding> {

    @Override
    protected FragmentZbarBinding onCreateViewBinding(@NonNull LayoutInflater layoutInflater) {
        return FragmentZbarBinding.inflate(layoutInflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {

    }
}