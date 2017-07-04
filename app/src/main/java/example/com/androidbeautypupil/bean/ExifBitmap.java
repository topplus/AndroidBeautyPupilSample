package example.com.androidbeautypupil.bean;

import android.graphics.Bitmap;

/**
 * @author fandong
 * @date 2016/9/13
 * @description 带exif信息的位图
 */

public class ExifBitmap {
    private Exif exif;
    private Bitmap bitmap;

    public Exif getExif() {
        return exif;
    }

    public void setExif(Exif exif) {
        this.exif = exif;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
