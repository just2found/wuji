package net.linkmate.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import net.linkmate.app.base.MyConstants;

import java.io.File;
import java.io.FileOutputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class StoreViewUtils {


    // 把一个View转换成图片
    // 添加水印
    public void viewSaveToImage(View view, String file_name, String tag) {
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        view.setDrawingCacheBackgroundColor(Color.WHITE);
        view.buildDrawingCache();

        // 把一个View转换成图片
        Bitmap cachebmp = loadBitmapFromView(view);

        // 添加水印
        Bitmap bitmap = Bitmap.createBitmap(createWatermarkBitmap(cachebmp, tag));

        FileOutputStream fos;
        try {
            // 判断手机设备是否有SD卡
            boolean isHasSDCard = Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED);
            if (isHasSDCard) {
                // SD卡根目录
                File sdRoot = new File(MyConstants.DEFAULT_EXTERNAL_FOLDER_IMG);
                if (!sdRoot.exists()) sdRoot.mkdir();
                if (!(file_name != null && file_name.length() > 0)) {
                    file_name = "IMG_" + System.currentTimeMillis() / 1000;
                }
                File file = new File(sdRoot, file_name + ".PNG");
                fos = new FileOutputStream(file);
            } else
                throw new Exception("Store View to a file Failed!");

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.flush();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        view.invalidate();
        view.requestLayout();
    }


    public Observable<Boolean> viewSaveToImage2(final View view, final File file, final String tag) {
        if (view == null) {
            Log.e(this.getClass().getSimpleName(), "view enable?");
            return null;
        }
        if (file == null) {
            Log.e(this.getClass().getSimpleName(), "file can't be null");
            return null;
        }
        WindowManager wm = (WindowManager) view.getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        //屏幕的宽度
        int width = outMetrics.widthPixels;
        //recycleview 截屏，重要方法
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        view.setDrawingCacheBackgroundColor(Color.WHITE);
        view.buildDrawingCache();

        // 把一个View转换成图片
        final Bitmap cachebmp = view.getDrawingCache();

        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
//                // 添加水印
                Bitmap bitmap = TextUtils.isEmpty(tag) ? cachebmp : Bitmap.createBitmap(createWatermarkBitmap(cachebmp, tag));
                FileOutputStream fos;
                fos = new FileOutputStream(file);
                boolean compress = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                emitter.onNext(compress);
                fos.flush();
                fos.close();


            }
        });


    }

    public Observable<Boolean> viewSaveToImage(final View view, final File file, final String tag) {
        if (view == null || view.getHeight() <= 0 || view.getWidth() <= 0) {
            Log.e(this.getClass().getSimpleName(), "view enable?");
            return null;
        }
        if (file == null) {
            Log.e(this.getClass().getSimpleName(), "file can't be null");
            return null;
        }
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        view.setDrawingCacheBackgroundColor(Color.WHITE);
        view.buildDrawingCache();

        // 把一个View转换成图片
        final Bitmap cachebmp = loadBitmapFromView(view);

        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        view.invalidate();
        view.requestLayout();
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                // 添加水印
                Bitmap bitmap = Bitmap.createBitmap(createWatermarkBitmap(cachebmp, tag));
                FileOutputStream fos;
                fos = new FileOutputStream(file);
                boolean compress = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                emitter.onNext(compress);
                fos.flush();
                fos.close();
            }
        });


    }

    private Bitmap loadBitmapFromView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        c.drawColor(Color.WHITE);
        /** 如果不设置canvas画布为白色，则生成透明 */

        v.layout(0, 0, w, h);
        v.draw(c);

        return bmp;
    }

    // 为图片target添加水印
    private Bitmap createWatermarkBitmap(Bitmap target, String str) {
        int w = target.getWidth();
        int h = target.getHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint p = new Paint();

        // 水印的颜色
        p.setColor(Color.RED);

        // 水印的字体大小
        p.setTextSize(16);

        p.setAntiAlias(true);// 去锯齿

        canvas.drawBitmap(target, 0, 0, p);

        // 在中间位置开始添加水印
        canvas.drawText(str, w / 2, h / 2, p);

//        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.save();
        canvas.restore();

        return bmp;
    }
}