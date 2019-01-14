package com.ingxin.android.banner.sample.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ingxin.android.banner.Banner;
import com.ingxin.android.banner.sample.R;
import com.ingxin.android.banner.sample.adapter.BannerAdapter;
import com.ingxin.android.banner.sample.pojo.BannerItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Banner banner;
    private BannerAdapter adapter;
    private List<BannerItem> bannerItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        banner = (Banner) findViewById(R.id.banner);
        loadData();

        findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bannerItemList.size() > 1) {
                    bannerItemList.remove(bannerItemList.size() - 1);
                    adapter.setData(bannerItemList);
                    adapter.notifyDataSetChanged();
                    banner.autoPay();
                }

            }
        });

    }

    private void loadData() {
        BannerItem item1 = new BannerItem(R.mipmap.pic);
        BannerItem item2 = new BannerItem(R.mipmap.pic2);
        BannerItem item3 = new BannerItem(R.mipmap.pic3);

        bannerItemList = new ArrayList<>();
        bannerItemList.add(item1);
        bannerItemList.add(item2);
        bannerItemList.add(item3);

        //设置模式
        banner.setAutoInterval(5000);
        banner.setCyclicEnable(true);
        //设置滑动系数
        banner.setScrollDurationFactor(5);

        //正常view pager 操作
        adapter = new BannerAdapter();
        adapter.setData(bannerItemList);
        banner.setAdapter(adapter);

        //准备完成，开始显示
        banner.autoPay();
    }


}
