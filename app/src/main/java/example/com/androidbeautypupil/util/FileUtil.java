package example.com.androidbeautypupil.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

import example.com.androidbeautypupil.BeautyApplication;
import example.com.androidbeautypupil.BuildConfig;


/**
 * time: 15/6/7
 * description: 文件管理类
 *
 * @author fandong
 */
public class FileUtil {
    public static final int DIR_TYPE_HOME = 0x01;
    public static final int DIR_TYPE_CACHE = 0x02;

    private static String DIR_HOME = "/home";
    private static String DIR_CACHE = "/cache";

    /**
     * 通过类型获取目录路径
     *
     * @param type
     * @return
     */
    public static String getPathByType(int type) {
        String dir = "/";

        String filePath;

        switch (type) {
            case DIR_TYPE_HOME:
                filePath = DIR_HOME;
                break;

            case DIR_TYPE_CACHE:
                filePath = DIR_CACHE;
                break;

            default:
                filePath = "";
                break;
        }
        File file = null;
        if (BuildConfig.DEBUG) {
            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/topplus/" + BuildConfig.APPLICATION_ID + filePath);
        } else {
            file = new File(BeautyApplication.gContext.getFilesDir().getAbsolutePath() + filePath);
        }
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }

        if (file.exists()) {
            if (file.isDirectory()) {
                dir = file.getPath();
            }
        } else {
            // 文件没创建成功，可能是sd卡不存在，但是还是把路径返回
            dir = filePath;
        }

        return dir + "/";
    }

    public static String getExternalImageDirectory() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/topplus/" + BuildConfig.APPLICATION_ID);

        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file.getAbsolutePath() + "/image";

    }

    public static String getExternalTargetImageDirectory() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/topplus/" + BuildConfig.APPLICATION_ID + "/target");

        try {
            if (!file.exists() || !file.isDirectory()) {
                file.mkdirs();
            }
            File nomedia = new File(file, ".nomedia");
            if (!file.exists()) {
                nomedia.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();

    }

}
