package com.example.lin_new_project_kotlin_ai.viewBinding;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

public abstract class BaseAlertDialog <VB extends ViewBinding> extends AlertDialog {
    private VB binding;
    protected BaseAlertDialog(Context context) {
        super(context);
        binding = onCreateViewBinding(LayoutInflater.from(context));
        setView(binding.getRoot());
    }
    protected abstract VB onCreateViewBinding(@NonNull LayoutInflater layoutInflater);

    public VB getBinding() {
        return binding;
    }
}
