package com.wufq.discmenu.view;

import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

/**
 * Pie menu item
 */
public class PieItem {

    private View mView;
    private int mLevel;

    private int mStart;
    private int mSweep;// 扫描
    private int mInner;
    private int mOuter;
    private boolean mPressed;
    private Path mPath;
    private PointF mCenter = null;
    private int mPosition;

    public PieItem(View view, int level, int position) {
        mView = view;
        mLevel = level;
        mPosition = position;
    }

    public int getmPosition() {
        return mPosition;
    }

    public void setmPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public void setPressed(boolean pressed) {
        mPressed = pressed;
        if (mView != null) {
            mView.setPressed(pressed);
        }
    }

    public boolean isPressed() {
        return mPressed;
    }

    public int getLevel() {
        return mLevel;
    }


    public void setCenter(PointF center) {
        mCenter = center;
    }

    // 浮点 x 坐标和 y 坐标点定义在二维平面中的有序的对
    public PointF getCenter() {
        return mCenter;
    }

    // 几何图形
    public void setGeometry(int start, int sweep, int inside, int outside, Path path) {
        mStart = start;
        mSweep = sweep;
        mInner = inside;
        mOuter = outside;
        mPath = path;
    }

    // 角度
    public int getStartAngle() {
        return mStart;
    }

    public int getSweep() {
        return mSweep;
    }

    // 内环半径
    public int getInnerRadius() {
        return mInner;
    }

    // 外环半径
    public int getOuterRadius() {
        return mOuter;
    }

    public View getView() {
        return mView;
    }

    public Path getPath() {
        return mPath;
    }

}
