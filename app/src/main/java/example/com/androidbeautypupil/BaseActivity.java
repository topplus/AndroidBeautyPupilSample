package example.com.androidbeautypupil;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * time: 2016/11/17
 * description:
 *
 * @author fandong
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        BeautyApplication.addPage(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BeautyApplication.removePage(this);
    }
}
