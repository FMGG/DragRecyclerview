package com.hangsu.dragrecyclerview.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.anarchy.classify.ClassifyView;
import com.hangsu.dragrecyclerview.R;
import com.hangsu.dragrecyclerview.logutils.LogUtil;

public class DataClassifyView extends ClassifyView {

    private View subContentView;

    public DataClassifyView(Context context) {
        super(context);
    }

    public DataClassifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DataClassifyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected Dialog createSubDialog() {
        Dialog dialog = new Dialog(getContext(), R.style.SubDialogStyle);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.dimAmount = 0.6f;
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.windowAnimations = R.style.CenterDialogAnimation;
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Override
    protected View getSubContent() {
        subContentView = inflate(getContext(), R.layout.extra_idata_sub_content, null);
        return subContentView;
    }
}
