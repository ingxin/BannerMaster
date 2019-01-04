package com.ingxin.android.banner.sample.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ingxin.android.banner.Banner;
import com.ingxin.android.banner.sample.R;
import com.ingxin.android.banner.sample.adapter.BannerAdapter;
import com.ingxin.android.banner.sample.pojo.BannerItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Banner banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        banner = (Banner) findViewById(R.id.banner);
        loadData();
    }

    private void loadData() {
        BannerItem item1 = new BannerItem(R.mipmap.pic);
        BannerItem item2 = new BannerItem(R.mipmap.pic2);
        BannerItem item3 = new BannerItem(R.mipmap.pic3);

        List<BannerItem> bannerItemList = new ArrayList<>();
        bannerItemList.add(item1);
        bannerItemList.add(item2);
        bannerItemList.add(item3);

        //设置模式
        banner.setAutoInterval(5000);
        banner.setCyclicEnable(true);
        //设置滑动系数
        banner.setScrollDurationFactor(5);

        //正常view pager 操作
        BannerAdapter adapter = new BannerAdapter();
        adapter.setData(bannerItemList);
        banner.setAdapter(adapter);

        //准备完成，开始显示
        banner.autoPay();
    }


}
