package com.ingxin.android.banner.sample.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ingxin.android.banner.Adapter;
import com.ingxin.android.banner.sample.R;
import com.ingxin.android.banner.sample.pojo.BannerItem;

public class BannerAdapter extends Adapter<BannerItem> {
    @NonNull
    @Override
    public View getItemView(@NonNull ViewGroup container, @NonNull BannerItem item, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.banner_item, container, false);
        ImageView imageView = (ImageView) view;
        imageView.setImageResource(item.imgRes);
        return imageView;
    }
}
