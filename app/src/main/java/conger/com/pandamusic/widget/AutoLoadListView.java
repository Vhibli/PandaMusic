package conger.com.pandamusic.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import conger.com.pandamusic.R;


/**
 * 自动加载更多ListView
 *  历史遗留问题。。。
 */
public class AutoLoadListView extends ListView implements AbsListView.OnScrollListener {
    private static final String TAG = AutoLoadListView.class.getSimpleName();
    private View vFooter;
    private OnLoadListener mListener;
    private int mFirstVisibleItem = 0;
    private boolean mEnableLoad = true;
    private boolean mIsLoading = false;

    public AutoLoadListView(Context context) {
        super(context);
        init();
    }

    public AutoLoadListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoLoadListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        vFooter = LayoutInflater.from(getContext()).inflate(R.layout.auto_load_list_view_footer, null);
        addFooterView(vFooter, null, false);
        setOnScrollListener(this);
        onLoadComplete();
    }

    public void setOnLoadListener(OnLoadListener listener) {
        mListener = listener;
    }

    public void onLoadComplete() {
        Log.d(TAG, "onLoadComplete");
        mIsLoading = false;
        removeFooterView(vFooter);
    }

    public void setEnable(boolean enable) {
        mEnableLoad = enable;
    }

    /**
     *  监听滑动事件。当最后一项可见的position大于总数说明到底了
     * @param view
     * @param firstVisibleItem
     * @param visibleItemCount
     * @param totalItemCount
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean isPullDown = firstVisibleItem > mFirstVisibleItem;
        if (mEnableLoad && !mIsLoading && isPullDown) {
            int lastVisibleItem = firstVisibleItem + visibleItemCount;
            if (lastVisibleItem >= totalItemCount - 1) {
                onLoad();
            }
        }
        mFirstVisibleItem = firstVisibleItem;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    private void onLoad() {
     ;
        mIsLoading = true;
        addFooterView(vFooter, null, false);
        if (mListener != null) {
            mListener.onLoad();
        }
    }

    public interface OnLoadListener {
        void onLoad();
    }
}
