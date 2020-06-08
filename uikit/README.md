# UIKIT
[TOC]
## 介绍
基础的ui框架库，包含recyclerview,webview的封装，封装其他功能性组件如badge,bottomview等

## recyclerview

## webview

## badge

## bottomview

## 主题换肤
整体方向和原则
- 原则 : 不需要控制到每一个小细节，粒度过细没有实际意义，比如已经定义好的底部menu的高度，虽然好做但是没有没意思，
主题换肤更多的还是menu图片，整体色系的变化，已经功能元素（功能元素的替换不在这里进行讨论）

- 方向 : 定义新的attr属性，重写TextView,ImageView,View将text,width,length,color,background,backgroundcolor,src,shape 用字符串代替传入
```
    text = "@string/home_bottom_item_settings"
```
textview 的text来源应属于单独的文件，将语言配置和主题配置区分开来，方便后续国际化

在自定义的TextView,ImageView,View中对传进来的text进行分析
如果在内存中能找到配置中map的key,则用map的value代替赋值
如果查询不到，使用原生R.string.home_bottom_item_settings
类似的比如text,width,lenght,color
重点需要考虑的是以下几个
- background 必然是将使用网络，本地不可能准备那么多皮肤，所以在background上要用glide进行重新加载，
这里只使用于本地资源文件，比如首页底部menu的图片
- src: 用glide 替换图片
- backgroundColor 用颜色作为背景色
- shape: shape实际上比较特殊，因为没有办法通过上面的方法通过重写一个view在view创建阶段重新查询赋值，查不到将使用R.drawable.home_list_item_button_shape
目前有两个方案，倾向于同时使用
1.将shape图片预先定义出几套，放在项目中，map中对shape的替换只是替换项目中原有的样式
2.将shape完全放开，{color,radius,stroke},但是只适用于简单的shape图形，比如正方形框这种，一但shape层级变复杂，这种方法将失控

## 规则
属性控制规则

远端对应到key属性>本地对应到key属性>远端key所在group组属性>本地所在group组属性，
当前用TestData模拟属性，用ThemeLocalMapping模拟本地数据属性

## more

For detailed usage, please refer to DemoRecyclerviewActivity.java/DemoBottomNavigationActivity.java/
DemoWebviewActivity.java/DemoThemeActivity.java/DemoBannerActivity.java in the demo app.
