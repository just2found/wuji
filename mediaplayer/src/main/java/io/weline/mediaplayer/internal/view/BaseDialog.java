package io.weline.mediaplayer.internal.view;

import android.app.Dialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

public abstract class BaseDialog extends Dialog {


    public BaseDialog(@NonNull Context context) {
        super(context);
        init(context);
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected void init(Context context) {
        View view = LayoutInflater.from(context).inflate(getLayoutId(), null);
        setContentView(view);

        getWindow().addFlags(Window.FEATURE_ACTION_BAR);

        initView(view);
    }

    protected abstract @LayoutRes
    int getLayoutId();

    protected abstract void initView(View view);

    protected int[] getViewLocation(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location;
    }

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}