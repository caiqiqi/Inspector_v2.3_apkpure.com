package mobi.acpm.inspeckage.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import mobi.acpm.inspeckage.util.Util;

public class HashHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_Hash:";
    private static StringBuffer sb;

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(MessageDigest.class, "getInstance", new Object[]{String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                HashHook.sb = new StringBuffer();
                HashHook.sb.append("Algorithm(" + param.args[0] + ") [");
            }
        }});
        XposedHelpers.findAndHookMethod(MessageDigest.class, "update", new Object[]{byte[].class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                HashHook.sb.append("" + Util.byteArrayToString((byte[]) param.args[0]) + " : ");
            }
        }});
        XposedHelpers.findAndHookMethod(MessageDigest.class, "update", new Object[]{byte[].class, "int", "int", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                HashHook.sb.append("" + Util.byteArrayToString((byte[]) param.args[0]) + " : ");
            }
        }});
        XposedHelpers.findAndHookMethod(MessageDigest.class, "update", new Object[]{ByteBuffer.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                HashHook.sb.append("" + Util.byteArrayToString(param.args[0].array()) + " : ");
            }
        }});
        XposedHelpers.findAndHookMethod(MessageDigest.class, "digest", new Object[]{new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                HashHook.sb.append(Util.toHexString((byte[]) param.getResult()) + "]");
                XposedBridge.log(HashHook.TAG + HashHook.sb.toString());
                HashHook.sb = new StringBuffer();
            }
        }});
        XposedHelpers.findAndHookMethod(MessageDigest.class, "digest", new Object[]{byte[].class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            }
        }});
        XposedHelpers.findAndHookMethod(MessageDigest.class, "digest", new Object[]{byte[].class, "int", "int", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            }
        }});
    }
}
