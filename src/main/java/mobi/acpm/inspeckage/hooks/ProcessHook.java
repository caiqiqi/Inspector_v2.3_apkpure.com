package mobi.acpm.inspeckage.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ProcessHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_Process:";

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod("android.os.Process", loadPackageParam.classLoader, "start", new Object[]{String.class, String.class, Integer.TYPE, Integer.TYPE, int[].class, Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class, String.class, String.class, String.class, String[].class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (((Integer) param.args[2]).intValue() == 10066) {
                        param.args[5] = Integer.valueOf(((Integer) param.args[5]).intValue() | 1);
                        XposedBridge.log("Inspeckage_Process:debugFlags: " + String.valueOf(param.args[5]));
                    }
                }
            }});
        } catch (Error e) {
            XposedBridge.log("ERROR_PROCESS: " + e.getMessage());
        }
    }
}
