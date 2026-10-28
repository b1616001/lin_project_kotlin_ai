package com.example.lin_new_project_kotlin_ai.viewBinding;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.example.lin_new_project_kotlin_ai.General;

/**
 * 優化版 BasePopupWindow
 * - 支援 ViewBinding 自動綁定
 * - 平滑淡入/平移動畫 (使用硬體加速層及標準插值器)
 * - 解決原版在 dismiss() 時立即將 binding 置空導致的動畫期間 NullPointerException 競態問題
 * - 解決 duration 遞減導致重複彈窗動畫速度異常的缺陷
 * - 內建生命週期與 Activity 存活狀態檢查，避免 WindowManager BadTokenException
 * - 支援軟鍵盤自動調適 (SOFT_INPUT_ADJUST_RESIZE) 與焦點獲取
 */
public abstract class BasePopupWindow<VB extends ViewBinding> extends PopupWindow {

    protected VB binding;
    protected int orientation;
    protected View parentView;
    protected int duration;
    protected Context mContext;
    protected boolean isDark;
    public boolean isClearDark;
    protected int viewX, viewY;
    private boolean isDismissing = false;
    protected OnCustomDismissListener onCustomDismissListener;

    public interface OnCustomDismissListener {
        void onStartDismiss();
        void onDismiss();
    }

    /**
     * 簡易建構子：預設 WRAP_CONTENT, 300ms, FADE 動畫, 點擊外部可關閉, 啟用暗色背景
     */
    public BasePopupWindow(@NonNull Context context) {
        this(context, 300, 1, 1, General.FADE, true, true, false, false);
    }

    /**
     * 自訂寬高的建構子
     */
    public BasePopupWindow(@NonNull Context context, int width, int height) {
        this(context, 300, height, width, General.FADE, true, true, false, false);
    }

    /**
     * 完整建構子 (完全相容既有調用方式)
     *
     * @param context       Context
     * @param duration      動畫時間 (ms)
     * @param height        Window 高度 (0: MATCH_PARENT, 1: WRAP_CONTENT, 其他: dp)
     * @param width         Window 寬度 (0: MATCH_PARENT, 1: WRAP_CONTENT, 其他: dp)
     * @param animateType   動畫類型 (General.TRANSLATION_X, TRANSLATION_Y, FADE, SCALE_X, SCALE_Y, NONE)
     * @param isTouchCancel 是否點擊視窗外面關閉視窗
     * @param isDark        視窗後面背景是否變暗
     * @param isFloating    是否為系統浮動 OVERLAY 視窗
     * @param isClearDark   是否在關閉時清除暗色
     */
    public BasePopupWindow(
            @NonNull Context context,
            int duration,
            int height,
            int width,
            @General.animationType int animateType,
            boolean isTouchCancel,
            boolean isDark,
            boolean isFloating,
            boolean isClearDark
    ) {
        super(context);
        this.mContext = context;
        this.duration = Math.max(0, duration);
        this.orientation = animateType;
        this.isDark = isDark;
        this.isClearDark = isClearDark;

        binding = onCreateViewBinding(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        int w;
        if (width == 0) {
            w = MATCH_PARENT;
        } else if (width == 1) {
            w = WRAP_CONTENT;
        } else {
            w = dp2px(width);
        }

        int h;
        if (height == 0) {
            h = MATCH_PARENT;
        } else if (height == 1) {
            h = WRAP_CONTENT;
        } else {
            h = dp2px(height);
        }

        setWidth(w);
        setHeight(h);

        // 預設可獲取焦點與外部觸控
        setFocusable(true);
        setOutsideTouchable(isTouchCancel);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        if (isFloating) {
            setWindowLayoutType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }

        fullScreenImmersive(getContentView());
    }

    @NonNull
    protected abstract VB onCreateViewBinding(@NonNull LayoutInflater layoutInflater);

    @Nullable
    public VB getBinding() {
        return binding;
    }

    @Override
    public void showAsDropDown(View anchor, int xOff, int yOff, int gravity) {
        if (isHostActivityDead()) return;
        super.showAsDropDown(anchor, xOff, yOff, gravity);
        animateIn();
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (isHostActivityDead()) return;
        super.showAtLocation(parent, gravity, x, y);
        this.parentView = parent;
        this.viewX = x;
        this.viewY = y;
        animateIn();
    }

    private boolean isHostActivityDead() {
        if (mContext instanceof Activity) {
            Activity act = (Activity) mContext;
            return act.isFinishing() || act.isDestroyed();
        }
        return false;
    }

    protected void animateIn() {
        final View content = getContentView();
        if (content == null || duration <= 0 || orientation == General.NONE) return;

        content.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        switch (orientation) {
            case General.TRANSLATION_X:
                ObjectAnimator animTx = ObjectAnimator.ofFloat(content, "translationX", getWidth() > 0 ? getWidth() : 500f, 0f);
                animTx.setDuration(duration);
                animTx.setInterpolator(new DecelerateInterpolator());
                animTx.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        content.setLayerType(View.LAYER_TYPE_NONE, null);
                    }
                });
                animTx.start();
                break;
            case General.TRANSLATION_Y:
                ObjectAnimator animTy = ObjectAnimator.ofFloat(content, "translationY", getHeight() > 0 ? getHeight() : 500f, 0f);
                animTy.setDuration(duration);
                animTy.setInterpolator(new DecelerateInterpolator());
                animTy.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        content.setLayerType(View.LAYER_TYPE_NONE, null);
                    }
                });
                animTy.start();
                break;
            case General.SCALE_X:
                ObjectAnimator animSx = ObjectAnimator.ofFloat(content, "scaleX", 0.6f, 1f);
                animSx.setDuration(duration);
                animSx.setInterpolator(new DecelerateInterpolator());
                animSx.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        content.setLayerType(View.LAYER_TYPE_NONE, null);
                    }
                });
                animSx.start();
                break;
            case General.SCALE_Y:
                ObjectAnimator animSy = ObjectAnimator.ofFloat(content, "scaleY", 0.6f, 1f);
                animSy.setDuration(duration);
                animSy.setInterpolator(new DecelerateInterpolator());
                animSy.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        content.setLayerType(View.LAYER_TYPE_NONE, null);
                    }
                });
                animSy.start();
                break;
            case General.FADE:
            default:
                ObjectAnimator animAlpha = ObjectAnimator.ofFloat(content, "alpha", 0f, 1f);
                animAlpha.setDuration(duration);
                animAlpha.setInterpolator(new DecelerateInterpolator());
                animAlpha.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        content.setLayerType(View.LAYER_TYPE_NONE, null);
                    }
                });
                animAlpha.start();
                break;
        }
    }

    /**
     * 直接關閉 PopupWindow，無退出動畫
     */
    public void superDismiss() {
        try {
            super.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isDismissing = false;
            binding = null;
            parentView = null;
            if (onCustomDismissListener != null) {
                onCustomDismissListener.onDismiss();
            }
        }
    }

    @Override
    public void dismiss() {
        if (isDismissing) return;
        isDismissing = true;

        if (onCustomDismissListener != null) {
            onCustomDismissListener.onStartDismiss();
        }

        if (duration <= 0 || orientation == General.NONE) {
            superDismiss();
            return;
        }

        try {
            animateOut(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    superDismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            superDismiss();
        }
    }

    private void animateOut(final Animator.AnimatorListener listener) {
        final View content = getContentView();
        if (content == null) {
            listener.onAnimationEnd(null);
            return;
        }

        int exitDuration = Math.max(100, duration - 60);
        content.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        Animator.AnimatorListener internalListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                content.setLayerType(View.LAYER_TYPE_NONE, null);
                content.animate().setListener(null);
                listener.onAnimationEnd(animation);
            }
        };

        switch (orientation) {
            case General.TRANSLATION_X:
                content.animate()
                        .translationX(getWidth() > 0 ? getWidth() : 500f)
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(exitDuration)
                        .setListener(internalListener)
                        .start();
                break;
            case General.TRANSLATION_Y:
                content.animate()
                        .translationY(getHeight() > 0 ? getHeight() : 500f)
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(exitDuration)
                        .setListener(internalListener)
                        .start();
                break;
            case General.SCALE_X:
                content.animate()
                        .scaleX(0.1f)
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(exitDuration)
                        .setListener(internalListener)
                        .start();
                break;
            case General.SCALE_Y:
                content.animate()
                        .scaleY(0.1f)
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(exitDuration)
                        .setListener(internalListener)
                        .start();
                break;
            case General.FADE:
            default:
                content.animate()
                        .alpha(0f)
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(exitDuration)
                        .setListener(internalListener)
                        .start();
                break;
        }
    }

    public void setOnCustomDismissListener(OnCustomDismissListener onCustomDismissListener) {
        this.onCustomDismissListener = onCustomDismissListener;
    }

    public int dp2px(final float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    protected void fullScreenImmersive(View view) {
        if (view == null) return;
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility(uiOptions);
    }
}
