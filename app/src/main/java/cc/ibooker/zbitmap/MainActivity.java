package cc.ibooker.zbitmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cc.ibooker.zbitmaplib.BitmapCacheUtil;
import cc.ibooker.zbitmaplib.BitmapUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bitmap方法类BtimapFun
        BitmapUtil.imgPathToBitmap("imagePath");
        // bitmap缓存类BitmapCache
        BitmapCacheUtil bitmapCacheUtil = new BitmapCacheUtil();
        bitmapCacheUtil.removeBitmapFromCache("imagePath");
    }
}
