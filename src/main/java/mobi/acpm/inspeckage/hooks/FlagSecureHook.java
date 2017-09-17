package mobi.acpm.inspeckage.hooks;

import android.view.SurfaceView;
import android.view.Window;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.util.Config;

public class FlagSecureHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_FlagSecure:";
    private static XSharedPreferences sPrefs;

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(Window.class, "setFlags", new Object[]{"int", "int", new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                FlagSecureHook.loadPrefs();
                if (FlagSecureHook.sPrefs.getBoolean(Config.SP_FLAG_SECURE, false) && ((Integer) param.args[0]).intValue() == 8192) {
                    param.args[0] = Integer.valueOf(0);
                    param.args[1] = Integer.valueOf(0);
                }
            }
        }});
        XposedHelpers.findAndHookMethod(SurfaceView.class, "setSecure", new Object[]{Boolean.TYPE, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                FlagSecureHook.loadPrefs();
                if (FlagSecureHook.sPrefs.getBoolean(Config.SP_FLAG_SECURE, false)) {
                    param.args[0] = Boolean.valueOf(false);
                }
            }
        }});
    }
}
