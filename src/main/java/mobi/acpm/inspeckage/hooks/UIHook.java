package mobi.acpm.inspeckage.hooks;

import android.app.Activity;
import android.app.Fragment;
import android.content.IntentFilter;
import android.os.Bundle;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import mobi.acpm.inspeckage.receivers.InspeckageReceiver;

public class UIHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_GUI:";

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", new Object[]{Bundle.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.thisObject.getApplicationContext().registerReceiver(new InspeckageReceiver(param.thisObject), new IntentFilter("mobi.acpm.inspeckage.INSPECKAGE_FILTER"));
            }
        }});
        XposedHelpers.findAndHookMethod(Fragment.class, "onCreate", new Object[]{Bundle.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.thisObject.getActivity().getApplicationContext().registerReceiver(new InspeckageReceiver(param.thisObject), new IntentFilter("mobi.acpm.inspeckage.INSPECKAGE_FILTER"));
            }
        }});
    }
}
