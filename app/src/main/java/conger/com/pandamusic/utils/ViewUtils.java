package conger.com.pandamusic.utils;

import android.view.View;


import conger.com.pandamusic.enums.LoadStateEnum;

import static conger.com.pandamusic.enums.LoadStateEnum.LOADING;


public class ViewUtils {

    /**
     *  根据网络拉取数据的情况决定显示哪个界面 ，失败界面，加载中，加载成功三种情况
     * @param loadSuccess
     * @param loading
     * @param loadFail
     * @param state
     */
    public static void changeViewState(View loadSuccess, View loading, View loadFail, LoadStateEnum state) {
        switch (state) {
            case LOADING:
                loadSuccess.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                loadFail.setVisibility(View.GONE);
                break;
            case LOAD_SUCCESS:
                loadSuccess.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
                loadFail.setVisibility(View.GONE);
                break;
            case LOAD_FAIL:
                loadSuccess.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                loadFail.setVisibility(View.VISIBLE);
                break;
        }
    }
}
