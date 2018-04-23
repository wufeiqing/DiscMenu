package com.wufq.discmenu.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wufq.discmenu.R;
import com.wufq.discmenu.view.HPostilPieMenu;
import com.wufq.discmenu.view.PieItem;

import java.util.ArrayList;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by wufq on 2018/4/23.
 */

public class StartDiscMenuService extends Service {
    private static final String TAG = StartDiscMenuService.class.getSimpleName();
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private int mScreenWidth, mScreenHeight;
    private View mRootView;
    private View mOpenPostilView;
    private ImageView mOpenPostil;
    private HPostilPieMenu mPostilPieMenu;
    private final int mPieMenuWidth = 244;
    private final int mOuterSideMenuRadius = 31;
    private final int mMenuWidth = 90;
    private int mPaintNColor, mPaintPColor;
    private ArrayList<ImageView> mImgList;
    private ArrayList<TextView> mTextList;
    private int[] mFLImgTagArray;
    private String[] mFLTextNameArray;
    private SharedPreferences mSharedPreferences;
    // 圆盘 状态 0关闭 1功能圆盘 2批注圆盘
    private int mState_ClosePiemenu = 0;
    private int mState_FunctionPiemenu = 1;
    private int mState_PostilPiemenu = 2;
    private int mPiemenuState = -1;
    private boolean mShowBig = false;
    private boolean mShowSmall = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initParams();
        initView();
    }

    private void initView() {
        mRootView = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        mPostilPieMenu = new HPostilPieMenu(this);
        RelativeLayout linear = (RelativeLayout) mRootView.findViewById(R.id.main_layout);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mPieMenuWidth, mPieMenuWidth);
        mPostilPieMenu.setLayoutParams(lp);
        mPostilPieMenu.setCenter(mPieMenuWidth / 2, mPieMenuWidth / 2);
        mPostilPieMenu.setNormalPaint(mPaintNColor);
        mPostilPieMenu.setPressedPaint(mPaintPColor);

        mPostilPieMenu.setIconNameSize(8);
        mPostilPieMenu.setCenterMenuRadius(31);
//        mPostilPieMenu.setDismissPanelHandler(mSelectionPanelHandler);
//        mPostilPieMenu.setUpdateTouchAreaHandler(mUpdateTouchAreaHanlder);
        mPostilPieMenu.setSavePieLocationHandler(myHandler);
        mPostilPieMenu.setOutSideMenuRadius(mOuterSideMenuRadius);
        mPostilPieMenu.setMenuWidth(mMenuWidth);
        addItem(mPostilPieMenu);
        linear.addView(mPostilPieMenu);

        mOpenPostilView = LayoutInflater.from(this).inflate(R.layout.postil_open, null);
        mOpenPostil = (ImageView) mOpenPostilView.findViewById(R.id.open_postil);
        mOpenPostil.setBackgroundResource(R.drawable.postil_gaojiao_selector);
        mOpenPostil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bMove) {
                    return;
                }
                switchToFunctionPiemenu();
            }
        });
        mOpenPostil.setOnTouchListener(new QuickPostilTouchListener());

        if (!mShowSmall && !mShowBig) {
            mWindowManager.addView(mOpenPostilView, mParams);
            mShowSmall = true;
        }
        mPiemenuState = mState_ClosePiemenu;

        mPostilPieMenu.setOnEventListener(new HPostilPieMenu.OnEventListener() {
            @Override
            public void onTouchMove(float x, float y) {
                mParams.x += (int) (x);
                mParams.y += (int) (y);
                if (mParams.x <= -(mScreenWidth / 2 - mPostilPieMenu.getLayoutParams().width / 2)) {
                    mParams.x = -(mScreenWidth / 2 - mPostilPieMenu.getLayoutParams().width / 2);
                }

                if (mParams.x > (mScreenWidth / 2 - mPostilPieMenu.getLayoutParams().width / 2)) {
                    mParams.x = (mScreenWidth / 2 - mPostilPieMenu.getLayoutParams().width / 2);
                }
                if (mParams.y <= -(mScreenHeight / 2 - mPostilPieMenu.getLayoutParams().height / 2)) {
                    mParams.y = -(mScreenHeight / 2 - mPostilPieMenu.getLayoutParams().height / 2);
                }
                if (mParams.y > (mScreenHeight / 2 - mPostilPieMenu.getLayoutParams().height / 2)) {
                    mParams.y = (mScreenHeight / 2 - mPostilPieMenu.getLayoutParams().height / 2);
                }

                mWindowManager.updateViewLayout(mRootView, mParams);
            }

            @Override
            public void onItemClick(PieItem pieItem) {
                findItem(pieItem.getmPosition());
            }

            @Override
            public void onCenterClick() {
                if (mPiemenuState == mState_ClosePiemenu) {
                    return;
                }
                if (mPiemenuState == mState_FunctionPiemenu) {
                    mPostilPieMenu.setCenterBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.postil_center_pressed));

                    if (mShowBig) {
                        mWindowManager.removeView(mRootView);
                        mShowBig = false;
                    }
                    if (!mShowSmall) {
                        mWindowManager.addView(mOpenPostilView, mParams);
                        mShowSmall = true;
                    }
                    mPiemenuState = mState_ClosePiemenu;
                    mPostilPieMenu.setPieMenuState(mPiemenuState);
                }
            }

            @Override
            public void onItemLongClick(PieItem item) {

            }
        });
    }

    private final int Pos_0 = 0;
    private final int Pos_1 = 1;
    private final int Pos_2 = 2;
    private final int Pos_3 = 3;
    private final int Pos_4 = 4;
    private final int Pos_5 = 5;

    private void findItem(int position) {
        Observable.just(0,1,2,3,4,5).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                mPostilPieMenu.setPressedItem(0, integer, false);
            }
        });

//        setSelectItem(position,true);
        Toast.makeText(getBaseContext(),"第 "+position+" Item",Toast.LENGTH_SHORT).show();
        switch (position){
            case Pos_0:
                break;
            case Pos_1:
                break;
            case Pos_2:
                break;
            case Pos_3:
                break;
            case Pos_4:
                break;
            case Pos_5:
                break;
        }
    }

    /**
     * wufeiqing
     * @param position
     * @param isSelect
     */
    private void setSelectItem(int position,boolean isSelect){
        mImgList.get(position).setPressed(isSelect);
        mTextList.get(position).setPressed(isSelect);
        mPostilPieMenu.setPressedItem(0, position, isSelect);
        mPostilPieMenu.setSkin();
    }

    private void switchToFunctionPiemenu() {
        if (mPiemenuState != mState_ClosePiemenu) {
            return;
        }
        if (mShowSmall) {
            mWindowManager.removeView(mOpenPostilView);
            mShowSmall = false;
        }
        if (!mShowBig) {
            mWindowManager.addView(mRootView, mParams);
            mShowBig = true;
        }
        mPostilPieMenu.setCenterBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.postil_center_normal));

        mPiemenuState = mState_FunctionPiemenu;
        mPostilPieMenu.setPieMenuState(mPiemenuState);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("mPosX", mParams.x);
        editor.putInt("mPosY", mParams.y);
        editor.commit();
    }

    private void initParams() {
        mSharedPreferences = getApplicationContext().getSharedPreferences("position", MODE_PRIVATE);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
        mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.gravity = Gravity.CENTER;
        int pos_x = mSharedPreferences.getInt("mPosX", -800);
        int pos_y = mSharedPreferences.getInt("mPosY", 286);
        mParams.x = pos_x;
        mParams.y = pos_y;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
        mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();

        mPaintNColor = getResources().getColor(R.color.normal_paint_color);
        mPaintPColor = getResources().getColor(R.color.pressed_paint_color);
        mFLImgTagArray = new int[]{R.drawable.btn_whiteboard_2, R.drawable.btn_computer_2, R.drawable.btn_presenter_2,
                R.drawable.btn_eraser_2, R.drawable.btn_postil_2, R.drawable.btn_snapshot_2};

        mFLTextNameArray = new String[]{getStringValue(R.string.whiteboard)
                , getStringValue(R.string.computer), getStringValue(R.string.presenter)
                , getStringValue(R.string.eraser), getStringValue(R.string.postil)
                , getStringValue(R.string.snap_shot)};
    }

    private String getStringValue(int id) {
        return getResources().getString(id);
    }

    private void addItem(final HPostilPieMenu pie) {
        mImgList = new ArrayList<>();
        mTextList = new ArrayList<>();
        Observable.just(0,1,2,3,4,5).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                ImageView img = new ImageView(StartDiscMenuService.this);
                img.setImageResource(mFLImgTagArray[integer]);
                mImgList.add(img);

                TextView text = new TextView(StartDiscMenuService.this);
                text.setText(mFLTextNameArray[integer]);
                mTextList.add(text);

                pie.addItem(pie.makeItem(img, text, 0, mFLTextNameArray[integer]));
            }
        });
    }

    private boolean bMove = false;
    private PointF mOrignalPosition = new PointF(0, 0);
    private PointF mTouchDownPoint = new PointF(0, 0);
    private float mXDownInScreen;
    private float mYDownInScreen;
    private float mXInScreen;
    private float mYInScreen;
    // 移动方法
    class QuickPostilTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    bMove = false;
                    mOrignalPosition.x = mParams.x;
                    mOrignalPosition.y = mParams.y;
                    mTouchDownPoint.x = event.getRawX();
                    mTouchDownPoint.y = event.getRawY();
                    mXDownInScreen = event.getRawX();
                    mYDownInScreen = event.getRawY();
                case MotionEvent.ACTION_MOVE:
                    mXInScreen = event.getRawX();
                    mYInScreen = event.getRawY();
                    if (Math.abs(mYDownInScreen - mYInScreen) > 5 && Math.abs(mXInScreen - mXDownInScreen) > 5) {
                        bMove = true;
                        mParams.x = (int) (mOrignalPosition.x + (mXInScreen - mTouchDownPoint.x));
                        mParams.y = (int) (mOrignalPosition.y + (mYInScreen - mTouchDownPoint.y));

                        mYDownInScreen = mYInScreen;
                        mXDownInScreen = mXInScreen;

                        if (mParams.x <= -(mScreenWidth / 2 - mPostilPieMenu.getLayoutParams().width / 2)) {
                            mParams.x = -(mScreenWidth / 2 - mPostilPieMenu.getLayoutParams().width / 2);
                        }
                        if (mParams.x > (mScreenWidth / 2 - mPostilPieMenu.getLayoutParams().width / 2)) {
                            mParams.x = (mScreenWidth / 2 - mPostilPieMenu.getLayoutParams().width / 2);
                        }
                        if (mParams.y <= -(mScreenHeight / 2 - mPostilPieMenu.getLayoutParams().height / 2)) {
                            mParams.y = -(mScreenHeight / 2 - mPostilPieMenu.getLayoutParams().height / 2);
                        }
                        if (mParams.y > (mScreenHeight / 2 - mPostilPieMenu.getLayoutParams().height / 2)) {
                            mParams.y = (mScreenHeight / 2 - mPostilPieMenu.getLayoutParams().height / 2);
                        }

                        mWindowManager.updateViewLayout(mOpenPostilView, mParams);

                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //记录位置
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putInt("mPosX", mParams.x);
                    editor.putInt("mPosY", mParams.y);
                    editor.commit();
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    private final int SAVE_PIE_LOCATION = 0x900;
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SAVE_PIE_LOCATION:
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putInt("mPosX", mParams.x);
                    editor.putInt("mPosY", mParams.y);
                    editor.commit();
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPiemenuState != mState_ClosePiemenu) {
            if (mShowBig) {
                mWindowManager.removeView(mRootView);
                mShowBig = false;
            }
        } else {
            if (mShowSmall) {
                mWindowManager.removeView(mOpenPostilView);
                mShowSmall = false;
            }
        }
        mShowBig = false;
        mShowSmall = false;
    }
}
