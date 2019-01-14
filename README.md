BannerMaster
---
- [x] viewPager轻量实现
- [x] 支持无限循环模式
- [x] 支持设置滑动速度
- [x] 支持自动翻页
- [x] 资源自动回收

使用方法
---
1. 添加依赖
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.ingxin:BannerMaster:v1.2.0'
}
```
2. 继承adapter
```java
public class BannerAdapter extends Adapter<T> {
}
```

3. 其余操作viewPager一样，只是多了以下设置
```java
//设置自动播放间隔时间，如果小于5000表示不自动播放，默认为0
banner.setAutoInterval(5000);
//设置是否循环模式
banner.setCyclicEnable(true);
//设置滑动系数（调整滑动时间）
banner.setScrollDurationFactor(5);

//正常view pager 操作
BannerAdapter adapter = new BannerAdapter();
adapter.setData(bannerItemList);
banner.setAdapter(adapter);

//准备完成，开始显示
banner.autoPay();

```

更新日志
---
##### v1.3.0
当只有一个item时，强制禁用循环翻页，自动翻页

##### v1.2.0
修改循环模式首尾页面切换实现方法，解决切换时不流畅问题

##### v1.1.0
完成基本功能
