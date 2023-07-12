package com.hangsu.dragrecyclerview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.anarchy.classify.simple.ChangeInfo;
import com.hangsu.dragrecyclerview.R;

public class DataGridLayout extends ViewGroup {
    private int mRowCount;
    private int mColumnCount;
    private int mGap;

    public DataGridLayout(Context context) {
        this(context, null);
    }

    public DataGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IDataGridLayout, defStyleAttr, 0);
        mRowCount = a.getInt(R.styleable.IDataGridLayout_iDataRowCount, 2);
        mColumnCount = a.getInt(R.styleable.IDataGridLayout_iDataColumnCount, 2);
        mGap = a.getDimensionPixelSize(R.styleable.IDataGridLayout_iDataGap, 0);
        a.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);


        if (widthMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.AT_MOST) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int suggestWidth = (widthSize - getPaddingLeft() - getPaddingRight() - (mColumnCount -1 )*mGap) / mColumnCount;
            int suggestHeight = (heightSize - getPaddingTop() - getPaddingBottom() - (mRowCount-1)*mGap) / mRowCount;
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                child.measure(MeasureSpec.makeMeasureSpec(suggestWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(suggestHeight, MeasureSpec.EXACTLY));
            }
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int left = getPaddingLeft() + (i%mColumnCount)*(child.getMeasuredWidth() + mGap);
            int top = getPaddingTop() + (i/mRowCount)*(child.getMeasuredHeight() + mGap);
            child.layout(left,top,left + child.getMeasuredWidth(),top + child.getMeasuredHeight());
        }
    }

    ChangeInfo getChangeInfo(){
        ChangeInfo info = new ChangeInfo();
        info.targetLeft = getPaddingLeft();
        info.targetTop = getPaddingTop();
        info.targetWidth = (getWidth() - getPaddingLeft() - getPaddingRight() - (mColumnCount -1 )*mGap)/mColumnCount;
        info.targetHeight = (getHeight() - getPaddingTop() - getPaddingBottom() - (mRowCount-1)*mGap)/mRowCount;
        return info;
    }

    /**
     * @hide
     */
    ChangeInfo getSecondItemChangeInfo(){
        ChangeInfo info = new ChangeInfo();
        info.targetWidth = (getWidth() - getPaddingLeft() - getPaddingRight() - (mColumnCount -1 )*mGap)/mColumnCount;
        info.targetHeight = (getHeight() - getPaddingTop() - getPaddingBottom() - (mRowCount-1)*mGap)/mRowCount;
        info.targetLeft = (int) (getPaddingLeft() + info.targetWidth + mGap);
        info.targetTop = getPaddingTop();
        return info;
    }


}
