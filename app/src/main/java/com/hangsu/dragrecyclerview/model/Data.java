package com.hangsu.dragrecyclerview.model;

import android.graphics.Color;

public class Data {
    private boolean isChecked;
    private int color = Color.BLUE;
    private DataGroup mParent;

    private int img;

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public DataGroup getParent() {
        return mParent;
    }

    public void setParent(DataGroup parent) {
        mParent = parent;
    }
}
