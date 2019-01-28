package com.ingxin.android.banner;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 可自动播放的banner
 */
public class Banner extends ViewPager {

    //自动翻页间隔阈值
    private static final long INTERVAL_THRESHOLD = 5000;
    //延迟页面变化时间，优化首尾切换效果
    private static final int DELAYED_CHANGE = 20;

    //自动播放间隔
    private long autoInterval;
    //滑动系数，默认滑动系数为5
    private double mScrollFactor = 5;
    //是否可以循环翻页
    private boolean isCyclic;
    //自动翻页滑动模式，true:有滑动效果，false：没有滑动效果
    private boolean autoSmoothEnable = true;
    //页面变化的监听回调
    private OnPageChangeListener onPageChangeListener;

    /**
     * 标记是否可以调用{@link #setCyclicEnable(boolean)}
     * 当调用了{@link #setAdapter(PagerAdapter)}后则不可再调用{@link #setCyclicEnable(boolean)}
     */
    private boolean canSetCyclic = true;
    //标记是否在首尾页面切换当中
    private boolean inTask;
    //是否在循环
    private boolean isLoop;
    //处于触摸中
    private boolean onTouching;
    //滑动Scroller
    private ScrollerCustomDuration mScroller = null;
    //代理页面变化的监听回调
    private ProxyOnPageChangeListener proxyOnPageChangeListener;
    //内部handle
    private Handler handler = new Handler();

    public Banner(@NonNull Context context) {
        super(context);
        postInitViewPager();
    }

    public Banner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        postInitViewPager();
    }

    /**
     * 自动播放任务
     */
    private Runnable loopTask = new Runnable() {
        @Override
        public void run() {
            Adapter adapter = getBannerAdapter();
            if (adapter == null || onTouching) {
                return;
            }
            int currentPos = getCurrentItem();
            int count = adapter.getCount();
            if (isCyclic) {
                if (count >= 3) {
                    if (currentPos < count - 1 && currentPos != 0) {
                        //0和count -1位置数据交给翻页任务处理，此处不处理
                        setCurrentItem(currentPos + 1, autoSmoothEnable);
                    }
                }
            } else {
                if (count > 1) {
                    if (currentPos >= count - 1) {
                        setCurrentItem(0, false);
                    } else {
                        setCurrentItem(currentPos + 1, autoSmoothEnable);
                    }
                }
            }
            if (isLoop) {
                handler.postDelayed(loopTask, autoInterval);
            }
        }
    };

    //循环时从末页滑到首页任务
    private Runnable toFirstTask = new Runnable() {
        @Override
        public void run() {
            if (!isCyclic) {
                return;
            }
            Adapter adapter = getBannerAdapter();
            if (adapter != null && adapter.getCount() >= 3) {
                int count = adapter.getCount();
                //循环模式个数至少为3
                if (getCurrentItem() == count - 1) {
                    setCurrentItem(1, false);
                    inTask = false;
                }
            }
        }
    };

    //循环时从首页滑到末页任务
    private Runnable toLastTask = new Runnable() {
        @Override
        public void run() {
            if (!isCyclic) {
                return;
            }
            Adapter adapter = getBannerAdapter();
            if (adapter != null && adapter.getCount() >= 3) {
                int count = adapter.getCount();
                if (getCurrentItem() == 0) {
                    setCurrentItem(count - 2, false);
                    inTask = false;
                }
            }
        }
    };

    /**
     * 设置自动播放的时间间隔，所设置的值必须大于等于{@link #INTERVAL_THRESHOLD}
     *
     * @param autoInterval 自动播放的时间间隔
     */
    public void setAutoInterval(long autoInterval) {
        if (autoInterval >= INTERVAL_THRESHOLD) {
            this.autoInterval = autoInterval;
        }
    }

    /**
     * 设置是循环模式，注意该方法需要在方法{@link #setAdapter(PagerAdapter)}之前调用
     *
     * @param cyclicEnable true表示无限循环，false表示正常模式
     */
    public void setCyclicEnable(boolean cyclicEnable) {
        if (!canSetCyclic) {
            throw new IllegalStateException("invoking method setCyclicEnable must before setAdapter");
        }
        this.isCyclic = cyclicEnable;
    }

    /**
     * 设置自动翻页的模式
     *
     * @param smoothEnable true翻页时有滑动效果，false没有滑动效果
     */
    public void setAutoSmoothEnable(boolean smoothEnable) {
        this.autoSmoothEnable = smoothEnable;
    }

    /**
     * 设置滑动参数系数，默认系数为{@link #mScrollFactor}，系数越高滑动速度越慢
     *
     * @param scrollFactor 系数
     */
    public void setScrollDurationFactor(double scrollFactor) {
        this.mScrollFactor = scrollFactor;
        setScrollFactor(scrollFactor);
    }

    /**
     * 设置滑动系数
     */
    private void setScrollFactor(double scrollFactor) {
        if (mScroller != null) {
            mScroller.setScrollDurationFactor(scrollFactor);
        }
    }

    /**
     * 开始自动翻页，只有{@link #autoInterval}大于{@link #INTERVAL_THRESHOLD}
     * 当调用了{@link PagerAdapter#notifyDataSetChanged()}or{@link Adapter#setData(List)}方法后应该调此方法
     */
    public void autoPay() {
        handler.removeCallbacks(loopTask);
        //重新获取是否可以循环的状态
        Adapter adapter = getBannerAdapter();
        isCyclic = adapter != null && adapter.isCyclic();
        if (isCyclic && adapter.getCount() >= 3) {
            setCurrentItem(1, false);
        }
        if (canAuto()) {
            handler.postDelayed(loopTask, autoInterval);
        }
    }

    /**
     * 停止自动播放
     */
    public void stopPlay() {
        isLoop = false;
        handler.removeCallbacks(loopTask);
    }

    /**
     * 根据时间判断是否能自动翻页
     */
    private boolean canAuto() {
        Adapter adapter = getBannerAdapter();
        return adapter != null && adapter.getCount() > 1 && autoInterval >= INTERVAL_THRESHOLD;
    }

    /**
     * 获取代理页面监听回调
     */
    private ProxyOnPageChangeListener getProxyOnPageChangeListener() {
        if (proxyOnPageChangeListener == null) {
            proxyOnPageChangeListener = new ProxyOnPageChangeListener();
        }
        return proxyOnPageChangeListener;
    }

    /**
     * 绑定滑动Scroller
     */
    private void postInitViewPager() {
        try {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = ViewPager.class.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);

            mScroller = new ScrollerCustomDuration(getContext(), (Interpolator) interpolator.get(null));
            scroller.set(this, mScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (inTask) {
            //正在页面切换当中
            return;
        }
        if (!mScroller.computeScrollOffset() && mScroller.isFinished()) {
            int currentItem = getCurrentItem();
            if (isCyclic) {
                Adapter adapter = getBannerAdapter();
                if (adapter != null && adapter.getCount() > 0) {
                    int count = adapter.getCount();
                    handler.removeCallbacks(toFirstTask);
                    handler.removeCallbacks(toLastTask);
                    if (currentItem == 0) {
                        inTask = true;
                        handler.postDelayed(toLastTask, DELAYED_CHANGE);
                    } else if (currentItem == count - 1) {
                        inTask = true;
                        handler.postDelayed(toFirstTask, DELAYED_CHANGE);
                    }
                }
            }
        }
    }

    /**
     * 获取BannerAdapter
     *
     * @return BannerAdapter
     */
    @Nullable
    private Adapter getBannerAdapter() {
        PagerAdapter adapter = getAdapter();
        if (adapter instanceof Adapter) {
            return (Adapter) adapter;
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean superResult = super.onTouchEvent(event);
        int action = event.getAction();
        onTouching = action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL;
        handler.removeCallbacks(loopTask);
        if (onTouching) {
            setScrollFactor(1);
        } else {
            if (isLoop && canAuto()) {
                handler.postDelayed(loopTask, autoInterval);
            }
        }
        return superResult;
    }

    @Override
    public void setAdapter(@Nullable PagerAdapter adapter) {
        if (!(adapter instanceof Adapter)) {
            throw new IllegalArgumentException("adapter must extends BannerAdapter");
        }
        //设置循环模式
        ((Adapter) adapter).setCyclicEnable(isCyclic);
        super.setAdapter(adapter);
        super.removeOnPageChangeListener(getProxyOnPageChangeListener());
        super.addOnPageChangeListener(getProxyOnPageChangeListener());
        //设置了adapter后不能再改变循环模式
        canSetCyclic = false;
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        handler.removeCallbacks(loopTask);
        isLoop = visibility == VISIBLE;
        if (isLoop) {
            int currentItem = getCurrentItem();
            setCurrentItem(currentItem,false);
            if (canAuto()) {
                handler.postDelayed(loopTask, autoInterval);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        /*
         *当view 被移除窗口，如果执行了onAttachedToWindow将会触发onVisibilityChanged
         *而onDetachedFromWindow不会触发onVisibilityChanged,所以此时应该停止循环
         */
        stopPlay();
    }

    @Override
    public void addOnPageChangeListener(@NonNull OnPageChangeListener listener) {
        //屏蔽原有逻辑，交给代理处理
        this.onPageChangeListener = listener;
    }

    /**
     * viewPager的页面变化监听代理
     */
    private class ProxyOnPageChangeListener implements OnPageChangeListener {
        /**
         * onPageScrolled
         *
         * @param position             变化的position
         * @param positionOffset       position+1位置view偏移量[0,1]百分比，0没有展示，1完全展示出来
         * @param positionOffsetPixels 与positionOffset相对应的像素偏移量
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (onPageChangeListener == null) {
                return;
            }
            if (isCyclic) {
                Adapter adapter = getBannerAdapter();
                if (adapter != null && adapter.getCount() > 0) {
                    int count = adapter.getCount();
                    if (position == count - 2 && positionOffset > 0) {
                        //显示的最后一页向后滑到显示的第一页的过程,不处理
                    } else if (position == count - 1) {
                        //停在真正的最后一页，缓存页面不处理
                    } else if (position == 0 && positionOffset == 1) {
                        //停在了显示的第一页，position = 0,等效于position = 1, positionOffset = 0, positionOffsetPixels = 0
                        //转化成显示的第一页
                        onPageChangeListener.onPageScrolled(0, 0, 0);
                    } else if (position == 0 && positionOffset >= 0) {
                        //显示的第一页向前滑到显示的最后一页过程，不处理
                    } else if (position == 0 && positionOffset == 0) {
                        //停在真正的第一页，缓存页面不需要处理
                    } else if (position > 0 && position <= count - 2) {
                        onPageChangeListener.onPageScrolled(position - 1, positionOffset, positionOffsetPixels);
                    }
                }
            } else {
                onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {
            setScrollFactor(mScrollFactor);
            if (isCyclic) {
                handler.removeCallbacks(toFirstTask);
                handler.removeCallbacks(toLastTask);
                Adapter adapter = getBannerAdapter();
                if (adapter != null && adapter.getCount() > 0) {
                    int count = adapter.getCount();
                    if (position != 0 && position != count - 1) {
                        if (onPageChangeListener != null) {
                            onPageChangeListener.onPageSelected(position - 1);
                        }
                    }
                }
            } else {
                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageSelected(position);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageScrollStateChanged(state);
            }
        }
    }
}
