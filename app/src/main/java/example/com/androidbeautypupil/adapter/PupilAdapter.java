package example.com.androidbeautypupil.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import example.com.androidbeautypupil.R;
import example.com.androidbeautypupil.manager.BpManager;
import example.com.androidbeautypupil.util.BitmapUtil;

/**
 * @author fandong
 * @date 2016/10/26.
 * @description
 */
public class PupilAdapter extends RecyclerView.Adapter<PupilAdapter.ViewHolder> {
    private List<String> paths;

    private OnItemClickListener mOnItemClickListener;

    private LayoutInflater mInflater;

    private Context context;

    private int selected = 0;

    public PupilAdapter(Context context) {
        this.paths = new ArrayList<>();
        this.context = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setPath(BpManager.getInstance().getPupilPaths());
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public void setPath(List<String> paths) {
        if (paths != null && !paths.isEmpty()) {
            this.paths.clear();
            this.paths.addAll(paths);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public PupilAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.vw_pupil_icon_item, parent, false));
    }

    @Override
    public void onBindViewHolder(PupilAdapter.ViewHolder holder, final int position) {
        if (position == selected) {
            holder.itemView.setBackgroundResource(R.drawable.vw_pupil_icon_press);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.vw_pupil_icon_bg);
        }
        //宽高
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        int size = context.getResources().getDisplayMetrics().widthPixels / 4;
        params.height = size;
        params.width = size;
        holder.itemView.setLayoutParams(params);
        BitmapUtil.loadImage(holder.iv, "file://" + paths.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    public String getBpPath(int position) {
        String png = paths.get(position);
        return png.substring(0, png.lastIndexOf(".")) + ".bp";
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;

        public ViewHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView.findViewById(R.id.item_pupil_icon);
        }
    }
}
