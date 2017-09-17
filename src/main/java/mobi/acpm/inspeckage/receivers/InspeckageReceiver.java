package mobi.acpm.inspeckage.receivers;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Process;
import android.webkit.WebView;
import com.google.android.gms.drive.DriveFile;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Field;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.util.Util;

public class InspeckageReceiver extends BroadcastReceiver {
    private Activity activity = null;

    public InspeckageReceiver(Object obj) {
        if (obj instanceof Activity) {
            this.activity = (Activity) obj;
        } else if (obj instanceof Fragment) {
            this.activity = ((Fragment) obj).getActivity();
        } else {
            XposedBridge.log("Inspeckage_Error >>>> Receiver");
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (this.activity != null) {
            if (intent.getExtras().getString(Config.SP_PACKAGE).equals(this.activity.getPackageName())) {
                String action = intent.getExtras().getString("action");
                if (action.equals("finish")) {
                    this.activity.finish();
                } else if (action.equals("query")) {
                    try {
                        this.activity.managedQuery(Uri.parse(intent.getExtras().getString("uri")), null, null, null, null);
                    } catch (Exception e) {
                        XposedBridge.log("InspeckageReceiver - query - " + e.getMessage());
                    }
                } else if (action.equals("startAct")) {
                    String act = intent.getExtras().getString("activity");
                    String flags = intent.getExtras().getString("flags");
                    String intent_action = intent.getExtras().getString("intent_action");
                    String uri = intent.getExtras().getString("data_uri");
                    String category = intent.getExtras().getString("category");
                    String mimetype = intent.getExtras().getString("mimetype");
                    String extras = intent.getExtras().getString("extra");
                    Intent i = new Intent();
                    i.setClassName(this.activity.getApplicationContext(), act);
                    if (!intent_action.trim().equals("")) {
                        i.setAction(intent_action);
                    }
                    if (flags.trim().equals("")) {
                        i.addFlags(DriveFile.MODE_READ_ONLY);
                    } else {
                        for (Field f : Intent.class.getFields()) {
                            try {
                                Object value = f.get(i);
                                if (flags.trim().contains(f.getName())) {
                                    i.addFlags(((Integer) value).intValue());
                                }
                            } catch (IllegalAccessException e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
                    if (!uri.trim().equals("")) {
                        i.setData(Uri.parse(uri));
                    }
                    if (!category.trim().equals("")) {
                        i.addCategory(category);
                    }
                    if (!mimetype.trim().equals("")) {
                        Intent.normalizeMimeType(mimetype);
                    }
                    if (!extras.trim().equals("")) {
                        String[] extra = new String[]{extras};
                        if (extras.contains(";")) {
                            extra = extras.split(";");
                        }
                        for (String e3 : extra) {
                            String[] values = e3.split(",");
                            if (values.length == 3) {
                                if (values[0].trim().toLowerCase().equals("string")) {
                                    i.putExtra(values[1], values[2]);
                                }
                                if (values[0].trim().toLowerCase().equals("boolean")) {
                                    i.putExtra(values[1], Boolean.valueOf(values[2]));
                                }
                                if (values[0].trim().toLowerCase().equals("int")) {
                                    i.putExtra(values[1], Integer.valueOf(values[2]));
                                }
                                if (values[0].trim().toLowerCase().equals("float")) {
                                    i.putExtra(values[1], Float.valueOf(values[2]));
                                }
                                if (values[0].trim().toLowerCase().equals("double")) {
                                    i.putExtra(values[1], Double.valueOf(values[2]));
                                }
                            }
                        }
                    }
                    this.activity.startActivity(i);
                } else if (action.equals("fileTree")) {
                    String tree = Util.FileTree(this.activity.getApplicationInfo().dataDir, "");
                    r0 = new Intent("mobi.acpm.inspeckage.INSPECKAGE_WEB");
                    r0.putExtra("action", "fileTree");
                    float m = ((float) tree.length()) / 3.0f;
                    String sub1 = tree.substring(0, (int) m);
                    String sub2 = tree.substring((int) m, tree.length());
                    r0.putExtra(Config.SP_DATA_DIR_TREE, tree);
                    this.activity.sendBroadcast(r0, null);
                    Util.sb = new StringBuilder();
                } else if (action.equals("checkApp")) {
                    r0 = new Intent("mobi.acpm.inspeckage.INSPECKAGE_WEB");
                    r0.putExtra("action", "checkApp");
                    r0.putExtra(Config.SP_APP_IS_RUNNING, true);
                    r0.putExtra("PID", Process.myPid());
                    this.activity.sendBroadcast(r0, null);
                } else if (action.equals("webviewSetDebug")) {
                    if (VERSION.SDK_INT >= 19) {
                        WebView.setWebContentsDebuggingEnabled(true);
                    }
                } else if (action.equals("clipboard")) {
                    ((ClipboardManager) context.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("simple text", intent.getExtras().getString("value")));
                }
            }
        }
    }
}
