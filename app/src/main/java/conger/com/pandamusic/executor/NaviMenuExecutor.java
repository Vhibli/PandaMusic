package conger.com.pandamusic.executor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import conger.com.pandamusic.R;
import conger.com.pandamusic.activity.MusicActivity;
import conger.com.pandamusic.activity.SettingActivity;
import conger.com.pandamusic.application.AppCache;
import conger.com.pandamusic.service.PlayService;
import conger.com.pandamusic.utils.Preferences;
import conger.com.pandamusic.utils.ToastUtils;



/**
 * 导航菜单执行器
 *
 */
public class NaviMenuExecutor {

    public static boolean onNavigationItemSelected(MenuItem item, Context context) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                startActivity(context, SettingActivity.class);
                return true;
            case R.id.action_night:
                nightMode(context);
                break;
            case R.id.action_timer:
                timerDialog(context);
                return true;
            case R.id.action_exit:
                exit(context);
                return true;

        }
        return false;
    }

    private static void startActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }

    private static void nightMode(Context context) {
        if (!(context instanceof MusicActivity)) {
            return;
        }
        final MusicActivity activity = (MusicActivity) context;
        final boolean on = !Preferences.isNightMode();
        final ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setCancelable(false);
        dialog.show();
        AppCache.updateNightMode(on);
        Handler handler = new Handler(activity.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.cancel();
                activity.recreate();
                Preferences.saveNightMode(on);
            }
        }, 500);
    }

    private static void timerDialog(final Context context) {
        if (!(context instanceof MusicActivity)) {
            return;
        }
        new AlertDialog.Builder(context)
                .setTitle(R.string.menu_timer)
                .setItems(context.getResources().getStringArray(R.array.timer_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int[] times = context.getResources().getIntArray(R.array.timer_int);
                        startTimer(context, times[which]);
                    }
                })
                .show();
    }

    private static void startTimer(Context context, int minute) {
        if (!(context instanceof MusicActivity)) {
            return;
        }

        MusicActivity activity = (MusicActivity) context;
        PlayService service = activity.getPlayService();
        service.startQuitTimer(minute * 60 * 1000);
        if (minute > 0) {
            ToastUtils.show(context.getString(R.string.timer_set, String.valueOf(minute)));
        } else {
            ToastUtils.show(R.string.timer_cancel);
        }
    }

    private static void exit(Context context) {
        if (!(context instanceof MusicActivity)) {
            return;
        }

        MusicActivity activity = (MusicActivity) context;
        PlayService service = activity.getPlayService();
        activity.finish();
        service.stop();
    }
}
