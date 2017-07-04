package example.com.androidbeautypupil;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import example.com.androidbeautypupil.adapter.PupilAdapter;
import example.com.androidbeautypupil.bean.Exif;
import example.com.androidbeautypupil.bean.ExifBitmap;
import example.com.androidbeautypupil.listener.OnSeekBarChangeAdapter;
import example.com.androidbeautypupil.manager.BitmapManager;
import example.com.androidbeautypupil.manager.BpManager;
import example.com.androidbeautypupil.manager.ThreadTask;
import example.com.androidbeautypupil.util.BitmapUtil;
import example.com.androidbeautypupil.util.FileUtil;
import example.com.androidbeautypupil.util.PreferenceUtil;
import topplus.com.beautypupil.StaticPupilTexture;
import uk.co.senab.photoview.PhotoView;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private PhotoView mImageView;
    private float mFocusLength = 22f;
    private int mRotation = 0;
    private float mScale = 1.f;
    private float mAlpha = 0.5f;
    private SeekBar mColorBar;
    private SeekBar mSizeBar;


    //调节大小的进度条监听
    private OnSeekBarChangeAdapter mSizeChangeAdapter;
    //调节颜色的进度条监听
    private OnSeekBarChangeAdapter mColorChangeAdapter;
    //上一次BP文件的本地路径
    private String mBPAbsolutePath;
    //图片路径
    private String mPath;
    //原图 // TODO: 17/2/10 对比原图
    private Bitmap originBitmap;
    //戴上彩瞳图
    private Bitmap beautyBitmap;
    //文字
    private TextView mMainTip;

    //列表按钮
    private TextView mListBtn;

    private RecyclerView mRecyclerView;

    private int mTargetWidth;
    private int mTargetHeight;
    private RecyclerView.OnScrollListener mOnScrollListener;
    private PupilAdapter mAdapter;
    private PupilAdapter.OnItemClickListener mOnItemClickListener;
    private View.OnTouchListener mOnTouchListener;
    private Dialog mDialog;
    private int mLastSelected;
    //从列表进来的时候会有显示问题，需要update一下
    private boolean mIsUpdated;
    private float r;
    private float ratio;
    //批量调试的按钮
    private TextView mFolderDebug;
    //是否需要隐藏list
    private boolean mIsHideList;
    private ExifBitmap mExifBitmap;
    private boolean mIsValidated;

    {
        //1.显示高度是宽度的r倍
        this.r = 1.35f;
        //2.显示的宽度是人脸宽度的ratio倍
        this.ratio = 1.0f;
        this.mOnItemClickListener = new PupilAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position != mLastSelected) {
                    mLastSelected = position;
                    //1.改变背景颜色
                    for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
                        View v = mRecyclerView.getChildAt(i);
                        if (v != null) {
                            v.setBackgroundResource(R.drawable.vw_pupil_icon_bg);
                        }
                    }
                    view.setBackgroundResource(R.drawable.vw_pupil_icon_press);
                    //2.RecyclerView侧滑动
                    int left = view.getLeft();
                    int size = getResources().getDisplayMetrics().widthPixels / 4;
                    int screen = getResources().getDisplayMetrics().widthPixels;
                    int distance = (int) (left - screen / 2.f + size / 2.f);
                    mRecyclerView.smoothScrollBy(distance, 0);
                    //3.加载眼镜
                    mBPAbsolutePath = mAdapter.getBpPath(position);
                    applyEffect(mBPAbsolutePath);

                }
            }
        };
        this.mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                }
            }
        };
        this.mSizeChangeAdapter = new OnSeekBarChangeAdapter() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mScale = (float) (0.002 * progress + 0.8);

                applyEffect(mBPAbsolutePath);
                //将progress存放到本地
                PreferenceUtil.putInt(MainActivity.this, "scale", progress);
            }
        };
        this.mColorChangeAdapter = new OnSeekBarChangeAdapter() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mAlpha = seekBar.getProgress() / (float) seekBar.getMax();
                applyEffect(mBPAbsolutePath);

                //将progress存放到本地
                PreferenceUtil.putInt(MainActivity.this, "color", seekBar.getProgress());
            }
        };

        this.mOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mImageView.setImageBitmap(originBitmap);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mImageView.setImageBitmap(beautyBitmap);
                }
                return false;
            }
        };
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public static void launch(Context context, boolean hideList, String image_path, String beautyPath) {
        Intent intent = new Intent(context, MainActivity.class);
        //是否隐藏列表按钮
        intent.putExtra("hideList", hideList);
        //图片路径
        intent.putExtra("path", image_path);
        //bp文件的路径
        intent.putExtra("beautyPath", beautyPath);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //2.初始化试图
        setContentView(R.layout.activity_main);
        mImageView = (PhotoView) findViewById(R.id.image_view);
        mMainTip = (TextView) findViewById(R.id.main_tip);
        mImageView.setMinimumScale(1.f);
        mImageView.setMaximumScale(3.f);
        mColorBar = (SeekBar) findViewById(R.id.color_bar);
        mSizeBar = (SeekBar) findViewById(R.id.size_bar);
        mFolderDebug = (TextView) findViewById(R.id.folder);
        if (BuildConfig.FLAVOR.equals("KEEDE")) {
            findViewById(R.id.control_layout).setVisibility(View.GONE);
        } else {
            int scale = PreferenceUtil.getInt(MainActivity.this, "scale", -1);
            if (scale > -1) {
                mSizeBar.setProgress(scale);
                mScale = (float) (0.23 * scale / mSizeBar.getMax() + 0.82);
            }
            mSizeBar.setOnSeekBarChangeListener(mSizeChangeAdapter);

            int color = PreferenceUtil.getInt(MainActivity.this, "color", -1);
            if (color > -1) {
                mColorBar.setProgress(color);
                mAlpha = color / (float) mColorBar.getMax();
            }
            mColorBar.setOnSeekBarChangeListener(mColorChangeAdapter);
        }

        this.mTargetWidth = getResources().getDisplayMetrics().widthPixels;
        this.mTargetHeight = getResources().getDisplayMetrics().heightPixels;
        this.mListBtn = (TextView) findViewById(R.id.list);
        //4.解析
        Intent intent = getIntent();
        if (intent != null) {
            this.mIsHideList = intent.getBooleanExtra("hideList", false);
            if (mIsHideList) {
                findViewById(R.id.pick_btn).setVisibility(View.GONE);
                findViewById(R.id.control_layout).setVisibility(View.GONE);
                mListBtn.setVisibility(View.GONE);
                mMainTip.setVisibility(View.GONE);
            }
            this.mPath = intent.getStringExtra("path");
            this.mBPAbsolutePath = intent.getStringExtra("beautyPath");
        }
        //5.批量调试
        if (BuildConfig.DEBUG && !this.mIsHideList) {
            mFolderDebug.setVisibility(View.VISIBLE);
        } else {
            mFolderDebug.setVisibility(View.GONE);
        }
        //6.初始化recyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //一屏幕显示四个
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRecyclerView.getLayoutParams();
        params.height = getResources().getDisplayMetrics().widthPixels / 4;
        mRecyclerView.setLayoutParams(params);

        mRecyclerView.addOnScrollListener(mOnScrollListener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        this.mAdapter = new PupilAdapter(this);
        this.mAdapter.setOnItemClickListener(mOnItemClickListener);
        //7.初始化资源文件
        initResource();
    }

    private void initResource() {
        new ThreadTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                if (!PreferenceUtil.getBoolean(MainActivity.this, "has_resource_config", false)) {
                    mDialog = new AlertDialog.Builder(MainActivity.this)
                            .setMessage("正在准备资源文件，请稍候！")
                            .setCancelable(false)
                            .show();
                }


            }

            @Override
            protected Boolean doInBackground(Void... params) {
                /*
                if (PreferenceUtil.getBoolean(MainActivity.this, "has_resource_config", false)) {
                    return BpManager.getInstance().initBpFiles();
                }*/
                return BpManager.getInstance().copyAssets();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                dismissDialog();
                if (result) {
                    //1.保存是否初始化
                    PreferenceUtil.putBoolean(MainActivity.this, "has_resource_config", true);
                    //2.初始化adapter
                    mAdapter.setPath(BpManager.getInstance().getPupilPaths());
                    mRecyclerView.setAdapter(mAdapter);
                    //3.带上第一个瞳片
                    mBPAbsolutePath = BpManager.getInstance().getBeautyPupilPath(0);
                    //
                    init();
                    //applyEffect(mBPAbsolutePath);
                } else {
                    mDialog = new AlertDialog.Builder(MainActivity.this)
                            .setMessage("初始化失败，请重试")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (null != mDialog && mDialog.isShowing()) {
                                        mDialog.dismiss();
                                        mDialog = null;
                                    }
                                }
                            })
                            .show();
                }
            }
        }.execute();
    }

    private void dismissDialog() {
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //1.清理内存
        System.gc();
    }

    private void init() {
        if (!TextUtils.isEmpty(this.mPath)) {
            initOriginBitmap();
            initScaleBitmap();
        }
    }

    private void loadImage(Uri uri) {
        String path = getRealPathFromURI(uri);
        File image = new File(path);
        if (!image.exists()) {
            Toast.makeText(getApplicationContext(), "图片已不存在", Toast.LENGTH_LONG).show();
        } else {
            this.mPath = image.getAbsolutePath();
            //// TODO: 17/2/20 测试不同处理方式的图片参数
            /*Bitmap origin = BitmapFactory.decodeFile(image.getAbsolutePath());
            Log.i("MainActivity", "\n bitmapW=" + origin.getWidth() + "\n bitmapH=" + origin.getHeight() + "\n bitSize=" + origin.getByteCount());
            Bitmap origin2 = BitmapUtil.loadAvailBitmap(image.getAbsolutePath());
            Log.i("MainActivity", "\n bitmap2W=" + origin2.getWidth() + "\n bitmap2H=" + origin2.getHeight() + "\n bit2Size=" + origin2.getByteCount());*/
            long startTime = System.currentTimeMillis();
            ExifBitmap origin3 = BitmapUtil.loadImageBitmap(getApplicationContext()
                    , "file://" + mPath
                    , mTargetWidth
                    , mTargetHeight);
            long endTime = System.currentTimeMillis();
            Log.i("MainActivity", "\n bitmap3W=" + origin3.getBitmap().getWidth() + "\n bitmap3H=" + origin3.getBitmap().getHeight() + "\n bit3Size=" + origin3.getBitmap().getByteCount());
            Log.i("MainActivity", "\n---------------->readTime=" + (endTime - startTime)
                    + "\n 3Size=" + origin3.getBitmap().getByteCount());
            /*Bitmap bitBuffer = BitmapUtil.scaleBitmap(image.getAbsolutePath(), 1300, 2000);
            Log.i("MainActivity", "\n bitmapBufferW=" + bitBuffer.getWidth() + "\n bitmapBufferH=" + bitBuffer.getHeight() + "\n bitmapBufferSize=" + bitBuffer.getByteCount());
            String newPath = BitmapUtil.saveBitmap(bitBuffer, FileUtil.DIR_TYPE_HOME);
            Bitmap newBit = BitmapFactory.decodeFile(newPath);
            Log.i("MainActivity", "\n newBitW=" + newBit.getWidth() + "\n newBitH=" + newBit.getHeight() + "\n bitSize=" + newBit.getByteCount());*/

            //2.显示裁剪的图片
            initOriginBitmap();
            //3.裁剪图片
            initScaleBitmap();
        }
    }

    private void initOriginBitmap() {
        mExifBitmap = BitmapUtil.loadImageBitmap(getApplicationContext()
                , "file://" + mPath
                , mTargetWidth
                , mTargetHeight);
        BitmapManager.getInstance().putExifBitmap(mPath, -1, -1, mExifBitmap);
    }

    /**
     * '
     * 1.不调用getDefaultBitmap，作图的时候就不传ratio 和r
     * 2.如果调用getDefaultBitmap,作图就需要传ratio和r
     */
    private void initScaleBitmap() {
        new ThreadTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                //1.初始化
                Exif exif = mExifBitmap.getExif();
                if (null != exif) {
                    mFocusLength = exif.getFocusLength();
                    mRotation = exif.getRotate();
                } else {
                    mFocusLength = 22.f;
                }
                //2.获取默认大小的bitmap

//                return StaticPupilTexture.getDefaultBitmap(mPath, mRotation, ratio, r, mFocusLength);
                long checkStart = System.currentTimeMillis();
                //Bitmap bitmap = StaticPupilTexture.getDefaultBitmap(mPath, mExifBitmap.getBitmap(), mRotation, ratio, r, mFocusLength);
                long checkEnd = System.currentTimeMillis();
                Log.i("Check Pupil", "\nCheckTime = " + (checkEnd - checkStart));
//                if (bitmap == null){
//                    ExifBitmap exifBitmap = BitmapUtil.loadImageBitmap(getApplicationContext()
//                            , "file://" + mPath
//                            , mTargetWidth
//                            , mTargetHeight);
//                    return exifBitmap.getBitmap();
//                }
//                return bitmap;
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap origin) {
                if (null != origin && !origin.isRecycled()) {
                    originBitmap = origin;
                    mImageView.setImageBitmap(originBitmap);
                } else {
                    ExifBitmap bitmap = BitmapUtil.loadImageBitmap(getApplicationContext()
                            , "file://" + mPath
                            , mTargetWidth
                            , mTargetHeight);
                    originBitmap = bitmap.getBitmap();
                    mImageView.setImageBitmap(originBitmap);
                }
                mImageView.update();
                long checkStart = System.currentTimeMillis();
                applyEffect(mBPAbsolutePath);
                long checkEnd = System.currentTimeMillis();
                Log.i("PutOn Pupil", "\nCheckTime = " + (checkEnd - checkStart));
            }
        }.execute();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pick_btn:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
                break;
            case R.id.list:
                if (TextUtils.isEmpty(mPath)) {
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                    //弹出提示框
                    mDialog = new AlertDialog.Builder(this)
                            .setMessage("请选择照片后点击")
                            .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mDialog != null && mDialog.isShowing()) {
                                        mDialog.dismiss();
                                        mDialog = null;
                                    }
                                }
                            })
                            .show();
                } else {
                    if (mIsValidated) {
                        ListActivity.launch(this, mPath, mRotation, mAlpha, mScale, mFocusLength);
                    } else {
                        Toast.makeText(this, "瞳孔检测失败或者授权失败！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.folder:
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                    mDialog = null;
                }
                //弹出提示框
                mDialog = new AlertDialog.Builder(this)
                        .setMessage("是否已经将测试图片放入了/sdcard/topplus/example.com.androidbeautypupil/image/文件夹下面")
                        .setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (mDialog != null && mDialog.isShowing()) {
                                    mDialog.dismiss();
                                    mDialog = null;
                                }
                            }
                        })
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //1.原来的dialog消失
                                if (mDialog != null && mDialog.isShowing()) {
                                    mDialog.dismiss();
                                    mDialog = null;
                                }
                                //2.批量生成
                                batchBeauty();
                            }
                        })
                        .show();
                break;
            default:
                break;
        }
    }

    private void batchBeauty() {
        new ThreadTask<String, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                //dialog
                mDialog = new AlertDialog.Builder(MainActivity.this)
                        .setMessage("正在批量生成图片，请稍候")
                        .setCancelable(false)
                        .show();
            }

            @Override
            protected Boolean doInBackground(String... params) {
                String directory = params[0];
                File file = new File(directory);
                if (!file.exists() || !file.isDirectory()) {
                    return false;
                }
                File[] files = file.listFiles();
                if (files.length <= 0) {
                    return false;
                }
                for (File f : files) {
                    String path = f.getAbsolutePath();
                    if (path.endsWith(".jpg") || path.endsWith(".JPG")) {
                        float focus = 22.f;
                        ExifBitmap exifBitmap = BitmapUtil.loadImageBitmap(getApplicationContext()
                                , "file://" + path
                                , mTargetWidth
                                , mTargetHeight);
                        Exif exif = exifBitmap.getExif();

                        int rotation = 0;
                        if (null != exif) {
                            focus = exif.getFocusLength();
                            rotation = exif.getRotate();
                        }
                        Log.e("Topplus", "path:" + path
                                + "\nmBPAbsolutePath:" + mBPAbsolutePath
                                + "\nratio:" + ratio
                                + "\nr:" + r
                                + "\nfocus:" + focus
                                + "\nmScale:" + mScale
                                + "\nmAlpha:" + mAlpha);
                        String highlight = BuildConfig.FLAVOR.equalsIgnoreCase("KEEDE") ?
                                FileUtil.getPathByType(FileUtil.DIR_TYPE_CACHE) + "highlight.png" : null;

                        /*Bitmap bitmap = new StaticPupilTexture.Builder(getApplicationContext())
                                .setImagePath(path)
                                .setBpPath(mBPAbsolutePath)
                                //.setRatio(ratio)
                                .setRotation(rotation)
                                //.setR(r)
                                .setHighlight(highlight)
                                .setFocusLength(focus)
                                .setScale(mScale)
                                .setAlpha(mAlpha)
                                .build();*/
                        Bitmap bitmap = new StaticPupilTexture.Builder(getApplicationContext())
                                .setImagePath(path)
                                .setBitmap(exifBitmap.getBitmap())
                                .setBpPath(mBPAbsolutePath)
//                                .setRatio(ratio)
//                                .setR(r)
                                .setRotation(rotation)
                                .setHighlight(highlight)
                                .setFocusLength(mFocusLength)
                                .setScale(mScale)
                                .setAlpha(mAlpha)
                                .build();
                        if (bitmap != null && !bitmap.isRecycled()) {
                            String fileName = path.substring(path.lastIndexOf("/") + 1);

                            String absolutePath = FileUtil.getExternalTargetImageDirectory() + "/" + fileName;
                            File target = new File(absolutePath);
                            FileOutputStream fos = null;
                            try {
                                if (target.createNewFile()) {
                                    fos = new FileOutputStream(target);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (null != fos) {
                                    try {
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            Log.e("Topplus", "bitmap为空");
                        }
                    }
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                    mDialog = null;
                }
                if (!aBoolean) {
                    Toast.makeText(MainActivity.this, "批量生成失败！", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(FileUtil.getExternalImageDirectory());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    mIsUpdated = false;
                    if (mDialog == null || !mDialog.isShowing()) {
                        mDialog = new AlertDialog.Builder(MainActivity.this)
                                .setMessage("正在生成效果图，请稍候")
                                .setCancelable(false)
                                .show();
                    }
                    mMainTip.setVisibility(View.GONE);
                    mImageView.setImageDrawable(new ColorDrawable(0xffffffff));
                    mListBtn.setVisibility(View.VISIBLE);
                    //加载图片并显示
                    loadImage(data.getData());
                    /*String path = getRealPathFromURI(data.getData());
                    File image = new File(path);
                    if (!image.exists()) {
                        Toast.makeText(getApplicationContext(), "图片已不存在", Toast.LENGTH_LONG).show();
                    } else {
                        this.mPath = image.getAbsolutePath();
                        //2.显示裁剪的图片
                        initOriginBitmap();
                        //3.裁剪图片
                        initScaleBitmap();
                    }*/
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        if (null == contentURI) {
            return null;
        }
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    /**
     * 应用美瞳效果
     *
     * @param absolutePath 美瞳的绝对路径
     */
    private void applyEffect(String absolutePath) {
        if (TextUtils.isEmpty(mPath)) {
            Toast.makeText(MainActivity.this, "请选择一张图片", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(absolutePath)) {
            Toast.makeText(MainActivity.this, R.string.select_pupil, Toast.LENGTH_SHORT).show();
            return;
        }
        mBPAbsolutePath = absolutePath;

        new ThreadTask<String, Void, Bitmap>() {
            private String path;

            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    this.path = params[0];
                    //计算角度
                    String highlight = BuildConfig.FLAVOR.equalsIgnoreCase("KEEDE") ?
                            FileUtil.getPathByType(FileUtil.DIR_TYPE_CACHE) + "highlight.png" : null;
                    Log.e("Topplus", "mPath:" + mPath
                            + "\npath:" + this.path
                            + "\nr:" + r
                            + "\nhighlight:" + highlight
                            + "\nmFocusLength:" + mFocusLength
                            + "\nmRotation:" + mRotation
                            + "\nmScale:" + mScale
                            + "\nmAlpha:" + mAlpha
                            + "\nratio:" + ratio);
                    return new StaticPupilTexture.Builder(getApplicationContext())
                            .setImagePath(mPath)
                            .setBpPath(this.path)
                            .setBitmap(mExifBitmap.getBitmap())
                            //.setRotation(mRotation)
                            //.setEnableHDMI(true)
                            //.setRatio(ratio)
                            //.setR(r)
                            .setHighlight(highlight)
                            .setFocusLength(mFocusLength)
                            .setScale(mScale)
                            .setAlpha(mAlpha)
                            .build();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                dismissDialog();
                if (null != bitmap && !bitmap.isRecycled()) {
                    if (bitmap.getWidth() > 3 && bitmap.getHeight() > 3) {

                        int[] position = new int[4];
                        StaticPupilTexture.getEyePosition(position);

                        Bitmap noticeIconBmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(noticeIconBmp);
                        Paint paint = new Paint();
                        paint.setAntiAlias(true);
                        paint.setColor(Color.RED);
                        paint.setStrokeWidth(12.f);

                        canvas.drawBitmap(bitmap, 0, 0, new Paint());

                        canvas.drawPoint(position[0], position[1], paint);
                        canvas.drawPoint(position[2], position[3], paint);
                        mIsValidated = true;
                        if (path.equals(mBPAbsolutePath)) {
                            beautyBitmap = noticeIconBmp;
                            mImageView.setImageBitmap(beautyBitmap);
                            if (!mIsUpdated) {
                                mIsUpdated = true;
                                mImageView.update();
                            }
                        } else {
                            bitmap.recycle();
                        }
                    } else {
                        mIsValidated = false;
                        //如果返回的图片宽高很小，证明瞳孔检测失败
                        Toast.makeText(MainActivity.this, R.string.no_pupil, Toast.LENGTH_SHORT).
                                show();
                    }
                } else {
                    //如果返回为null，证明授权失败
                    Toast.makeText(MainActivity.this, R.string.no_permission, Toast.LENGTH_SHORT).show();
                }

            }
        }.execute(mBPAbsolutePath);
    }
}
