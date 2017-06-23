package conger.com.pandamusic.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by selfimpr on 2017/6/5. 正在施工。。。。
 */

public class DownloadFragment extends BaseFragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView mTextView = new TextView(container.getContext());
        mTextView.setText("正在施工中。。。");
        return mTextView;
    }

    @Override
    protected void init() {

    }

    @Override
    protected void setListener() {

    }
}
