package example.com.androidbeautypupil.manager;


import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import example.com.androidbeautypupil.bean.ExifBitmap;

/**
 * @author fandong
 * @date 2016/9/30
 * @description
 */

public class BitmapManager {
    private static BitmapManager sManager;

    //    private HashMap<String, ExifBitmap> sCache;
    private LruCache<String, ExifBitmap> sCache;

    private BitmapManager() {

    }

    public static BitmapManager getInstance() {
        if (null == sManager) {
            sManager = new BitmapManager();
            sManager.checkCache();
        }
        return sManager;
    }

    public static String encode(String pwd) {
        StringBuffer sb = new StringBuffer();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(pwd.getBytes());
            for (int i = 0; i < bytes.length; i++) {
                String s = Integer.toHexString(0xff & bytes[i]);
                if (s.length() == 1) {
                    sb.append("0" + s);
                } else {
                    sb.append(s);
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    //---------------存取exifBitmap
    public void putExifBitmap(String path, int width, int height, ExifBitmap bitmap) {
        if (null == getExifBitmap(path, width, height)) {
            sCache.put(encode(path + width + height), bitmap);
        }
    }

    public ExifBitmap removeExifBitmap(String path, String beautyPath) {
        return sCache.remove(encode(path + beautyPath));
    }

    public ExifBitmap getExifBitmap(String path, int width, int height) {
        return sCache.get(encode(path + width + height));
    }

    //--------------存取Bitmap
    public void putBitmap(Bitmap bitmap, String path, int width, int height) {
        //首先查找有没有
        ExifBitmap exifBitmap = getExifBitmap(path, width, height);
        if (null == exifBitmap) {
            exifBitmap = new ExifBitmap();
            exifBitmap.setBitmap(bitmap);
            sCache.put(encode(path + width + height), exifBitmap);
        }
    }

    //--------------遍历
    public void iterate() {
//        Log.e("Topplus", "size:" + sCache.size());
//        for (Map.Entry<String, ExifBitmap> entry : sCache.entrySet()) {
//            Log.e("Topplus", entry.getKey() + "---" + entry.getValue().getBitmap().hashCode());
//        }
    }

    public void removeBitmap(String path, int width, int height) {
        //首先查找有没有
        sCache.remove(encode(path + width + height));
    }

    public Bitmap getBitmap(String path, int width, int height) {
        ExifBitmap exifBitmap = sCache.get(encode(path + width + height));
        if (null != exifBitmap) {
            return exifBitmap.getBitmap();
        }
        return null;
    }

    public void clear() {
        sCache.evictAll();
    }

    public void checkCache() {
        if (null == sCache) {
//            sCache = new HashMap<>();
            sCache = new LruCache<>(10);
        }
    }

}
