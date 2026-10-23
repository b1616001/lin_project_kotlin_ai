package com.example.lin_new_project_kotlin_ai.viewBinding;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

public abstract class BaseView<VB extends ViewBinding> extends LinearLayout {
    private VB binding;

    public BaseView(@NonNull Context context) {
        super(context);
        binding = onCreateViewBinding(LayoutInflater.from(context));

    }
    public BaseView(@NonNull Context context, @NonNull @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutParams layoutParams = new LayoutParams(context,attrs);
        layoutParams.width =MATCH_PARENT;
        layoutParams.height=MATCH_PARENT;
        binding = onCreateViewBinding(LayoutInflater.from(context));
        this.addView(binding.getRoot(),layoutParams);
    }
    protected abstract VB onCreateViewBinding(@NonNull LayoutInflater layoutInflater);

    public VB getBinding() {
        return binding;
    }

    public interface OnCustomDismissListener {
        void onDismiss();
    }
    public void setLog(String mes){
        Log.d(getClass().getSimpleName(),mes );
    }
}
