package mobi.acpm.inspeckage.hooks;

import android.content.ContextWrapper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.io.File;
import java.net.URI;

public class FileSystemHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_FileSystem:";

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "openFileOutput", new Object[]{String.class, "int", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String name = param.args[0];
                int mode = ((Integer) param.args[1]).intValue();
                if (name.contains("Inspeckage")) {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                    return;
                }
                String m;
                switch (mode) {
                    case 0:
                        m = "MODE_PRIVATE";
                        break;
                    case 1:
                        m = "MODE_WORLD_READABLE";
                        break;
                    case 2:
                        m = "MODE_WORLD_WRITEABLE";
                        break;
                    case 32768:
                        m = "MODE_APPEND";
                        break;
                    default:
                        m = "?";
                        break;
                }
                XposedBridge.log("Inspeckage_FileSystem:openFileOutput(" + name + ", " + m + ")");
            }
        }});
        XposedHelpers.findAndHookConstructor(File.class, new Object[]{String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String str = param.args[0];
                if (str.contains("Inspeckage")) {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                } else {
                    XposedBridge.log("Inspeckage_FileSystem:R/W [new File(String)]: " + str);
                }
            }
        }});
        XposedHelpers.findAndHookConstructor(File.class, new Object[]{String.class, String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String dir = param.args[0];
                String fileName = param.args[1];
                if (dir.contains("Inspeckage") || fileName.contains("Inspeckage")) {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                } else {
                    XposedBridge.log("Inspeckage_FileSystem:R/W Dir: " + dir + " File: " + fileName);
                }
            }
        }});
        XposedHelpers.findAndHookConstructor(File.class, new Object[]{File.class, String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                File fileDir = param.args[0];
                String fileName = param.args[1];
                if (fileDir.getAbsolutePath().contains("Inspeckage") || fileName.contains("Inspeckage")) {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                } else {
                    XposedBridge.log("Inspeckage_FileSystem:R/W Dir: " + fileDir.getAbsolutePath() + " File: " + fileName);
                }
            }
        }});
        XposedHelpers.findAndHookConstructor(File.class, new Object[]{URI.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                URI uri = param.args[0];
                if (uri.toString().contains("Inspeckage")) {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                } else {
                    XposedBridge.log("Inspeckage_FileSystem:R/W [new File(URI)]: " + uri.toString());
                }
            }
        }});
    }
}
