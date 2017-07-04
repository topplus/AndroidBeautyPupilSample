package example.com.androidbeautypupil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import example.com.androidbeautypupil.adapter.ListAdapter;
import example.com.androidbeautypupil.bean.event.ScrollEvent;
import example.com.androidbeautypupil.manager.EventBusManager;

/**
 * @author fandong
 * @date 2016/9/30
 * @description 美瞳列表
 */

public class ListActivity extends BaseActivity {

    private ListView mListView;

    private ListAdapter mAdapter;

    private String path;


    private Handler mHandler;
    private boolean mIsResumed;
    private AbsListView.RecyclerListener mRecyclerListener = new AbsListView.RecyclerListener() {
        @Override
        public void onMovedToScrapHeap(View view) {
            View v = view.findViewById(R.id.iv);
            if (v instanceof BeautyImageView) {
                ((BeautyImageView) v).setImageBitmap(mAdapter.getOriginBitmap());
            }
        }
    };

    {
        this.mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (1 == msg.what) {
                    mAdapter.loadBeauty();
                }
            }
        };
    }

    public static void launch(Context context, String path, int rotation, float alpha, float scale, float focus) {
        Intent intent = new Intent(context, ListActivity.class);
        intent.putExtra("path", path);
        intent.putExtra("alpha", alpha);
        intent.putExtra("rotation", rotation);
        intent.putExtra("scale", scale);
        intent.putExtra("focus", focus);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Intent intent = getIntent();
        this.path = intent.getStringExtra("path");

        mListView = (ListView) findViewById(R.id.lv);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                boolean isIdle = AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState;
                if (isIdle) {
                    mHandler.sendEmptyMessageDelayed(1, 100);
                } else {
                    mHandler.removeMessages(1);
                    mAdapter.setLoad(true);
                }
                ScrollEvent event = new ScrollEvent();
                event.setScroll(!isIdle);
                EventBusManager.post(event);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.launch(ListActivity.this, true, path, mAdapter.getBeautyPath(position));
            }
        });
        mListView.setRecyclerListener(mRecyclerListener);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!mIsResumed) {
            mIsResumed = true;
            //初始化adapter
            //原始的bitmap
            Intent intent = getIntent();
            mAdapter = new ListAdapter(this, path, intent.getIntExtra("rotation", 0), intent.getFloatExtra("alpha", .5f)
                    , intent.getFloatExtra("scale", 1.f), intent.getFloatExtra("focus", 22.f));
            mListView.setAdapter(mAdapter);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdapter.loadBeauty();
                }
            }, 100);
        }
    }

    @Override
    protected void onDestroy() {
        int count = mListView.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mListView.getChildAt(i);
            BeautyImageView iv = (BeautyImageView) view.findViewById(R.id.iv);
            iv.destroy();
        }
        //销毁iv
        mAdapter.onDestroy();
        super.onDestroy();
    }
}
