package mobi.acpm.inspeckage.hooks;

import android.content.ClipData;
import android.content.ClipboardManager;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ClipboardHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_Clipboard:";

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(ClipboardManager.class, "setPrimaryClip", new Object[]{ClipData.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                ClipData cd = param.args[0];
                StringBuilder sb = new StringBuilder();
                if (cd != null && cd.getItemCount() > 0) {
                    for (int i = 0; i < cd.getItemCount(); i++) {
                        sb.append(cd.getItemAt(i).getText());
                    }
                }
                XposedBridge.log("Inspeckage_Misc:Copied to the clipboard: " + sb.toString() + "");
            }
        }});
    }
}
