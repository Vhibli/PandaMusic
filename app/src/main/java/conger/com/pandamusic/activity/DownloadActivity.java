package conger.com.pandamusic.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import conger.com.pandamusic.R;
import conger.com.pandamusic.fragment.DownloadFragment;

/**
 * Created by selfimpr on 2017/6/5.
 *  正在施工。。。。
 */

public class DownloadActivity extends BaseActivity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_download);

        DownloadFragment downloadFragment = new DownloadFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container,downloadFragment).commit();

    }
}
