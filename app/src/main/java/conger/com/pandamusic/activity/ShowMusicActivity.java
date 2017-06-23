package conger.com.pandamusic.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.support.v7.widget.Toolbar;


import conger.com.pandamusic.R;
import conger.com.pandamusic.fragment.LocalMusicFragment;
import conger.com.pandamusic.fragment.SongListFragment;
import conger.com.pandamusic.fragment.localMusicSubFragment.AlbumDetailFragment;
import conger.com.pandamusic.fragment.localMusicSubFragment.ArtistDetailFragment;
import conger.com.pandamusic.fragment.localMusicSubFragment.MusicFragment;
import conger.com.pandamusic.utils.binding.Bind;



/**
 * Created by selfimpr on 2017/5/18.
 */

public class ShowMusicActivity extends BaseActivity {


    @Bind(R.id.container)
    FrameLayout mFrameLayout;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;


    public static final int LOCAL_FRAGMENT = 0;
    public static final int ONLINE_FRAGMENT = 1;
    public static final String WHICH_FRAGMETN = "SHOW";
    int show;
    //本地音乐点击的Fragment,包含三个字Fragment.
    private MusicFragment mMusicFragment;
    private LocalMusicFragment mLocalMusicFragment;
    private SongListFragment mOnLineFragment;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmusic);

        mLocalMusicFragment = new LocalMusicFragment();
        mMusicFragment = new MusicFragment();
        mOnLineFragment = new SongListFragment();


        show = getIntent().getIntExtra(WHICH_FRAGMETN, LOCAL_FRAGMENT);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof ArtistDetailFragment) {

                    getSupportFragmentManager().popBackStack();

                } else if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof AlbumDetailFragment) {

                    getSupportFragmentManager().popBackStack();

                } else {

                    finish();

                }


            }
        });

        showFragment();


    }

    private void showFragment() {

        switch (show) {

            case LOCAL_FRAGMENT:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, mMusicFragment).commit();
                break;
            case ONLINE_FRAGMENT:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, mOnLineFragment).commit();

                break;

        }

    }
}
