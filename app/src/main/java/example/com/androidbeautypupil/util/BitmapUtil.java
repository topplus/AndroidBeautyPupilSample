package example.com.androidbeautypupil.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import example.com.androidbeautypupil.R;
import example.com.androidbeautypupil.bean.Exif;
import example.com.androidbeautypupil.bean.ExifBitmap;
import threethird.it.sephiroth.android.library.exif2.ExifInterface;
import threethird.it.sephiroth.android.library.exif2.ExifTag;

import static example.com.androidbeautypupil.BeautyApplication.gContext;

/**
 * @author fandong
 * @date 2016/9/13
 * @description
 */

public class BitmapUtil {

    public static Bitmap decodeSampledBitmapFromResource(String filePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((height / inSampleSize) >= reqHeight && (width / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    //根据图片rotation信息，旋转图片
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.Orientation.TOP_LEFT:
                return bitmap;
            case ExifInterface.Orientation.TOP_RIGHT:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.Orientation.BOTTOM_RIGHT:
                matrix.setRotate(180);
                break;
            case ExifInterface.Orientation.BOTTOM_LEFT:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.Orientation.LEFT_TOP:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.Orientation.RIGHT_TOP:
                matrix.setRotate(90);
                break;
            case ExifInterface.Orientation.RIGHT_BOTTOM:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.Orientation.LEFT_BOTTOM:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * ORIENTATION_NORMAL = 1;
     * ORIENTATION_ROTATE_180 = 3;
     * ORIENTATION_ROTATE_270 = 8;
     * ORIENTATION_ROTATE_90 = 6;
     * ORIENTATION_TRANSPOSE = 5;
     * ORIENTATION_TRANSVERSE = 7;
     * ORIENTATION_UNDEFINED = 0;
     */
    public static Exif getExif(String absolutePath) {
        int rotation = ExifInterface.Orientation.TOP_LEFT;
        Exif exif = new Exif();
        try {
            ExifInterface exifInterface = new ExifInterface();
            exifInterface.readExif(absolutePath, ExifInterface.Options.OPTION_ALL);
            ExifTag focalTag = exifInterface.getTag(ExifInterface.TAG_FOCAL_LENGTH_IN_35_MM_FILE);
            float focalLength = 22f;
            if (focalTag != null) {
                focalTag.getIfd();
                focalLength = focalTag.getValueAsInt(22);
            }
            if (focalLength < 10 || focalLength > 40) {
                exif.setFocusLength(22f);
            } else {
                exif.setFocusLength(focalLength);
            }
            ExifTag rotaTag = exifInterface.getTag(ExifInterface.TAG_ORIENTATION);
            if (rotaTag != null) {
                rotation = rotaTag.getValueAsInt(0);
                if (rotation == 1) {
                    rotation = 0;
                } else if (rotation == 6) {
                    rotation = 90;
                } else if (rotation == 3) {
                    rotation = 180;
                } else if (rotation == 8) {
                    rotation = 270;
                }
            }
            exif.setRotate(rotation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exif;
    }

    public static Bitmap clipBitmap(Bitmap bitmap) {
        try {
            if (bitmap == null || bitmap.isRecycled()) {
                return null;
            }
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int length = Math.min(width, height);
            if (height > width) {
                int start = (height - length) / 2;
                return Bitmap.createBitmap(bitmap, 0, start, length, length);
            } else if (height < width) {
                int start = (width - length) / 2;
                return Bitmap.createBitmap(bitmap, start, 0, length, length);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    public static boolean loadImage(View view, String url) {
        try {
            if (null != view) {
                Context context = view.getContext();
                Bitmap bitmap = null;
                int width = view.getWidth();
                int height = view.getHeight();
                if (null != context && !TextUtils.isEmpty(url)) {
                    if (url.startsWith("assets://")) {
                        AssetManager manager = context.getAssets();
                        String fileName = url.substring("assets://".length());
                        if (null != manager) {
                            InputStream ins = manager.open(fileName);
                            if (null != ins) {
                                //1.宽高，计算sample
                                bitmap = inputStreamBitmap(ins, width, height);
                            }
                        }
                    } else if (url.startsWith("drawable://")) {
                        String fileName = url.substring("drawable://".length());
                        InputStream inputStream = context.getResources().openRawResource(Integer.valueOf(fileName));
                        if (null != inputStream) {
                            bitmap = inputStreamBitmap(inputStream, width, height);
                        }
                    } else if (url.startsWith("file://")) {
                        String fileName = url.substring("file://".length());
                        if (!TextUtils.isEmpty(fileName)) {
                            File file = new File(fileName);
                            if (file.exists()) {
                                bitmap = inputStreamBitmap(new FileInputStream(file), width, height);
                            }
                        }
                    }
                }
                if (null != bitmap) {
                    if (view instanceof ImageView) {
                        ((ImageView) view).setImageBitmap(bitmap);
                        return true;
                    }
                    if (view instanceof ImageSwitcher) {
                        ((ImageSwitcher) view).setImageDrawable(new BitmapDrawable(bitmap));
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static ExifBitmap loadImageBitmap(Context context, String url, int width, int height) {
        try {
            ExifBitmap bitmap = new ExifBitmap();
            if (null != context && !TextUtils.isEmpty(url)) {
                if (url.startsWith("assets://")) {
                    AssetManager manager = context.getAssets();
                    String fileName = url.substring("assets://".length());
                    if (null != manager) {
                        InputStream ins = manager.open(fileName);
                        if (null != ins) {
                            bitmap.setBitmap(inputStreamBitmap(ins, width, height));
                        }
                    }
                } else if (url.startsWith("drawable://")) {
                    String fileName = url.substring("drawable://".length());
                    InputStream inputStream = context.getResources().openRawResource(Integer.valueOf(fileName));
                    if (null != inputStream) {
                        bitmap.setBitmap(inputStreamBitmap(inputStream, width, height));
                    }
                } else if (url.startsWith("file://")) {
                    String fileName = url.substring("file://".length());
                    if (!TextUtils.isEmpty(fileName)) {
                        File file = new File(fileName);
                        Exif exif = getExif(fileName);
                        if (file.exists()) {
                            Bitmap originBitmap = inputStreamBitmap(new FileInputStream(file), width, height);
                            if (null != exif && exif.getRotate() != ExifInterface.Orientation.TOP_LEFT) {
                                originBitmap = rotateBitmap(originBitmap, exif.getRotate());
                            }
                            bitmap.setBitmap(originBitmap);
                            bitmap.setExif(exif);
                        }
                    }
                }
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap inputStreamBitmap(InputStream inputStream, int width, int height) {
        Bitmap bitmap = null;
        try {
            if (width > 0 && height > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = inputStream.read(buffer)) > 0) {
                    bos.write(buffer, 0, length);
                }
                inputStream.close();
                byte[] data = bos.toByteArray();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data, 0, data.length, options);

                int sample = calculateInSampleSize(options, width, height);
                sample = sample > 1 ? sample : 1;
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inSampleSize = sample;
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
            } else {
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap loadAvailBitmap(String fileName) {
        if (new File(fileName).exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileName, options);
            int width = gContext.getResources().getDisplayMetrics().widthPixels;
            int ratio = Math.round(options.outWidth / (float) width);
            options.inSampleSize = ratio;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = false;
            options.inPurgeable = true;
            return BitmapFactory.decodeFile(fileName, options);
        }
        return BitmapFactory.decodeResource(gContext.getResources(),
                R.mipmap.ic_launcher);
    }


    /**
     * 将bitmap按照比例进行裁剪
     *
     * @param bitmap      原始的bitmap
     * @param widthRatio  宽度的比例值
     * @param heightRatio 高度的比例值
     * @return
     */
    public static Bitmap clipBitmap(Bitmap bitmap, int widthRatio, int heightRatio) {
        try {
            if (bitmap == null || bitmap.isRecycled()) {
                return null;
            }
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            //1.如果是长图
            if (h / (float) w > heightRatio / (float) widthRatio) {
                float targetH = w / (float) widthRatio * heightRatio;
                float y = (h - targetH) / 2.f;
                Bitmap bmp = Bitmap.createBitmap(bitmap, 0, (int) y, w, (int) targetH);
                bitmap.recycle();
                return bmp;
            } else if (w / (float) h > widthRatio / (float) heightRatio) {
                float targetW = h * widthRatio / (float) heightRatio;
                float x = (w - targetW) / 2.f;
                Bitmap bmp = Bitmap.createBitmap(bitmap, (int) x, 0, (int) targetW, h);
                bitmap.recycle();
                return bmp;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 对图片进行缩放操作
     *
     * @param reW 缩放后的宽度
     * @return
     */
    public static Bitmap scaleBitmap(String path, int reW, int reH) {
        if (!new File(path).exists()) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int size = computeSampleSize(options, -1, reW * reH);
        options.inSampleSize = size;
        float scale = reW * 1.0f / options.outWidth;
        int height = (int) (reH * scale);
        options.outWidth = reW;
        options.outHeight = height;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = null;
        return BitmapFactory.decodeFile(path, options);
    }

    //动态计算图片内存大小
    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * 对图片进行等比例缩放
     *
     * @param bitmap
     * @param reW
     * @param reH
     * @return
     */
    public static Bitmap scaleEqualBase(Bitmap bitmap, int reW, int reH) {
        if (bitmap == null) {
            return null;
        }

        int bmpW = bitmap.getWidth();
        int bmpH = bitmap.getHeight();

        float wScale = reW * 1.0f / bmpW;
        float hScale = reH * 1.0f / bmpH;

        float scale = Math.max(wScale, hScale);

        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);

        return Bitmap.createBitmap(bitmap, 0, 0, bmpW, bmpH, matrix, true);
    }

    /**
     * 对某个view截屏
     *
     * @param view 需要截屏的视图
     * @return
     */
    public static Bitmap getBitmap(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(canvas);
        return bitmap;
    }

    /**
     * 对某个位图保存成文件
     *
     * @param bitmap 需要保存的位图
     * @return 保存文件的路径
     */
    public static String saveBitmap(Bitmap bitmap) {
        return saveBitmap(bitmap, FileUtil.DIR_TYPE_CACHE);
    }

    /**
     * 对某个位图保存成文件
     *
     * @param bitmap 需要保存的位图
     * @return 保存文件的路径
     */
    public static String saveBitmap(Bitmap bitmap, int dirType) {
        if (null == bitmap) {
            return null;
        }
        String dirPath = FileUtil.getPathByType(FileUtil.DIR_TYPE_CACHE);
        File parent = new File(dirPath);

        String fileName = System.currentTimeMillis() + ".jpg";
        File newFile = new File(parent, fileName);
        FileOutputStream mFileOutputStream = null;
        try {
            newFile.createNewFile();
            mFileOutputStream = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    mFileOutputStream);
            //如果是保存到系统相册,发广播,更新相册
            if (dirType == FileUtil.DIR_TYPE_CACHE) {
                MediaScannerConnection.scanFile(gContext, new String[]{newFile.getAbsolutePath()},
                        new String[]{"image/jpeg"}, null);
            }
            return newFile.getAbsolutePath();
        } catch (IOException localIOException) {
            localIOException.printStackTrace();
        } finally {
            if (null != mFileOutputStream) {
                try {
                    mFileOutputStream.flush();
                    mFileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


}
