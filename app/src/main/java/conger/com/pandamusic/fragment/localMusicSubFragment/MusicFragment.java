package conger.com.pandamusic.fragment.localMusicSubFragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import conger.com.pandamusic.R;
import conger.com.pandamusic.fragment.BaseFragment;
import conger.com.pandamusic.fragment.LocalMusicFragment;
import conger.com.pandamusic.utils.binding.Bind;



/**
 * Created by selfimpr on 2017/5/22.
 */

public class MusicFragment extends BaseFragment {

    @Bind(R.id.titletab)
    private TabLayout mTabLayout;
    @Bind(R.id.subViewPager)
    private ViewPager mViewPager;

    private LocalMusicFragment mLocalMusicFragment;
    private AlbumFragment mAlbumFragment;
    private ArtistFragment mArtistFragment;

    public List<BaseFragment> mFragmentList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmet_music,container,false);
        return view;
    }

    @Override
    protected void init() {


        mLocalMusicFragment = new LocalMusicFragment();
        mAlbumFragment = new AlbumFragment();
        mArtistFragment = new ArtistFragment();

        mFragmentList.add(mLocalMusicFragment);
        mFragmentList.add(mArtistFragment);
        mFragmentList.add(mAlbumFragment);

        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new FragmentAdapter(getChildFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);






    }

    @Override
    protected void setListener() {

    }

    public  class FragmentAdapter extends FragmentPagerAdapter{


        private  String[] titles = new String[]{

                "单曲",
                "歌手",
                "专辑"

        };

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        public FragmentAdapter(FragmentManager fm) {
            super(fm);

        }




        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    }

}
