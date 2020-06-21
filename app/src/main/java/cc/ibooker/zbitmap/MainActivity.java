package cc.ibooker.zbitmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cc.ibooker.zbitmaplib.BitmapCache;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bitmap方法类BtimapFun
        BitmapFun.imgPathToBitmap2("imagePath");
        // bitmap缓存类BitmapCache
        BitmapCache bitmapCache = new BitmapCache();
        bitmapCache.removeBitmapFromCache("imagePath");
    }
}
