package mobi.acpm.inspeckage.hooks;

import android.content.ContextWrapper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.Set;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.util.FileType;
import mobi.acpm.inspeckage.util.FileUtil;

public class SharedPrefsHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_Prefs:";
    public static String putFileName = "";
    private static XSharedPreferences sPrefs;
    static StringBuffer sb = null;

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        loadPrefs();
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "getSharedPreferences", new Object[]{String.class, "int", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int modeId = ((Integer) param.args[1]).intValue();
                String mode = "MODE_PRIVATE";
                if (modeId == 1) {
                    mode = "MODE_WORLD_READABLE";
                } else if (modeId == 2) {
                    mode = "MODE_WORLD_WRITEABLE";
                } else if (modeId > 2) {
                    mode = "APPEND or MULTI_PROCESS";
                }
                SharedPrefsHook.sb = new StringBuffer();
                SharedPrefsHook.putFileName = "PUT[" + ((String) param.args[0]) + ".xml , " + mode + "]";
            }
        }});
        XposedHelpers.findAndHookConstructor("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, new Object[]{File.class, "int", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                File mFile = param.args[0];
                String text = "";
                if (mFile.exists() && mFile.canRead()) {
                    FileChannel ch = new FileInputStream(mFile).getChannel();
                    MappedByteBuffer mbb = ch.map(MapMode.READ_ONLY, 0, ch.size());
                    while (mbb.hasRemaining()) {
                        text = Charset.forName("UTF-8").decode(mbb).toString();
                    }
                }
                FileUtil.writeToFile(SharedPrefsHook.sPrefs, text, FileType.PREFS_BKP, mFile.getName());
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "commit", new Object[]{new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (SharedPrefsHook.sb.toString().length() > 0) {
                    XposedBridge.log(SharedPrefsHook.TAG + SharedPrefsHook.sb.toString().substring(0, SharedPrefsHook.sb.length() - 1) + "");
                }
                SharedPrefsHook.sb = new StringBuffer();
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "apply", new Object[]{new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (SharedPrefsHook.sb.toString().length() > 0) {
                    XposedBridge.log(SharedPrefsHook.TAG + SharedPrefsHook.sb.toString().substring(0, SharedPrefsHook.sb.length() - 1) + "");
                }
                SharedPrefsHook.sb = new StringBuffer();
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "getString", new Object[]{String.class, String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_Prefs:GET[" + ((File) XposedHelpers.getObjectField(param.thisObject, "mFile")).getName() + "] String(" + ((String) param.args[0]) + " , " + ((String) param.getResult()) + ")");
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "getStringSet", new Object[]{String.class, Set.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Set<String> set = (Set) param.getResult();
                StringBuffer sb = new StringBuffer();
                if (set != null && set.size() > 0) {
                    for (String x : set) {
                        sb.append(x + "\n");
                    }
                }
                XposedBridge.log("Inspeckage_Prefs:GET[" + ((File) XposedHelpers.getObjectField(param.thisObject, "mFile")).getName() + "] StringSet(" + ((String) param.args[0]) + ")= " + sb.toString() + ")");
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "getBoolean", new Object[]{String.class, "boolean", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                File f = (File) XposedHelpers.getObjectField(param.thisObject, "mFile");
                Module.sPrefs.reload();
                String[] strReplace = Module.sPrefs.getString(Config.SP_REPLACE_SP, "").split(",");
                if (param.args[0].equals(strReplace[0])) {
                    param.setResult(strReplace[1]);
                }
                XposedBridge.log("Inspeckage_Prefs:GET[" + f.getName() + "] Boolean(" + param.args[0] + " , " + String.valueOf(param.getResult()) + ")");
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "getFloat", new Object[]{String.class, "float", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_Prefs:GET[" + ((File) XposedHelpers.getObjectField(param.thisObject, "mFile")).getName() + "] Float(" + ((String) param.args[0]) + " , " + Float.toString(((Float) param.getResult()).floatValue()) + ")");
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "getInt", new Object[]{String.class, "int", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_Prefs:GET[" + ((File) XposedHelpers.getObjectField(param.thisObject, "mFile")).getName() + "] Int(" + ((String) param.args[0]) + " , " + Integer.toString(((Integer) param.getResult()).intValue()) + ")");
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "getLong", new Object[]{String.class, "long", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_Prefs:GET[" + ((File) XposedHelpers.getObjectField(param.thisObject, "mFile")).getName() + "] Long(" + ((String) param.args[0]) + " , " + Long.toString(((Long) param.getResult()).longValue()) + ")");
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "contains", new Object[]{String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_Prefs:CONTAINS[" + ((File) XposedHelpers.getObjectField(param.thisObject, "mFile")).getName() + "](" + ((String) param.args[0]) + " , " + Boolean.toString(((Boolean) param.getResult()).booleanValue()) + ")");
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "putString", new Object[]{String.class, String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                SharedPrefsHook.sb.append(SharedPrefsHook.putFileName + " String(" + ((String) param.args[0]) + "," + ((String) param.args[1]) + "),");
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "putBoolean", new Object[]{String.class, "boolean", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                SharedPrefsHook.sb.append(SharedPrefsHook.putFileName + " Boolean(" + ((String) param.args[0]) + "," + String.valueOf(param.args[1]) + "),");
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "putInt", new Object[]{String.class, "int", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                SharedPrefsHook.sb.append(SharedPrefsHook.putFileName + " Int(" + ((String) param.args[0]) + "," + Integer.toString(((Integer) param.args[1]).intValue()) + "),");
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "putLong", new Object[]{String.class, "long", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                SharedPrefsHook.sb.append(SharedPrefsHook.putFileName + " Long(" + ((String) param.args[0]) + "," + Long.toString(((Long) param.args[1]).longValue()) + "),");
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "putFloat", new Object[]{String.class, "float", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                SharedPrefsHook.sb.append(SharedPrefsHook.putFileName + " Float(" + ((String) param.args[0]) + "," + Float.toString(((Float) param.args[1]).floatValue()) + "),");
            }
        }});
    }
}
