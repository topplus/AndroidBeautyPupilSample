package example.com.androidbeautypupil.manager;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Properties;

import example.com.androidbeautypupil.BeautyApplication;
import example.com.androidbeautypupil.util.FileUtil;
import topplus.com.beautypupil.StaticPupilTexture;

/**
 * @author fandong
 * @date 2016/10/26.
 * @description 单例，用于管理BP文件
 */
public class BpManager extends Observable {

    private static BpManager sManager;

    private List<String> pupilPaths;


    private BpManager() {
        this.pupilPaths = new ArrayList<>();
    }

    public static BpManager getInstance() {
        if (null == sManager) {
            sManager = new BpManager();
        }
        return sManager;
    }

    //初始化bp文件
    public boolean initBpFiles() {
        String picture = FileUtil.getPathByType(FileUtil.DIR_TYPE_CACHE) + "pupil/";
        File dir = new File(picture);
        //2.解压文件
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files.length > 0) {
                for (File file : files) {
                    checkFiles(file);
                }
            }
        }
        return pupilPaths != null && !pupilPaths.isEmpty();
    }

    private void checkFiles(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files.length > 0) {
                    for (File target : files) {
                        checkFiles(target);
                    }
                }

            } else {
                String path = file.getAbsolutePath();
                if (path.endsWith("png") && !pupilPaths.contains(path)) {
                    pupilPaths.add(path);
                }
            }
        }

    }

    //开始复制
    public boolean copyAssets() {
        List<String> icons = new ArrayList<String>();
        String cacheDir = FileUtil.getPathByType(FileUtil.DIR_TYPE_CACHE);
        //1.复制模特图
        unZipAssets("default_show_img.jpg", cacheDir + "default_show_img.jpg", icons);
        //2.复制高光Stop
        unZipAssets("highlight.png", cacheDir + "highlight.png", icons);
        //3.复制文件夹
        unZipAssets("pupil", cacheDir + "pupil", icons);

        pupilPaths.clear();
        //0.排序
        if (!icons.isEmpty()) {
            int i = 0;
            for (; i < icons.size(); i++) {
                String path = icons.get(i);
                int startIndex = path.lastIndexOf("/") + 1;
                String fileName = path.substring(startIndex);
                if (fileName.startsWith("KEDE")) {
                    pupilPaths.add(0, path);
                } else {
                    pupilPaths.add(icons.get(i));
                }
            }
        }
        return this.pupilPaths != null && !this.pupilPaths.isEmpty();
    }

    public List<String> getPupilPaths() {
        return pupilPaths;
    }

    public String getBeautyPupilPath(int index) {
        if (index >= 0 && index < pupilPaths.size()) {
            String png = pupilPaths.get(index);
            if (!TextUtils.isEmpty(png)) {
                int endIndex = png.lastIndexOf(".");
                return png.substring(0, endIndex) + ".bp";
            }
        }
        return null;
    }

    private void unZipAssets(String oldPath, String newPath, List<String> icons) {
        try {
            String[] assets = BeautyApplication.gContext.getAssets().list(oldPath);
            int byteCount;
            if (assets.length > 0) {
                File is = new File(newPath);
                is.mkdirs();
                int buffer = assets.length;
                for (byteCount = 0; byteCount < buffer; ++byteCount) {
                    String fileName = assets[byteCount];
                    unZipAssets(oldPath + "/" + fileName, newPath + "/" + fileName, icons);
                }
            } else {
                //1.复制文件
                File distFile = new File(newPath);
                if (!distFile.exists() || distFile.isDirectory()) {
                    InputStream var10 = BeautyApplication.gContext.getAssets().open(oldPath);
                    FileOutputStream var11 = new FileOutputStream(distFile);
                    byte[] var12 = new byte[1024];
                    while ((byteCount = var10.read(var12)) != -1) {
                        var11.write(var12, 0, byteCount);
                    }
                    var11.flush();
                    var10.close();
                    var11.close();
                }
                //2.解压文件
                if (!TextUtils.isEmpty(newPath) && newPath.endsWith(".bp")) {
                    String prefixName = newPath.substring(0, newPath.lastIndexOf("."));
                    String pngName = prefixName + ".png";
                    String proName = prefixName + ".properties";
                    File png = new File(pngName);
                    File pro = new File(proName);

                    if (!png.exists()) {
                        png.createNewFile();
                        if (!pro.exists()) {
                            pro.createNewFile();
                        }
                        StaticPupilTexture.unZipBpFile(newPath, pngName, proName);
                    }

                    Log.e("Topplus", "解压文件：" + png.getAbsolutePath());
                    String fileName = png.getAbsolutePath();
                    if (icons != null && !icons.contains(fileName)) {
                        icons.add(fileName);
                    }
                }
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }
    }

    public String getProductName(String bpPath) {
        try {
            String prefixName = bpPath.substring(0, bpPath.lastIndexOf("."));
            String proFileName = prefixName + ".properties";

            Properties properties = new Properties();
            properties.load(new FileInputStream(proFileName));
            String name = properties.getProperty("productName");
            return new String(name.getBytes("iso-8859-1"), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
