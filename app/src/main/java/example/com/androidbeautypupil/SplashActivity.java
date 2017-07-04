package example.com.androidbeautypupil;

import android.os.Bundle;
import android.os.Handler;

import topplus.com.commonutils.Library;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Library.init(getApplicationContext(), "", "", false);
    }

    @Override
    protected void onResume() {
        //测试
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.launch(SplashActivity.this);
                finish();
            }
        }, 1200);
    }
}