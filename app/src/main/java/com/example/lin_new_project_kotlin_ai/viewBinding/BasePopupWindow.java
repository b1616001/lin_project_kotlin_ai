package com.example.lin_new_project_kotlin_ai.viewBinding;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.example.lin_new_project_kotlin_ai.General;


public abstract class BasePopupWindow<VB extends ViewBinding> extends PopupWindow {

    private VB binding;

    int orientation;
    protected View parentView;
    protected int duration;
    protected Context mContext;

    private final boolean isDark;
    public boolean isClearDark;

    protected int viewX, viewY;

   // protected View v;


    /**
     * @param context       context
     * @param duration      動畫持續時間
     * @param height        window高度
     * @param width         window寬度
     * @param animateType   動畫類型
     * @param isTouchCancel 是否點擊視窗外面關閉視窗
     * @param isDark        視窗後面的背景色 是否變暗
     * @param isFloating    是否為 OVERLAY視窗
     */
    public BasePopupWindow(Context context, int duration, int height, int width, @General.animationType int animateType, boolean isTouchCancel, boolean isDark, boolean isFloating, boolean isClearDark) {
        this.orientation = animateType;
        this.duration = duration;
        this.mContext = context;
        this.isDark = isDark;
        this.isClearDark = false;
        binding = onCreateViewBinding(LayoutInflater.from(context));
        setContentView(binding.getRoot());
//        View baseView = ((Activity) context).findViewById(android.R.id.content);
        int w, h;
        if (width == 0) {
            w = MATCH_PARENT;
        } else if (width == 1) {
            w = WRAP_CONTENT;
        } else {
            w = dp2px(width);
        }
        if (height == 0) {
            h = MATCH_PARENT;
        } else if (width == 1) {
            h = WRAP_CONTENT;
        } else {
            h = dp2px(height);
        }
        setWidth(w);
        setHeight(h);
        setFocusable(false);
        setOutsideTouchable(isTouchCancel);

//        setBackgroundDrawable(mWindowBackgroundDrawable);
        //  setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //這個方法是設置是否允許PopupWindow超出屏幕邊界，默認的，彈窗超出屏幕邊界是要被剪裁掉。如果傳入false,將允許彈窗顯示實際的（正確無誤）位置。
        // setClippingEnabled(false);
        //android 10 沒設定 TYPE_APPLICATION _OVERLAY ，下面會有空隙
        if (isFloating) {//浮動視窗，要有Window權限
            setWindowLayoutType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }

        fullScreenImmersive(getContentView());
        if (isDark){


        }

    }

    @Override
    public void showAsDropDown(View anchor, int xOff, int yOff, int gravity) {
        super.showAsDropDown(anchor, xOff, yOff, gravity);

        if (orientation == General.TRANSLATION_X) {
            ObjectAnimator.ofFloat(getContentView(), "translationX", getWidth(), 0).setDuration(duration).start();
        } else if (orientation == General.TRANSLATION_Y) {
            ObjectAnimator.ofFloat(getContentView(), "translationY", getHeight(), 0).setDuration(duration).start();
        } else {
            ObjectAnimator.ofFloat(getContentView(), "alpha", 0f, 1f).setDuration(600).start();
        }
    }


    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        parentView = parent;

//        if (isDark) setAlpha();

        switch (orientation) {
            case General.TRANSLATION_X:
                ObjectAnimator.ofFloat(getContentView(), "translationX", getWidth(), 0).setDuration(duration).start();
                break;
            case General.TRANSLATION_Y:
                ObjectAnimator.ofFloat(getContentView(), "translationY", getHeight(), 0).setDuration(duration).start();
                break;
            case General.FADE:
                ObjectAnimator.ofFloat(getContentView(), "alpha", 0f, 1f).setDuration(duration).start();
                break;
            case General.SCALE_X:
                ObjectAnimator.ofFloat(getContentView(), "scaleX", 0f, 1f).setDuration(duration).start();
                break;
            case General.SCALE_Y:
                ObjectAnimator.ofFloat(getContentView(), "scaleY", 0f, 1f).setDuration(duration).start();
                break;
            case General.NONE:
                break;
        }
        viewX = x;
        viewY = y;
    }

    protected void setAlpha() {
        ViewGroup viewGroup = (ViewGroup) parentView.getRootView();
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parentView.getWidth(), parentView.getHeight());
        dim.setAlpha(Math.round(255 * 0.6f));
        ViewGroupOverlay overlay = viewGroup.getOverlay();
        overlay.add(dim);
    }

    /**
     * Value of dp to value of px.
     *
     * @param dpValue The value of dp.
     * @return value of px
     */
    public int dp2px(final float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    protected OnCustomDismissListener onCustomDismissListener;

    /**
     * 直接關閉PopupWindow，沒有動畫效果
     */
    public void superDismiss() {
        super.dismiss();
        if (onCustomDismissListener != null) {
            onCustomDismissListener.onDismiss();
        }
    }

    @Override
    public void dismiss() {
        try {
            animateOut(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    superDismiss();
                }
            });
            if (onCustomDismissListener != null) {
                onCustomDismissListener.onStartDismiss();
            }
            binding = null;
            parentView = null;

//            if (isDark) LiveEventBus.get(SET_ALPHA_BACKGROUND).post(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateOut(final Animator.AnimatorListener listener) {

        if(duration != 0) duration = duration - 100;

        Animator.AnimatorListener animatorListenerAdapter = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onAnimationEnd(animation);
                getContentView().animate().setListener(null);
            }
        };

        switch (orientation) {
            case General.TRANSLATION_X:
                getContentView().animate().translationX(getWidth()).setListener(animatorListenerAdapter).setDuration(duration).start();
                break;
            case General.TRANSLATION_Y:
                getContentView().animate().translationY(getHeight()).setListener(animatorListenerAdapter).setDuration(duration).start();
                break;
            case General.FADE:
                getContentView().animate().alpha(0.1f).setListener(animatorListenerAdapter).setDuration(duration).start();
                break;
            case General.SCALE_X:
                getContentView().animate().scaleX(0.1f).setListener(animatorListenerAdapter).setDuration(duration).start();
                break;
            case General.SCALE_Y:
                getContentView().animate().scaleY(0.1f).setListener(animatorListenerAdapter).setDuration(duration).start();
                break;
            case General.NONE:
                listener.onAnimationEnd(null);
                break;
        }

    }

    public void setOnCustomDismissListener(OnCustomDismissListener onCustomDismissListener) {
        this.onCustomDismissListener = onCustomDismissListener;
    }

    public interface OnCustomDismissListener {

        void onStartDismiss();

        void onDismiss();
    }



    protected abstract VB onCreateViewBinding(@NonNull LayoutInflater layoutInflater);
    public VB getBinding() {
        return binding;
    }


    protected void fullScreenImmersive(View view) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility(uiOptions);
    }


}
