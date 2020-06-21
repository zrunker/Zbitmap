package cc.ibooker.zbitmaplib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * Bitmap处理工具类
 *
 * @author 邹峰立
 */
public class BitmapUtil {
    private static final String TEMP_PATH = Environment.getExternalStorageDirectory() + File.separator + "temp" + File.separator;

    public enum BitmapFunCompressFormat {
        PNG, JPEG, WEBP
    }

    public enum BitmapFunConfig {
        RGB_565, ALPHA_8, ARGB_4444, ARGB_8888
    }

    /**
     * 本地图片转Bitmap，宽800,高800
     *
     * @param path 本地图片路径
     * @return 位图Bitmap
     */
    public static Bitmap imgPathToReSizeBitmap(@NonNull String path) {
        return imgPathToReSizeBitmap(path, 800, 800);
    }


    /**
     * 根据图片路径压缩图片并转成Bitmap，宽和高同步递减
     *
     * @param imgPath 真实图片路径
     * @param width   图片最终宽
     * @param height  图片最终高
     */
    public static Bitmap imgPathToReSizeBitmap(@NonNull String imgPath, int width, int height) {
        Bitmap bitmap = null;
        FileInputStream in = null;
        try {
            File imgFile = new File(imgPath);
            if (imgFile.exists() && imgFile.isFile()) {
                in = new FileInputStream(imgFile);
                BitmapFactory.Options options = new BitmapFactory.Options();
                // 生产Bitmap不分配内存空间
                options.inJustDecodeBounds = true;
                // 传递图片，主要为了获取图片宽和高
                BitmapFactory.decodeStream(in, null, options);
                in.close();

                int i = 0;
                while (true) {
                    if ((options.outWidth >> i <= width) && (options.outHeight >> i <= height)) {
                        in = new FileInputStream(new File(imgPath));
                        // 新生成的图是原图的几分之几
                        options.inSampleSize = (int) Math.pow(2.0D, i);
                        // 生产Bitmap分配内存空间
                        options.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeStream(in, null, options);
                        break;
                    }
                    i += 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * 按照质量来压缩图片，压缩到<=size
     *
     * @param image   待压缩位图
     * @param maxSize 最大大小（K）
     * @return 位图Bitmap
     */
    public static Bitmap compressBitmapByQuality(@NonNull Bitmap image, int maxSize) {
        return compressBitmapByQuality(image, maxSize, BitmapFunCompressFormat.PNG);
    }

    /**
     * Bitmap按照质量来压缩图片，压缩到<=maxSize
     *
     * @param bitmap                  bitmap数据源
     * @param maxSize                 最大大小（K）
     * @param bitmapFunCompressFormat 图片格式
     */
    public static Bitmap compressBitmapByQuality(@NonNull Bitmap bitmap, int maxSize,
                                                 BitmapFunCompressFormat bitmapFunCompressFormat) {
        Bitmap.CompressFormat bCompressFormat = Bitmap.CompressFormat.PNG;
        if (bitmapFunCompressFormat != null) {
            if (bitmapFunCompressFormat == BitmapFunCompressFormat.JPEG)
                bCompressFormat = Bitmap.CompressFormat.JPEG;
            else if (bitmapFunCompressFormat == BitmapFunCompressFormat.WEBP)
                bCompressFormat = Bitmap.CompressFormat.WEBP;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(bCompressFormat, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > maxSize) { // 循环判断如果压缩后图片是否大于maxSize kb,大于继续压缩
            if (options > 0) {
                baos.reset();// 重置baos，即清空baos
                options -= 20;// 每次都减少20
                bitmap.compress(bCompressFormat, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            } else {
                break;
            }
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap resultBitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            isBm.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultBitmap;
    }

    /**
     * 图片按比例大小压缩方法
     *
     * @param image  图片资源
     * @param pixelW 宽
     * @param pixelH 高
     * @return 位图Bitmap
     */
    public static Bitmap compressBitmapByRatio(@NonNull Bitmap image, float pixelW, float pixelH) {
        return compressBitmapByRatio(image, pixelW, pixelH, BitmapFunConfig.RGB_565, BitmapFunCompressFormat.PNG);
    }

    /**
     * Bitmap图片按比例大小压缩方法
     *
     * @param bitmap                  Bitmap数据源
     * @param pixelW                  对比宽
     * @param pixelH                  对比高
     * @param bitmapFunConfig         BitmapConfig
     * @param bitmapFunCompressFormat 图片格式
     */
    public static Bitmap compressBitmapByRatio(@NonNull Bitmap bitmap, float pixelW, float pixelH,
                                               BitmapFunConfig bitmapFunConfig,
                                               BitmapFunCompressFormat bitmapFunCompressFormat) {
        Bitmap.Config bConfig = Bitmap.Config.RGB_565;
        if (bitmapFunConfig != null) {
            if (bitmapFunConfig == BitmapFunConfig.ARGB_8888)
                bConfig = Bitmap.Config.ARGB_8888;
            else if (bitmapFunConfig == BitmapFunConfig.ALPHA_8)
                bConfig = Bitmap.Config.ALPHA_8;
            else if (bitmapFunConfig == BitmapFunConfig.ARGB_4444)
                bConfig = Bitmap.Config.ARGB_4444;
        }
        Bitmap.CompressFormat bCompressFormat = Bitmap.CompressFormat.PNG;
        if (bitmapFunCompressFormat != null) {
            if (bitmapFunCompressFormat == BitmapFunCompressFormat.JPEG)
                bCompressFormat = Bitmap.CompressFormat.JPEG;
            else if (bitmapFunCompressFormat == BitmapFunCompressFormat.WEBP)
                bCompressFormat = Bitmap.CompressFormat.WEBP;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(bCompressFormat, 100, os);
        if (os.toByteArray().length / 1024 > 2048) {// 判断如果图片大于2M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            os.reset();// 重置baos即清空baos
            bitmap.compress(bCompressFormat, 50, os);// 这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设true
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = bConfig;
        Bitmap resultBitmap;
        BitmapFactory.decodeStream(is, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > pixelW) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / pixelW);
        } else if (w < h && h > pixelH) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / pixelH);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设false
        is = new ByteArrayInputStream(os.toByteArray());
        resultBitmap = BitmapFactory.decodeStream(is, null, newOpts);
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultBitmap;
    }

    /**
     * 通过uri获取图片并进行比例压缩
     *
     * @param activity 当前Activity
     * @param uri      图片Uri信息
     * @return 位图Bitmap
     */
    public static Bitmap uriToReSizeBitmap(@NonNull Activity activity, @NonNull Uri uri) {
        return uriToReSizeBitmap(activity, uri, BitmapFunConfig.ARGB_8888, BitmapFunCompressFormat.PNG);
    }

    /**
     * 通过uri获取图片并进行比例压缩和进行一次质量压缩，大于1M的时候进行质量压缩
     *
     * @param activity                上下文对象
     * @param uri                     uri数据源
     * @param bitmapFunConfig         BitmapConfig
     * @param bitmapFunCompressFormat 图片格式
     */
    public static Bitmap uriToReSizeBitmap(@NonNull Activity activity, @NonNull Uri uri,
                                           BitmapFunConfig bitmapFunConfig,
                                           BitmapFunCompressFormat bitmapFunCompressFormat) {
        Bitmap bitmap = null;
        InputStream input = null;
        try {
            Bitmap.Config bConfig = Bitmap.Config.ARGB_8888;
            if (bitmapFunConfig != null) {
                if (bitmapFunConfig == BitmapFunConfig.RGB_565)
                    bConfig = Bitmap.Config.RGB_565;
                else if (bitmapFunConfig == BitmapFunConfig.ALPHA_8)
                    bConfig = Bitmap.Config.ALPHA_8;
                else if (bitmapFunConfig == BitmapFunConfig.ARGB_4444)
                    bConfig = Bitmap.Config.ARGB_4444;
            }

            Bitmap.CompressFormat bCompressFormat = Bitmap.CompressFormat.PNG;
            if (bitmapFunCompressFormat != null) {
                if (bitmapFunCompressFormat == BitmapFunCompressFormat.JPEG)
                    bCompressFormat = Bitmap.CompressFormat.JPEG;
                else if (bitmapFunCompressFormat == BitmapFunCompressFormat.WEBP)
                    bCompressFormat = Bitmap.CompressFormat.WEBP;
            }

            input = activity.getContentResolver().openInputStream(uri);

            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inDither = true;
            onlyBoundsOptions.inPreferredConfig = bConfig;
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            if (input != null) {
                input.close();
            }

            int originalWidth = onlyBoundsOptions.outWidth;
            int originalHeight = onlyBoundsOptions.outHeight;
            if ((originalWidth == -1) || (originalHeight == -1))
                return null;

            //图片分辨率以480x800为标准
            float hh = 800f;//这里设置高度为800f
            float ww = 480f;//这里设置宽度为480f
            //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
            int be = 1;//be=1表示不缩放
            if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
                be = (int) (originalWidth / ww);
            } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
                be = (int) (originalHeight / hh);
            }
            if (be <= 0)
                be = 1;

            //比例压缩
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = be;//设置缩放比例
            bitmapOptions.inDither = true;
            bitmapOptions.inPreferredConfig = bConfig;
            input = activity.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            if (input != null) {
                input.close();
            }

            //做一次质量压缩
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (bitmap != null) {
                bitmap.compress(bCompressFormat, 100, baos);
                if (baos.toByteArray().length / 1024 > 1024) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
                    baos.reset();// 重置baos即清空baos
                    bitmap.compress(bCompressFormat, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * 本地图片转化为Bitmap
     *
     * @param path 本地图片路径
     * @return 位图Bitmap
     */
    public static Bitmap imgPathToBitmap(@NonNull String path) {
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            bitmap = BitmapFactory.decodeStream(fis); // 把流转化为Bitmap图片
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * 由图片路径转换成Bitmap
     *
     * @param imgPath         图片路径
     * @param bitmapFunConfig BitmapConfig
     */
    public static Bitmap imgPathToBitmap(@NonNull String imgPath,
                                         BitmapFunConfig bitmapFunConfig) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 分配内存空间
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        // 不进行压缩
        options.inSampleSize = 1;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        if (bitmapFunConfig != null) {
            if (bitmapFunConfig == BitmapFunConfig.ALPHA_8)
                options.inPreferredConfig = Bitmap.Config.ALPHA_8;
            else if (bitmapFunConfig == BitmapFunConfig.ARGB_4444)
                options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            else if (bitmapFunConfig == BitmapFunConfig.ARGB_8888)
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }

        return BitmapFactory.decodeFile(imgPath, options);
    }

    /**
     * 将bitmap转换成File，写入SD卡
     *
     * @param bitmap   待处理位图
     * @param fileName 文件名称，带后缀
     * @return 文件URI
     */
    public Uri bitmapToFileUri(@NonNull Bitmap bitmap, @NonNull String fileName) {
        return saveBitmapToUri(bitmap, TEMP_PATH, fileName, BitmapFunCompressFormat.PNG);
    }

    /**
     * 将Bitmap转换成File，写入SD卡
     *
     * @param bitmap                  Bitmap数据源
     * @param filePath                保存SD卡文件路径
     * @param fileName                文件名称，带后缀
     * @param bitmapFunCompressFormat 图片格式
     * @return Uri
     */
    public static Uri saveBitmapToUri(@NonNull Bitmap bitmap, @NonNull String filePath, String fileName,
                                      BitmapFunCompressFormat bitmapFunCompressFormat) {
        Uri uri = null;
        FileOutputStream fos = null;
        // 创建文件夹
        File tmpDir = new File(filePath);
        boolean isMkdirsSuccess = tmpDir.exists();
        if (!isMkdirsSuccess)
            isMkdirsSuccess = tmpDir.mkdirs();
        if (isMkdirsSuccess) {
            // 创建文件
            if (TextUtils.isEmpty(fileName))
                fileName = System.currentTimeMillis() + ".png";
            File imgFile = new File(tmpDir.getAbsolutePath() + fileName);
            try {
                // bitmap写入文件
                fos = new FileOutputStream(imgFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                if (bitmapFunCompressFormat != null) {
                    if (bitmapFunCompressFormat == BitmapFunCompressFormat.JPEG)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    else if (bitmapFunCompressFormat == BitmapFunCompressFormat.WEBP)
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, fos);
                }
                fos.flush();
                fos.close();
                uri = Uri.fromFile(imgFile);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return uri;
    }

    /**
     * 将bitmap转换成图片文件，写入SD卡
     *
     * @param bitmap 待转换位图
     * @return 文件路径
     */
    public static String saveBitmapToImgPath(@NonNull Bitmap bitmap) {
        return saveBitmapToImgPath(bitmap, TEMP_PATH, BitmapFunCompressFormat.PNG);
    }

    /**
     * 将Bitmap转换成图片文件，写入SD卡
     *
     * @param bitmap                  Bitmap数据源
     * @param filePath                保存SD卡文件路径
     * @param bitmapFunCompressFormat 图片格式
     * @return ImagePath
     */
    public static String saveBitmapToImgPath(@NonNull Bitmap bitmap, @NonNull String filePath,
                                             BitmapFunCompressFormat bitmapFunCompressFormat) {
        FileOutputStream fos = null;
        String imgPath = null;
        // 创建文件夹
        File tmpDir = new File(filePath);
        boolean isMkdirsSuccess = tmpDir.exists();
        if (!isMkdirsSuccess)
            isMkdirsSuccess = tmpDir.mkdirs();
        if (isMkdirsSuccess) {
            // 保存文件路径
            imgPath = tmpDir.getAbsolutePath() + System.currentTimeMillis() + ".png";
            // 创建文件
            File imgFile = new File(imgPath);
            try {
                // bitmap写入文件
                fos = new FileOutputStream(imgFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                if (bitmapFunCompressFormat != null) {
                    if (bitmapFunCompressFormat == BitmapFunCompressFormat.JPEG)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    else if (bitmapFunCompressFormat == BitmapFunCompressFormat.WEBP)
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, fos);
                }

                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return imgPath;
    }

    /**
     * 转化成带删除按钮的图片
     *
     * @param context        上下文对象
     * @param x              图像的宽度
     * @param y              图像的高度
     * @param image          源图片
     * @param outerRadiusRat 圆角的大小
     * @return 圆角图片
     */
    public static Bitmap bitmapWithDelImg(@NonNull Context context, int x, int y, @NonNull Bitmap image, float outerRadiusRat) {
        // 根据源文件新建一个darwable对象
        Drawable imageDrawable = new BitmapDrawable(context.getResources(), image);

        // 新建一个新的输出图片
        Bitmap output = Bitmap.createBitmap(x, y, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // 新建一个矩形
        RectF outerRect = new RectF(0, 0, x, y);

        // 产生一个红色的圆角矩形
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        canvas.drawRoundRect(outerRect, outerRadiusRat, outerRadiusRat, paint);

        // 将源图片绘制到这个圆角矩形上
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        imageDrawable.setBounds(0, 0, x, y);
        canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
        imageDrawable.draw(canvas);
        canvas.restore();

        return output;
    }

    /**
     * 获取视频第一帧 - 子线程
     *
     * @param path 视频地址
     */
    public static Bitmap getVideoThumb(@NonNull String path) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            String formatPath = path.toLowerCase();
            if (formatPath.startsWith("http://")
                    || formatPath.startsWith("https://")
                    || formatPath.startsWith("widevine://")) {
                // 网络
                retriever.setDataSource(path, new Hashtable<String, String>());
            } else {
                // 本地
                retriever.setDataSource(path);
            }
            bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * Bitmap转File
     *
     * @param bitmap 待转换数据
     */
    public static File bitmapToFile(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        File tempFile = new File(TEMP_PATH + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            fos = new FileOutputStream(tempFile);
            is = new ByteArrayInputStream(baos.toByteArray());
            int x;
            byte[] b = new byte[1024 * 100];
            while ((x = is.read(b)) != -1) {
                fos.write(b, 0, x);
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tempFile;
    }

}
