package com.example.lin_new_project_kotlin_ai.viewBinding;




import static com.example.lin_new_project_kotlin_ai.MainActivity.mainActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.example.lin_new_project_kotlin_ai.fun.ToastMethod;


public abstract class BaseBindingFragment<VB extends ViewBinding> extends Fragment {
    private VB binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //  binding = ViewBindingUtil.inflateWithGeneric(this, getLayoutInflater(), container, false);
        binding = onCreateViewBinding(getLayoutInflater());
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected abstract VB onCreateViewBinding(@NonNull LayoutInflater layoutInflater);

    public VB getBinding() {
        return binding;
    }

    @Override
    public void onResume() {
        mainActivity.currentFragment=getClass().getSimpleName();
        super.onResume();
//
//        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
////        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//
//        requireActivity().getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                        View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    public void setLog(String mes){
        Log.d(getClass().getSimpleName(),mes );
    }
    public  void showToast( String msg) {
        ToastMethod.showToast(getContext(), msg);
    }

}