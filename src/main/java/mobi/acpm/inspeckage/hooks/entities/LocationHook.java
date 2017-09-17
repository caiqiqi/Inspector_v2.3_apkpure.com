package mobi.acpm.inspeckage.hooks.entities;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.util.Config;

public class LocationHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_Location: ";
    private static XSharedPreferences sPrefs;

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        try {
            Class<?> location = XposedHelpers.findClass("android.location.Location", loadPackageParam.classLoader);
            XposedHelpers.findAndHookMethod(location, "getLatitude", new Object[]{new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LocationHook.loadPrefs();
                    String geolocation = LocationHook.sPrefs.getString(Config.SP_GEOLOCATION, "");
                    if (!geolocation.equals("") && geolocation.contains(",")) {
                        param.setResult(Double.valueOf(geolocation.split(",")[0]));
                    }
                }
            }});
            XposedHelpers.findAndHookMethod(location, "getLongitude", new Object[]{new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    LocationHook.loadPrefs();
                    String geolocation = LocationHook.sPrefs.getString(Config.SP_GEOLOCATION, "");
                    if (!geolocation.equals("") && geolocation.contains(",")) {
                        param.setResult(Double.valueOf(geolocation.split(",")[1]));
                    }
                }
            }});
        } catch (ClassNotFoundError e) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
