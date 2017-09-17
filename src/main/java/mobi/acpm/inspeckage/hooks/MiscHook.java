package mobi.acpm.inspeckage.hooks;

import android.net.Uri;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MiscHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_Misc:";

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(Uri.class, "parse", new Object[]{String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_Misc:URI: " + param.args[0] + "");
            }
        }});
        try {
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.google.android.gms.ads.identifier.AdvertisingIdClient$Info", loadPackageParam.classLoader), "getId", new Object[]{new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (param != null) {
                        XposedBridge.log("Inspeckage_Misc:AdvertisingID: " + param.args[0] + "");
                    }
                }

                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param != null) {
                        XposedBridge.log("Inspeckage_Misc:AdvertisingID before: " + param.args[0] + "");
                    }
                }
            }});
        } catch (ClassNotFoundError e) {
        }
    }
}
