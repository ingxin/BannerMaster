package com.ingxin.android.banner;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * banner adapter
 *
 * @param <T> banner数据类型
 */
public abstract class Adapter<T> extends PagerAdapter {

    private List<T> items = new ArrayList<>();
    private boolean isCyclic;

    /**
     * 设置数据源
     */
    public void setData(@NonNull List<T> data) {
        items.clear();
        if (!data.isEmpty()) {
            this.items.addAll(data);
        }
    }

    /**
     * 获取items
     *
     * @return 获取items
     */
    public List<T> getItems() {
        return items;
    }

    /**
     * 设置banner循环模式
     *
     * @param enable true表示可以无限循环，false则正常模式
     */
    void setCyclicEnable(boolean enable) {
        this.isCyclic = enable;
    }

    /**
     * 获取当前是否可以循环模式
     *
     * @return true表示可以无限循环，false表示正常
     */
    public boolean isCyclic() {
        return isCyclic;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    final public int getCount() {
        if (items.isEmpty()) {
            return 0;
        } else {
            int count = items.size();
            return isCyclic ? count + 2 : count;
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (isCyclic) {
            if (position == 0) {
                int size = items.size();
                T item = items.get(size - 1);
                return instantiateItem(container, item, size - 1);
            } else if (position == getCount() - 1) {
                T item = items.get(0);
                return instantiateItem(container, item, 0);
            } else {
                T item = items.get(position - 1);
                return instantiateItem(container, item, position - 1);
            }
        } else {
            T item = items.get(position);
            return instantiateItem(container, item, position);
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    /**
     * see{@link #instantiateItem(ViewGroup, int)}
     */
    public abstract View instantiateItem(@NonNull ViewGroup container, T item, int position);

}
