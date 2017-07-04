package example.com.androidbeautypupil;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import example.com.androidbeautypupil.bean.event.ScrollEvent;
import example.com.androidbeautypupil.manager.EventBusManager;
import example.com.androidbeautypupil.manager.ThreadTask;
import example.com.androidbeautypupil.util.FileUtil;
import topplus.com.beautypupil.StaticPupilTexture;

/**
 * @author fandong
 * @date 2016/9/30
 * @description
 */

public class BeautyImageView extends ImageView {

    private String path;

    private Context mContext;

    private boolean isScroll;

    private float ratio;
    private float r;


    public BeautyImageView(Context context) {
        super(context);
        this.mContext = context;
    }

    public BeautyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        EventBusManager.register(this);
    }

    public BeautyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        EventBusManager.register(this);
    }

    @TargetApi(21)
    public BeautyImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        EventBusManager.register(this);
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScrollEvent(ScrollEvent event) {
        this.isScroll = event.isScroll();
    }

    public void applyEffect(String image_path, int rotate, float focusLength, float alpha, float scale) {

        new ThreadTask<Object, Void, Bitmap>() {
            private String bpPath;

            @Override
            protected Bitmap doInBackground(Object... params) {
                this.bpPath = (String) params[0];
                String imagePath = (String) params[1];
                float focus = (float) params[2];
                float al = (float) params[3];
                float sc = (float) params[4];
                int r = (int) params[5];
                if (isScroll) {
                    return null;
                }
                //1.获取高光图片的本地路径(要求是200x200的png图片)
                String highlight = BuildConfig.FLAVOR.equalsIgnoreCase("KEEDE") ?
                        FileUtil.getPathByType(FileUtil.DIR_TYPE_CACHE) + "highlight.png" : null;
                Bitmap target = new StaticPupilTexture.Builder(mContext)
                        //1.设置原图片的绝对路径
                        .setImagePath(imagePath)
                        //2.设置bp文件的绝对路径
                        .setBpPath(bpPath)
                        //3.设置显示宽度是脸宽的ratio倍(可选，如果没有设置，则按照方式一展示)
                        .setRatio(2.f)
                        //4.设置显示高度是显示宽度的r倍(可选，如果没有设置，则按照方式一展示)
                        .setR(1.f)
                        //5.设置高光图片的路径(可选，如果没有设置，则不显示高光)
                        .setHighlight(highlight)
                        //6.设置原图片的焦距，获取图片的焦距方法如文档所示(可选，如果没有设置，默认为22.f)
                        .setFocusLength(focus)
                        //7.设置瞳片的放大倍数
                        .setScale(sc)
                        //8.设置瞳片的透明度
                        .setAlpha(al)
                        .setEnableHDMI(true)
                        .setRotation(r)
                        //9.试戴瞳片
                        .build();
                if (null != target && !target.isRecycled()) {
                    return target;
                } else {
                    return null;
                }

            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                //显示
                if (!isScroll) {
                    if (null != bitmap && !bitmap.isRecycled()) {
                        if (this.bpPath.equals(getPath())) {
                            setImageBitmap(bitmap);
                        } else {
                            bitmap.recycle();
                        }
                    }
                }
            }
        }.execute(path, image_path, focusLength, alpha, scale, rotate);
    }

    public void applyEffect(String image_path, final Bitmap bitmap, int rotate, float focusLength, float alpha, float scale) {

        new ThreadTask<Object, Void, Bitmap>() {
            private String bpPath;

            @Override
            protected Bitmap doInBackground(Object... params) {
                this.bpPath = (String) params[0];
                String imagePath = (String) params[1];
                float focus = (float) params[2];
                float al = (float) params[3];
                float sc = (float) params[4];
                int r = (int) params[5];
                if (isScroll) {
                    return null;
                }
                //1.获取高光图片的本地路径(要求是200x200的png图片)
                String highlight = BuildConfig.FLAVOR.equalsIgnoreCase("KEEDE") ?
                        FileUtil.getPathByType(FileUtil.DIR_TYPE_CACHE) + "highlight.png" : null;
                Bitmap target = new StaticPupilTexture.Builder(mContext)
                        //1.设置原图片的绝对路径
                        .setImagePath(imagePath)
                        .setBitmap(bitmap)
                        //2.设置bp文件的绝对路径
                        .setBpPath(bpPath)
                        //3.设置显示宽度是脸宽的ratio倍(可选，如果没有设置，则按照方式一展示)
                        .setRatio(2.f)
                        //4.设置显示高度是显示宽度的r倍(可选，如果没有设置，则按照方式一展示)
                        .setR(1.f)
                        //5.设置高光图片的路径(可选，如果没有设置，则不显示高光)
                        .setHighlight(highlight)
                        //6.设置原图片的焦距，获取图片的焦距方法如文档所示(可选，如果没有设置，默认为22.f)
                        .setFocusLength(focus)
                        //7.设置瞳片的放大倍数
                        .setScale(sc)
                        //8.设置瞳片的透明度
                        .setAlpha(al)
                        .setEnableHDMI(false)
                        .setRotation(r)
                        //9.试戴瞳片
                        .build();
                if (null != target && !target.isRecycled()) {
                    return target;
                } else {
                    return null;
                }

            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                //显示
                if (!isScroll) {
                    if (null != bitmap && !bitmap.isRecycled()) {
                        if (this.bpPath.equals(getPath())) {
                            setImageBitmap(bitmap);
                        } else {
                            bitmap.recycle();
                        }
                    }
                }
            }
        }.execute(path, image_path, focusLength, alpha, scale, rotate);
    }

    public void destroy() {
        if (EventBusManager.isRegister(this)) {
            EventBusManager.unregister(this);
        }
    }
}
