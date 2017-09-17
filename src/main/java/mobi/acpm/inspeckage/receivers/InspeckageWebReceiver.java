package mobi.acpm.inspeckage.receivers;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.util.Config;

public class InspeckageWebReceiver extends BroadcastReceiver {
    private Context mContext;

    public InspeckageWebReceiver(Context ctx) {
        this.mContext = ctx;
    }

    public void onReceive(Context context, Intent intent) {
        Context context2 = this.mContext;
        String str = Module.PREFS;
        Context context3 = this.mContext;
        Editor edit = context2.getSharedPreferences(str, 1).edit();
        String action = intent.getExtras().getString("action");
        if (action.equals("fileTree")) {
            edit.putString(Config.SP_DATA_DIR_TREE, "<script>\n$(document).ready(function() {\n\n    CollapsibleLists.apply();\n\n});\n</script>" + "<ul class=\"collapsibleList\">" + intent.getExtras().getString(Config.SP_DATA_DIR_TREE) + "</ul>");
            edit.apply();
        } else if (action.equals("checkApp")) {
            boolean isRunning = intent.getExtras().getBoolean(Config.SP_APP_IS_RUNNING);
            int pid = intent.getExtras().getInt("PID");
            edit.putBoolean(Config.SP_APP_IS_RUNNING, isRunning);
            edit.putInt(Config.SP_APP_PID, pid);
            edit.apply();
        } else if (action.equals("clipboard")) {
            ((ClipboardManager) context.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("simple text", intent.getExtras().getString("value")));
        }
    }
}
