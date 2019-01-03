#### 添加依赖
Gradle：
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.ingxin:BannerMaster:v1.1.0'
}
```

#### 开始使用
```
//...与viewPager一样，只是多了以下设置

//设置自动播放间隔时间，如果小于5000表示不自动播放，默认为0
banner.setAutoInterval(5000);
//设置是否循环
banner.setCyclicEnable(true);
//设置滑动系数
banner.setScrollDurationFactor(2.2);

//正常view pager 操作
BannerAdapter adapter = new BannerAdapter();
adapter.setData(bannerItemList);
banner.setAdapter(adapter);

//准备完成，开始显示
banner.autoPay();
```
