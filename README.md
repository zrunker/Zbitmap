# ZBitmap
Bitmap管理，包括Bitmap常见管理方法的封装，和Bitmap的内存缓存的封装。使用工具Android Studio

>作者：邹峰立，微博：zrunker，邮箱：zrunker@yahoo.com，微信公众号：书客创作，个人平台：[www.ibooker.cc](http://www.ibooker.cc)。

>本文选自[书客创作](http://www.ibooker.cc)平台第21篇文章。[阅读原文](http://www.ibooker.cc/article/21/detail) 。

![书客创作](http://upload-images.jianshu.io/upload_images/3480018-7b36898e0754caf0..jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Bitmap是开发当中常常要使用到的，但对于大多数开发者来说，Bitmap的使用起来并不是那么简单。拿Android平台来说，Bitmap的大小不能超过4M，否则就会出现OOM问题，而且Bitmap使用完后要进行及时回收，否则也会出现OOM问题。

Android OOM（内存溢出），产生原因一般情况下：a、加载对象过大。b、响应资源过多，没有来不及释放。

ZBitmap是最近封装的一个对Bitmap进行压缩，转换，缓存的工具类，这里只是跟大家介绍一下如何使用这个工具类，至于该类是怎么处理的，大家可以自行去gitHub上获取相关源码。

**一、引入资源**

这里提供两种方式，引入资源文件：

1、通过gradle进行引入：

```
allprojects {
   repositories {
      maven { url 'https://www.jitpack.io' }
   }
}

dependencies {
   compile 'com.github.zrunker:ZBitmap:v1.0.0'
}
```
2、通过Maven引入，在pom.xml文件中添加如下代码：
```
<repositories>
   <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
   </repository>
</repositories>
```
```
<dependency>
   <groupId>com.github.zrunker</groupId>
   <artifactId>ZBitmap</artifactId>
   <version>v1.0.0</version>
</dependency>
```
**二、用法**

**1、BitmapFun类-用于转换，压缩，保存Bitmap**
```
/**
 *由图片路径转换成Bitmap
 *@param imgPath图片路径
 *@param bitmapFunConfigBitmapConfig
 */
Bitmap bitmap = BitmapFun.imgPathToBitmap1(String imgPath,BitmapFunConfig bitmapFunConfig);

/**
 *将图片路径转换成流之后转化为Bitmap
 *@param imgPath图片路径
 */
Bitmap bitmap = BitmapFun.imgPathToBitmap2(String imgPath) ;

/**
 *将Bitmap转换成File，写入SD卡
 *@param bitmap Bitmap数据源
 *@param filePath保存SD卡文件路径
 *@param bitmapFunCompressFormat图片格式
 *@return Uri
 */
Uri uri = BitmapFun.saveBitmapToUri(Bitmap bitmap,String filePath,BitmapFunCompressFormat bitmapFunCompressFormat) ;

/**
 *将Bitmap转换成图片文件，写入SD卡
 *@param bitmap Bitmap数据源
 *@param filePath保存SD卡文件路径
 *@param bitmapFunCompressFormat图片格式
 *@return ImagePath
 */
String imagePath = BitmapFun.saveBitmapToImgPath(Bitmap bitmap,String filePath,BitmapFunCompressFormat bitmapFunCompressFormat) ;

/**
 *根据图片路径压缩图片并转成Bitmap，宽和高同步递减
 *@param imgPath真实图片路径
 *@param width图片最终宽
 *@param height图片最终高
 */
Bitmap bitmap = BitmapFun.imgPathToReSizeBitmap(String imgPath, intwidth, intheight);

/**
 *通过uri获取图片并进行比例压缩和进行一次质量压缩，大于1M的时候进行质量压缩
 *@param activity上下文对象
 *@param uri uri数据源
 *@param bitmapFunConfigBitmapConfig
 *@param bitmapFunCompressFormat图片格式
 */
Bitmap bitmap = BitmapFunc.uriToReSizeBitmap(Activity activity,Uri uri,BitmapFunConfig bitmapFunConfig,BitmapFunCompressFormat bitmapFunCompressFormat);

/**
 * Bitmap按照质量来压缩图片，压缩到<=maxSize
 *@param bitmap bitmap数据源
 *@param maxSize最大大小（K）
 *@param bitmapFunCompressFormat图片格式
 */
Bitmap bitmap = BitmapFun.compressBitmapByQuality(Bitmap bitmap, intmaxSize,BitmapFunCompressFormat bitmapFunCompressFormat);

/**
 * Bitmap图片按比例大小压缩方法
 *@param bitmap Bitmap数据源
 *@param pixelW对比宽
 *@param pixelH对比高
 *@param bitmapFunConfigBitmapConfig
 *@param bitmapFunCompressFormat图片格式
 */
Bitmap bitmap = BitmapFun.compressBitmapByRatio(Bitmap bitmap, floatpixelW, floatpixelH,BitmapFunConfig bitmapFunConfig,BitmapFunCompressFormat bitmapFunCompressFormat);
```
**2、BitmapCache类-用于缓存Bitmap**

BitmapCache bitmapCacheUtil =new BitmapCache();// 初始化缓存类
```
/**
 *添加缓存
 *@param key键
 *@param value值
 */
bitmapCacheUtil.addBitmapToCache(String key,Bitmap value);

/**
 *更新缓存
 *@param key键
 *@param value值
 */
bitmapCacheUtil.updateBitmapToCache(String key,Bitmap value);

/**
 *取出Bitmap
 *@param key键
 */
Bitmap bitmap = bitmapCacheUtil.getBitmapFromCache(String key);

/**
 *移除Bitmap
 *@param key键
 */
bitmapCacheUtil.removeBitmapFromCache(String key);

/**
 *清空缓存
 */
bitmapCacheUtil.clearCache();
```
最后只需要在相应界面调用这些方法即可，如：
```
// bitmap方法类BtimapFun
BitmapFun.imgPathToBitmap2("imagePath");
        
// bitmap缓存类BitmapCache
BitmapCache bitmapCacheUtil = new BitmapCache();
bitmapCacheUtil.removeBitmapFromCache("imagePath");
```
[Github地址](https://github.com/zrunker/ZBitmap)
[阅读原文](http://www.ibooker.cc/article/21/detail)

----------
![微信公众号：书客创作](http://upload-images.jianshu.io/upload_images/3480018-02ad75034e28cdab..jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
