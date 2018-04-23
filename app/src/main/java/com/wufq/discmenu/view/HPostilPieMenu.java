package com.wufq.discmenu.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.wufq.discmenu.R;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("DrawAllocation")
public class HPostilPieMenu extends View {
	
    private static final int CIRCLE_ANGLE = 360;
    private static final int LEVEL_FIRST_MENUCOUNT = 6;
    private static final int LEVEL_SECOND_MENUCOUNT = 8;
    private static final int LEVEL_THIRD_MENUCOUNT = 10;
    private static final int MAX_LEVEL = 3;//最多3级菜单  
    private  int mOutSideMenuRadius ;//外部菜单半径
    private  int mCenterMenuRadius ;//中心菜单的半径
    private  int mMenuWidth;//每一级菜单的宽度
    private Point mCenter;
    private OnEventListener mItemEventListener = null;
    private List<List<PieItem>> mItemList;
    private Paint mNormalPaint;//普通
    private Paint mPressedPaint;//按下
    private int [] mLevel1ItemIcons;//一级菜单图标
    private int [] mLevel2ItemIcons;//二级菜单图标
    private int [] mLevel3ItemIcons;//三级菜单图标
    private int mItemIconSize;//图片尺寸
    private int mItemNameSize;//文字大小
    private String[] mLevel1ItemNames;//一级图标名字
    private String[] mLevel2ItemNames;//二级图标名字
    private String[] mLevel3ItemNames;//三级图标名字
    private int mItemNameColor;//图标名字颜色
    private int mLevel1MenuCount;//一级菜单个数
    private int mLevel2MenuCount;//二级菜单个数
    private int mLevel3MenuCount;//三级菜单个数
    private int mIntervalColor;//分割线颜色
    private Context mContext;
    private Bitmap mCenterBitmap = null;
    private PieItem mLastTouchItem = null;
    private PieItem mThisTouchItem = null;
    private boolean mIsLayoutItems = false;
    private boolean mCenterPressed = false;
    private boolean mIsTouchMove = false;
    private boolean mCreateWay = false;
    private long mLastClickTime = -1L;
    
    public HPostilPieMenu(Context context) {
        this(context,null);
        mCreateWay = true;
    }
    
    public HPostilPieMenu(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HPostilPieMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        mContext = context;
        if(!mCreateWay){
        	TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.PieMenu);
            //一级菜单个数  二级菜单  三级菜单
            mLevel1MenuCount = typeArray.getInt(R.styleable.PieMenu_Menu1Count, 0);
            mLevel2MenuCount = typeArray.getInt(R.styleable.PieMenu_Menu2Count, 0);
            mLevel3MenuCount = typeArray.getInt(R.styleable.PieMenu_Menu3Count, 0);
            //每一级菜单个数
            if(mLevel1MenuCount >= LEVEL_FIRST_MENUCOUNT){
            	mLevel1MenuCount = LEVEL_FIRST_MENUCOUNT;
            }
            if(mLevel2MenuCount >= LEVEL_SECOND_MENUCOUNT){
            	mLevel2MenuCount = LEVEL_SECOND_MENUCOUNT;
            }
            if(mLevel3MenuCount >= LEVEL_THIRD_MENUCOUNT){
            	mLevel3MenuCount = LEVEL_THIRD_MENUCOUNT;
            }
            
            //中心彩菜单半径  
            mCenterMenuRadius = (int) typeArray.getDimension(R.styleable.PieMenu_CenterMenuRadius, 50);
            //外部菜单半径
            mOutSideMenuRadius = (int) typeArray.getDimension(R.styleable.PieMenu_OutSideMenuRadius, 60);
            //菜单宽度
            mMenuWidth = typeArray.getDimensionPixelOffset(R.styleable.PieMenu_MenuWidth, 80);
            //中心图片
            Drawable drawable =
            getResources().getDrawable(typeArray.getResourceId(R.styleable.PieMenu_CenterIcon, R.drawable.function_center_pressed));
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            mCenterBitmap = bitmapDrawable.getBitmap();
            //一级菜单图标 二级菜单 三级菜单
            // 一级图标 二级图标 三级图标文字
            if(mLevel1MenuCount >= 1){
            	TypedArray intar1 = context.getResources().obtainTypedArray(typeArray.getResourceId(R.styleable.PieMenu_Menu1Icon,R.array.defaultIcon));
            	mLevel1ItemIcons = new int [mLevel1MenuCount];
            	 for(int i = 0;i < mLevel1MenuCount;i++){
                 	if(i < intar1.length()){
    					mLevel1ItemIcons[i] = intar1.getResourceId(i, -1);
                 	}
                 	else{
                 		mLevel1ItemIcons[i] = -1;
                 	}
                }
            	TypedArray strar1 = context.getResources().obtainTypedArray(typeArray.getResourceId(R.styleable.PieMenu_Menu1IconName,R.array.defaultIconName));
                mLevel1ItemNames = new String[mLevel1MenuCount];
                for(int i = 0;i < mLevel1MenuCount;i++){
                	if(i < strar1.length()){
    					mLevel1ItemNames[i] = strar1.getString(i);
                	}
                	else{
                		mLevel1ItemNames[i] = "";
                	}
                 }  
                intar1.recycle();
                strar1.recycle();
            }
            
            if(mLevel2MenuCount >= 1){
            	TypedArray intar2 = context.getResources().obtainTypedArray(typeArray.getResourceId(R.styleable.PieMenu_Menu2Icon,R.array.defaultIcon));
            	
            	mLevel2ItemIcons = new int [mLevel2MenuCount];
            	for(int i = 0;i < mLevel2MenuCount;i++){
                 	if(i < intar2.length()){
    					mLevel2ItemIcons[i] = intar2.getResourceId(i, -1);
                 	}
                 	else{
                 		mLevel2ItemIcons[i] = -1;
                 	}
                }
    			TypedArray strar2 = context.getResources().obtainTypedArray(typeArray.getResourceId(R.styleable.PieMenu_Menu2IconName, R.array.defaultIconName));
    			mLevel2ItemNames = new String[mLevel2MenuCount];
    			for(int i = 0;i < mLevel2MenuCount;i++){
                	if(i < strar2.length()){
    					mLevel2ItemNames[i] = strar2.getString(i);
                	}
                	else{
                		mLevel2ItemNames[i] = "";
                	}
                 }
    			intar2.recycle();
    			strar2.recycle();
            }
            if(mLevel3MenuCount >= 1){
            	TypedArray intar3 = context.getResources().obtainTypedArray(typeArray.getResourceId(R.styleable.PieMenu_Menu3Icon,R.array.defaultIcon));
            	mLevel3ItemIcons = new int [mLevel3MenuCount];
            	for(int i = 0;i < mLevel3MenuCount;i++){
                 	if(i < intar3.length()){
    					mLevel3ItemIcons[i] = intar3.getResourceId(i, -1);
                 	}
                 	else{
                 		mLevel3ItemIcons[i] = -1;
                 	}
                }
            	TypedArray strar3 = context.getResources().obtainTypedArray(typeArray.getResourceId(R.styleable.PieMenu_Menu3IconName,R.array.defaultIconName));
            	mLevel3ItemNames = new String[mLevel3MenuCount];
            	for(int i = 0;i < mLevel3MenuCount;i++){
                	if(i < strar3.length()){
    					mLevel3ItemNames[i] = strar3.getString(i);
                	}
                	else{
                		mLevel3ItemNames[i] = "";
    					
                	}
                 }
            	intar3.recycle();
            	strar3.recycle();
            }
            //图标大小
            mItemIconSize = (int) typeArray.getDimension(R.styleable.PieMenu_ItemIconSize, 25);
            //文字大小
            mItemNameSize = (int) typeArray.getDimension(R.styleable.PieMenu_ItemNameSize, 10);
            //分割线颜色
            mIntervalColor = typeArray.getColor(R.styleable.PieMenu_IntervalColor, Color.WHITE);
            //图标文字颜色
            mItemNameColor = typeArray.getColor(R.styleable.PieMenu_IconNameColor, Color.BLACK);
            //笔颜色(未选中 normal pressed)
            mNormalPaint.setColor(typeArray.getColor(R.styleable.PieMenu_NormalPaint, Color.BLUE));
            mPressedPaint.setColor(typeArray.getColor(R.styleable.PieMenu_PressedPaint, Color.RED));
            typeArray.recycle();
            initPieMenu() ;  
        }
        
       
        
    }
    /**
     * 中心图片资源缩放
     * @param bitmap
     * @return 缩放后的图片资源
     */
    private Bitmap scaleBitmap(Bitmap bitmap) {
    	  Matrix matrix = new Matrix();
    	  float scale = ((float)mCenterMenuRadius-2)/(float)bitmap.getHeight();
    	  matrix.postScale(scale,scale); //长和宽放大缩小的比例
    	  Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    	  matrix.reset();
    	 // bitmap.recycle();
    	  return resizeBmp;
		
	}
    /**
     *  初始化自定义属性获取的参数 添加每一级菜单
     */
	
    private void initPieMenu() {
    	
//        // 初始化一级菜单
//		for (int i = 0; i < mLevel1MenuCount; i++) {
//			addItem(makeItem(mLevel1ItemIcons[i], 0, mLevel1ItemNames[i]));
//		}
//		// 初始化二级菜单
//		if(mLevel1MenuCount>=1){
//			for (int i = 0; i < mLevel2MenuCount; i++) {
//				addItem(makeItem(mLevel2ItemIcons[i], 1, mLevel2ItemNames[i]));
//			}
//			
//		}
//		// 初始化三级菜单
//		if(mLevel2MenuCount>=1){
//			for (int i = 0; i < mLevel3MenuCount; i++) {
//				addItem(makeItem(mLevel3ItemIcons[i], 2, mLevel3ItemNames[i]));
//			}
//			
//		}

	}
    
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		 //保存当前视图view的宽度和高度
		 setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		 setCenter(getMeasuredWidth()/2, getMeasuredHeight()/2); 
		 if(mCenterBitmap.getHeight()>mCenterMenuRadius){
  			//centerBitmap=scaleBitmap(centerBitmap);
  		 }
		 setCenterBitmap(mCenterBitmap);
		  
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	/**
	 * 设置控件的响应监听事件
	 * @param listener 圆盘响应监听
	 */
	public void setOnEventListener(OnEventListener listener){
    	mItemEventListener = listener;
    }
    /**
     * 初始化所需要的参数
     */
    private void init(Context Context) {
        mItemList = new ArrayList<List<PieItem>>();
        setWillNotDraw(false);
        setDrawingCacheEnabled(false);
        mCenter = new Point(0, 0);
        mNormalPaint = new Paint();
        mNormalPaint.setAntiAlias(true);
        mPressedPaint = new Paint();
        mPressedPaint.setAntiAlias(true);
       
    }
  /** 
   *  初始化每一级菜单
   *  @param item 菜单 <br>
   *  需要调用方法 makeItme来创建一个菜单实例
   * */
    public void addItem(PieItem item) {
        int level = item.getLevel();
        if (level >= MAX_LEVEL) {
            level = MAX_LEVEL;
        }
        
        while (mItemList.size() <= level) {
            mItemList.add(new ArrayList<PieItem>());
        }
        mItemList.get(level).add(item);
    
    }

    public void setPressedItem(int level, int index, boolean pressed) {
    	
        if (level <= MAX_LEVEL) {
            if (index >= 0 && index < mItemList.get(level).size()) {
                mItemList.get(level).get(index).setPressed(pressed);
                mThisTouchItem = mItemList.get(level).get(index);
            }
        }
    }
    /**
     * 设置控件的位置
     * @param x 圆心在布局中的x轴坐标
     * @param y 圆心在布局中的y轴坐标
     */
    public void setCenter(int x, int y) {
        if (mCenter == null) {
            mCenter = new Point(x, y);
        } else {
            mCenter.x = x;
            mCenter.y = y;
        }
    }
    /**
     * 角度转化为弧度
     * @param angle 角度
     * @return 弧度
     */
    private double angle2arc(int angle) {
        return (angle * Math.PI / 180);
    }
    /**
     * 弧度转化为角度
     * @param arc 弧度
     * @return 角度
     */
    private int arc2angle(double arc) {
        return (int) (arc * 180 / Math.PI);
    }

    /**
     * 绘制外层圆弧 
     * @param startAngle 开始角度
     * @param endAngle   结束角度
     * @param inner      内层大小
     * @param outer      外层大小
     * @param center     中心
     * @return 圆弧
     */
    private Path makePath(int startAngle, int endAngle, int inner, int outer, Point center) {
        
        Path path = new Path();
        // inner  middleradius
        RectF innerRect = new RectF(center.x - inner, center.y - inner,
                center.x + inner, center.y + inner);
        RectF outerRect = new RectF(center.x - outer, center.y - outer,
                center.x + outer, center.y + outer);
        path.arcTo(innerRect, startAngle, endAngle - startAngle, true);
        path.arcTo(outerRect, endAngle, startAngle - endAngle, false);
        path.close();
        return path;
    }
    /**
     * 绘制中心圆弧
     * @param center 中心
     * @return  圆弧
     */
    private Path makePath(Point center) {
        Path path = new Path();
        RectF innerRect = new RectF(center.x - mCenterMenuRadius/2, center.y - mCenterMenuRadius/2,
                center.x + mCenterMenuRadius/2, center.y + mCenterMenuRadius/2);
        path.arcTo(innerRect,0 ,360,true);
        path.close();
        return path;
    }
    /**
     * 绘制控件分割线
     * @param canvas
     */
    private void layoutInterval(Canvas canvas){
    	
        int intervalAngle = 0;//item的间隔
        int inner = mOutSideMenuRadius;//
        
        int outer = mOutSideMenuRadius + mMenuWidth;//
        int incInterval = 0; 
        int startAngleInterval = 0;
        int levelStartAngle = 0;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(mIntervalColor);
        
        paint.setStrokeWidth(2);
        paint.setXfermode(new PorterDuffXfermode(Mode.XOR));
        for (List<PieItem> list : mItemList) {
            int itemAngle = CIRCLE_ANGLE / list.size() - intervalAngle;
            int startAngle = levelStartAngle;
            //TODO
            if(list.size() == 3){
            	startAngle = levelStartAngle+90/3;
            }
            else if(list.size() == 5){
            	startAngle = levelStartAngle+90/5;
            }
            else if(list.size() == 7){
            	startAngle = levelStartAngle+90/7;
            }
            else if(list.size() == 9){
            	startAngle = levelStartAngle+90/9;
            }
           
            for (PieItem item : list) {
            	
                Point p1 = new Point((int) (mCenter.x+inner* Math.cos(startAngle* Math.PI/180)), (int) (mCenter.y+inner* Math.sin(startAngle* Math.PI/180)));
                Point p2 = new Point((int) (mCenter.x+outer* Math.cos(startAngle* Math.PI/180)), (int) (mCenter.y+outer* Math.sin(startAngle* Math.PI/180)));
                int state = canvas.save();
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y,paint );
                canvas.restoreToCount(state);
                startAngle += itemAngle + intervalAngle;
            }
            inner += mMenuWidth + incInterval;
            outer += mMenuWidth + incInterval;
            levelStartAngle += startAngleInterval;
        }
    }
   /**
    *  布局Item的位置在圆环中心
    */
    private void layoutItems(Canvas canvas) {
        int intervalAngle = 0;//item的间隔
        
        int inner = mOutSideMenuRadius;//
        int outer = mOutSideMenuRadius + mMenuWidth;//
        int incInterval = 0; //每一级菜单中间间隔
        int startAngleInterval = 0;
        int levelStartAngle = 0;
        for (List<PieItem> list : mItemList) {
            int itemAngle = CIRCLE_ANGLE / list.size() - intervalAngle;
            
            int startAngle = levelStartAngle;
            if(list.size() == 3){
            	startAngle = levelStartAngle+90/3;
            }
            else if(list.size() == 5){
            	startAngle = levelStartAngle+90/5;
            }
            else if(list.size() ==7 ){
            	startAngle = levelStartAngle+90/7;
            }
            else if(list.size() == 9){
            	startAngle = levelStartAngle+90/9;
            }
            for (PieItem item : list) {
                View view = item.getView();
                view.measure(view.getLayoutParams().width,
                        view.getLayoutParams().height);
                int w = view.getMeasuredWidth();
                int h = view.getMeasuredHeight();
                //show in center
                int r = inner + (outer - inner) / 2;
                double arc = angle2arc(startAngle + itemAngle / 2);
                int x = mCenter.x + (int) (r * Math.cos(arc)) - w / 2;
                int y = mCenter.y + (int) (r * Math.sin(arc)) - h / 2;
                view.layout(x, y, x + w, y + h);
                Path path = makePath(startAngle, startAngle + itemAngle, inner, outer, mCenter);
                item.setGeometry(startAngle, itemAngle, inner, outer, path);
                startAngle += itemAngle + intervalAngle;
            }
            inner += mMenuWidth + incInterval;
            outer += mMenuWidth + incInterval;
            levelStartAngle += startAngleInterval;
        }

    }
    /**
     * 发现点击的菜单
     * @param x touch x坐标
     * @param y touch y坐标
     * @return 需要响应的具体菜单
     */
    private PieItem findItem(float x, float y) {
        //将 (0,0)点置于控件中心
        x -= mCenter.x;
        y -= mCenter.y;
        int r = (int) Math.sqrt(x * x + y * y);
        int angle = arc2angle(Math.atan2(y, x));
        
        angle %= 360;
        if (angle < 0) {
            angle += 360;
        }
        for (List<PieItem> list : mItemList) {
            for (PieItem item : list) {
                if (isPointInItem(r, angle, item)) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * 是否点击了菜单
     *
     * @param r     touch点到控件中心的坐标
     * @param angle 角度 [0, 360)
     * @param item  菜单
     * @return true 是菜单上的point otherwise false
     */
    private boolean isPointInItem(int r, int angle, PieItem item) {
        if (item.getInnerRadius() <= r && item.getOuterRadius() >= r) {
//            while (angle + CIRCLE_ANGLE <= item.getStartAngle() + item.getSweep()) {
//                angle += CIRCLE_ANGLE;
//                Log.e("ondraw", "while 循环angle"+angle);
//            }
            if (item.getStartAngle() <= angle && item.getStartAngle() + item.getSweep() >= angle) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private int mLastTouchX;
    private int mLastTouchY;
    private Boolean mIsCent;
    private float xDownInScreen,yDownInScreen,xInScreen,yInScreen;
    private Runnable mLongPressRunnable = new Runnable() {
        
        @Override
        public void run() {
            if(!mIsTouchMove && mLastTouchItem != null){
            	if(mLastTouchItem.getmPosition() != 3 && mLastTouchItem.getmPosition() != 4
                        && mLastTouchItem.getmPosition() != 2){
                    mLastTouchItem.setPressed(false);
                }
            	mItemEventListener.onItemLongClick(mLastTouchItem);
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        int x = (int) evt.getX();
        int y = (int) evt.getY();
        Point touchPoint = new Point();
        touchPoint.set(x, y);
        int action = evt.getActionMasked();
        if (MotionEvent.ACTION_DOWN == action) {
            xDownInScreen = evt.getRawX();
            yDownInScreen = evt.getRawY();
            
            mLastTouchX = x;
            mLastTouchY = y;
            mIsTouchMove = false;
            mLastTouchItem = findItem(x, y);
            mIsCent = isCenter(mCenter, touchPoint);
            //发送更新全屏触控区域的消息
            if(mLastTouchItem != null || mIsCent ){
                if(mUpdateTouchAreaHandler != null){
                	mUpdateTouchAreaHandler.sendEmptyMessage(UPDATE_TOUCH_AREA_FULLSCREEN);
                }
            }
            if (mLastTouchItem != null) {
                mLastTouchItem.setPressed(true);
				if (mPiemenuState == mState_PostilPiemenu) {

					if (mLastTouchItem != mThisTouchItem) {
                        if (mSelectionPanelHandler != null) {
                            mSelectionPanelHandler.sendEmptyMessage(DISMISS_PANEL);
                        }
						mLastTouchItem.setPressed(true);
						if (mThisTouchItem != null) {
							mThisTouchItem.setPressed(false);
						}
					} else {
                        if (mSelectionPanelHandler != null) {
                            if(mLastTouchItem.getmPosition() != 1){
                                mSelectionPanelHandler.sendEmptyMessage(SELECTED_PANEL);
                            }
                        }
                    }

				} else if(mPiemenuState == mState_FunctionPiemenu){ //add by wufq
                    if (mLastTouchItem != mThisTouchItem) {
                        if (mSelectionPanelHandler != null) {
                            mSelectionPanelHandler.sendEmptyMessage(DISMISS_PANEL);
                            if (mThisTouchItem != null) {
                                mThisTouchItem.setPressed(false);
                            }
                        }
                    }
                }
                invalidate();
            } else {
                if (mIsCent) {
                    mCenterPressed = true;
                    invalidate();
                    return true;
                }
                return false;
            }
            if(mPiemenuState == mState_PostilPiemenu){
            	if (mLastTouchItem != null) {
            		postDelayed(mLongPressRunnable, 500);
            	}
            }
            return true;
        } else if (MotionEvent.ACTION_UP == action) {
        	//发送更新局部触控区域的消息
            if(mUpdateTouchAreaHandler != null){
            	mUpdateTouchAreaHandler.sendEmptyMessage(UPDATE_TOUCH_AREA_RECT);
            }
            if(mSavePieLocationHandler != null){
                mSavePieLocationHandler.sendEmptyMessage(SAVE_PIE);
            }
        	if(mPiemenuState == mState_FunctionPiemenu){
        		if (mLastTouchItem != null) {
                    mLastTouchItem.setPressed(false);
                    invalidate();
        		}
        	}
            if (mIsCent) {
                mCenterPressed = false;
                invalidate();
            }
            if (!mIsTouchMove) {
				if (mPiemenuState == mState_PostilPiemenu) {
					if (mLastTouchItem != null) {
						if (mLastTouchItem.getmPosition() != 3 && mLastTouchItem.getmPosition() != 4
								&& mLastTouchItem.getmPosition() != 2 && mLastTouchItem.getmPosition() != 1) {
							mLastTouchItem.setPressed(false);
							if (mThisTouchItem != null) {
								mThisTouchItem.setPressed(true);
							}
						} else {
							if (mThisTouchItem != mLastTouchItem) {

								mLastTouchItem.setPressed(true);
								mThisTouchItem = mLastTouchItem;
							}
						}
						invalidate();
					}
				}
                long curTime = SystemClock.uptimeMillis();
                if (mItemEventListener != null && mLastTouchItem != null) {
                    if (curTime - mLastClickTime > 50) {
                        mLastClickTime = curTime;
                        mItemEventListener.onItemClick(mLastTouchItem);
                    }
                }
                if (mIsCent) {
                    if (mItemEventListener != null) {
                        if (curTime - mLastClickTime > 500) {
                            mItemEventListener.onCenterClick();
                            mLastClickTime = curTime;
                        }
                    }
                }
                if (mLongPressRunnable != null) {
                    removeCallbacks(mLongPressRunnable);
                }
            } else {
				if (mPiemenuState == mState_PostilPiemenu) {
					if (mLastTouchItem != null) {
						mLastTouchItem.setPressed(false);
						if (mThisTouchItem != null) {
							mThisTouchItem.setPressed(true);
						}
						invalidate();
					}
				}
            }
            
        } else if (MotionEvent.ACTION_CANCEL == action) {

        } else if (MotionEvent.ACTION_MOVE == action) {
            double _x = Math.abs(x - mLastTouchX);
            double _y = Math.abs(y - mLastTouchY);
            if (_x > 10 || _y > 10) {
                mIsTouchMove = true;
                xInScreen = evt.getRawX();
                yInScreen = evt.getRawY();
                float movex = (xInScreen - xDownInScreen);
                float movey = (yInScreen - yDownInScreen);
                yDownInScreen = yInScreen;
                xDownInScreen = xInScreen;
                if (mLongPressRunnable != null) {
                    removeCallbacks(mLongPressRunnable);
                }
                if (mItemEventListener != null) {
                    mItemEventListener.onTouchMove(movex, movey);
                }
            }
        }
        return false;
    }
   
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG| Paint.FILTER_BITMAP_FLAG));
        if(!mIsLayoutItems){  
            layoutItems(canvas);
            mIsLayoutItems = true;
        }
        int state;
        for (List<PieItem> list : mItemList) {
            for (PieItem item : list) {
                Paint p = item.isPressed() ? mPressedPaint : mNormalPaint;
                state = canvas.save();
                drawPath(canvas, item.getPath(), p);
                canvas.restoreToCount(state);
                drawItem(canvas, item);  
            }
        }
       
        Paint paint = mCenterPressed ?  mPressedPaint : mNormalPaint;
        canvas.drawCircle(mCenter.x,mCenter.y,mCenterMenuRadius,paint);
        drawPath(canvas, makePath(mCenter), paint);
        canvas.drawBitmap(mCenterBitmap, mCenter.x - mCenterBitmap.getWidth()/2, mCenter.y - mCenterBitmap.getHeight()/2, null);
        
        //绘制分割线
        //layoutInterval(canvas);
    }
    /**
     * 绘制外层菜单
     */
    
    private void drawItem(Canvas canvas, PieItem item) {
        View view = item.getView();
        int state = canvas.save();
         
      
        canvas.translate(view.getX(), view.getY());
        view.draw(canvas);
        
        canvas.restoreToCount(state);
        state = canvas.save();
        canvas.restoreToCount(state);
        
    }
  
    private void drawPath(Canvas canvas, Path path, Paint paint) {
        canvas.drawPath(path, paint);
    }
    /** 
     *  创建每一个菜单
     *  @param level 0:第一级菜单 ;1:第二级菜单;2:第三级菜单 
     *  
     * */
   
    public PieItem makeItem(ImageView view, TextView tv, int level, String iconName) {
        
        LinearLayout itemLayout=new LinearLayout(mContext);
        itemLayout.setGravity(Gravity.CENTER);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        
        //view.setImageResource(image);
        if(view!=null){
            
            //图标
//            view.setMinimumWidth(mItemIconSize);
//            view.setMinimumHeight(mItemIconSize);
           // view.setScaleType(ImageView.ScaleType.FIT_XY);
            LayoutParams params = new LayoutParams(65, LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(params);
            itemLayout.setLayoutParams(params);
            itemLayout.addView(view);
        }
        else{
            if(tv!=null)
            tv.setTextSize(mItemNameSize+5);
        }
        if(!iconName.equalsIgnoreCase("")){
            //图标名字
            
            tv.setMaxHeight(mMenuWidth/2);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(mItemNameSize+2);
            tv.setTextColor(Color.parseColor("#ffffff"));
            itemLayout.addView(tv);
        }
       
       
        position++;
        return new PieItem(itemLayout, level,position);
        
    }
   
    private int  position=-1;
    /**
     * 控件的响应监听事件 </br>
     * onItemClick(PieItem item)外层菜单点击事件 </br>
     * onCenterClick() 中心点击事件 </br>
     * onTouchMove(float x, float y) 控件移动处理事件
     */
    public static interface OnEventListener{
    	public void onItemClick(PieItem item);
        public void onCenterClick();
        public void onTouchMove(float x, float y);
        public void onItemLongClick(PieItem item);
    }
    /**
     * 判断是否点击控件中心菜单
     * @param centerPoint 控件中心坐标
     * @param touchDownPoint 触屏点坐标
     * @return true:是控件中心菜单；
     *         otherwise false
     */
    private boolean isCenter(Point centerPoint, Point touchDownPoint){
        double _x = Math.abs(centerPoint.x - touchDownPoint.x);
        double _y = Math.abs(centerPoint.y - touchDownPoint.y);
        double distence= Math.sqrt(_x*_x+_y*_y);
        if (mCenterMenuRadius>distence){
            return true;
        }
        return false;
    }
    /**
     * 设置控件中心图片
     * @param bitmap 中心图片
     */
    public void setCenterBitmap(Bitmap bitmap){
        this.mCenterBitmap=bitmap;
    }
    /**
     * 设置普通状态背景颜色
     * @param color 背景颜色
     */
	public void setNormalPaint(int color) {
		mNormalPaint.setColor(color);
		
	}
	/**
	 * 设置触屏事件背景颜色
	 * @param color 背景颜色
	 */
	public void setPressedPaint(int color) {
		mPressedPaint.setColor(color);
		
	}
	/**
	 * 设置图标大小 
	 * @param size 图标大小
	 */
	private void setIconSize(int size){
		mItemIconSize = size;
	}
	/**
	 * 设置图标文字大小
	 * @param size 图标文字大小
	 */
	public void setIconNameSize(int size){
	    mItemNameSize = size;
	}
	/**
	 * 设置图标文字颜色
	 * @param color 图标文字颜色
	 */
	public void setIconNameColor(int color){
		mItemNameColor = color;
	}
	/**
	 * 设置中心菜单半径
	 * @param radius 中心菜单半径
	 */
	public void  setCenterMenuRadius(int radius){
		mCenterMenuRadius = radius;
	}
	/**
	 * 设置外部菜单半径
	 * @param radius 外部菜单半径
	 */
	public void setOutSideMenuRadius(int radius){
		mOutSideMenuRadius = radius;
	}
	/**
	 * 设置菜单宽度
	 * @param width 菜单宽度
	 */
	public void setMenuWidth(int width){
		mMenuWidth = width;
	}
	/**
	 * 设置分割线颜色
	 * @param color 分割线颜色
	 */
    public void setIntervalColor(int color){
    	mIntervalColor = color;
    }
    
    public void setSkin(){
    	invalidate();
    } 
    private Handler mSelectionPanelHandler;
    private Handler mUpdateTouchAreaHandler;
    private Handler mSavePieLocationHandler;
    private static final int DISMISS_PANEL = 0x100;
    private static final int SELECTED_PANEL = 0x500;
    private static final int UPDATE_TOUCH_AREA_FULLSCREEN = 0x200;
    private static final int UPDATE_TOUCH_AREA_RECT = 0x400;
    private static final int SAVE_PIE = 0x900;
    public void setDismissPanelHandler(Handler mSelectionPanelHandler){
        this.mSelectionPanelHandler = mSelectionPanelHandler;
    }
    
    public void setUpdateTouchAreaHandler (Handler mUpdateTouchAreaHandler){
    	this.mUpdateTouchAreaHandler = mUpdateTouchAreaHandler;
    }

    public void setSavePieLocationHandler(Handler mSavePieLocationHandler){
        this.mSavePieLocationHandler = mSavePieLocationHandler;
    }
    /**
     * 设置圆盘的状态 
     *  mPiemenuState =1 功能圆盘 
     *  mPiemenuState =2 批注圆盘 
     */
    private int mState_FunctionPiemenu = 1;
    private int mState_PostilPiemenu = 2;
    private int mPiemenuState = -1; 
    public void setPieMenuState (int mPiemenuState){
    	this.mPiemenuState = mPiemenuState;
    }
}
