package example.com.androidbeautypupil.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import example.com.androidbeautypupil.BeautyImageView;
import example.com.androidbeautypupil.MainActivity;
import example.com.androidbeautypupil.R;
import example.com.androidbeautypupil.manager.BitmapManager;
import example.com.androidbeautypupil.manager.BpManager;
import topplus.com.beautypupil.StaticPupilTexture;

/**
 * @author fandong
 * @date 2016/9/30
 * @description
 */

public class ListAdapter extends BaseAdapter {

    private Context mContext;
    //bp文件的路径
    private List<String> paths;

    private LayoutInflater mLayoutInflater;

    private List<BeautyImageView> ivs;

    private boolean isLoad;

    private Bitmap originBitmap;
    private float focusLength;
    private String mPath;

    private float alpha;
    private float scale;
    private int rotate;


    public ListAdapter(Context context, String path, int rotate, float alpha, float scale, float focus) {
        this.mContext = context;
        this.mPath = path;
        this.rotate = rotate;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //准备数据
        this.paths = new ArrayList<>();
        this.ivs = new ArrayList<>();
        this.alpha = alpha;
        this.scale = scale;
        this.focusLength = focus;
        this.init(path);
    }

    private void init(String picPath) {
        scanBpFile();
        //加载
        this.originBitmap = StaticPupilTexture.getDefaultBitmap(mPath, BitmapManager.getInstance().getExifBitmap(mPath, -1, -1).getBitmap(), rotate, 2.f, 1.f, 22.f);
//        this.originBitmap = StaticPupilTexture.getDefaultBitmap(B)
    }

    //遍历bp文件
    private void scanBpFile() {
        List<String> list = BpManager.getInstance().getPupilPaths();
        for (String p : list) {
            paths.add(p.substring(0, p.lastIndexOf(".")) + ".bp");
        }
    }

    public String getBeautyPath(int position) {
        return paths.get(position);
    }

    public void setLoad(boolean load) {
        this.isLoad = load;
    }

    @Override
    public int getCount() {
        return paths.size();
    }

    @Override
    public Object getItem(int position) {
        return paths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.vw_list_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.iv = (BeautyImageView) convertView.findViewById(R.id.iv);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.iv.getLayoutParams();
            params.width = mContext.getResources().getDisplayMetrics().widthPixels;
            params.height = params.width;
            viewHolder.iv.setLayoutParams(params);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            if (!ivs.contains(viewHolder.iv)) {
                ivs.add(viewHolder.iv);
            }
            convertView.setTag(viewHolder);
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.name.setText(getFileName(paths.get(position)));
        viewHolder.iv.setImageBitmap(this.originBitmap);
        viewHolder.iv.setPath(paths.get(position));
        viewHolder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.launch(mContext, true, mPath, getBeautyPath(position));
            }
        });
        return convertView;
    }

    public void loadBeauty() {
        for (BeautyImageView iv : ivs) {
            iv.applyEffect(this.mPath
                    , BitmapManager.getInstance().getExifBitmap(mPath, -1, -1).getBitmap(), rotate, focusLength, alpha, scale);
        }
    }


    //销毁使用过的iv
    public void onDestroy() {
        for (int i = 0; i < ivs.size(); i++) {
            ivs.get(i).destroy();
        }
    }


    private String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public Bitmap getOriginBitmap() {
        return originBitmap;
    }

    public static class ViewHolder {
        BeautyImageView iv;
        TextView name;
    }
}
