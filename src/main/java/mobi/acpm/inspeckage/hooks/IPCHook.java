package mobi.acpm.inspeckage.hooks;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import mobi.acpm.inspeckage.BuildConfig;

public class IPCHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_IPC:";

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "startActivities", new Object[]{Intent[].class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Intent[] it = (Intent[]) param.args[0];
                StringBuffer sb = new StringBuffer();
                for (Intent i : it) {
                    sb.append(i + ",");
                }
                XposedBridge.log("Inspeckage_IPC:startActivities: " + sb.toString().substring(0, sb.length() - 1));
            }
        }});
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "startService", new Object[]{Intent.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_IPC:startService: " + param.args[0]);
            }
        }});
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "startActivity", new Object[]{Intent.class, Bundle.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_IPC:startActivity: " + param.args[0]);
            }
        }});
        XposedHelpers.findAndHookMethod(Activity.class, "startActivity", new Object[]{Intent.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_IPC:startActivity: " + param.args[0]);
            }
        }});
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "sendBroadcast", new Object[]{Intent.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Intent intent = param.args[0];
                if (intent != null && !intent.getAction().contains(BuildConfig.APPLICATION_ID)) {
                    XposedBridge.log("Inspeckage_IPC:sendBroadcast: " + intent);
                }
            }
        }});
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "sendBroadcast", new Object[]{Intent.class, String.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Intent intent = param.args[0];
                if (intent != null && !intent.getAction().contains(BuildConfig.APPLICATION_ID)) {
                    XposedBridge.log("Inspeckage_IPC:sendBroadcast: " + intent);
                }
            }
        }});
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "registerReceiver", new Object[]{BroadcastReceiver.class, IntentFilter.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                IntentFilter intentFilter = param.args[1];
                StringBuffer sb = new StringBuffer();
                sb.append("Actions: ");
                for (int i = 0; i < intentFilter.countActions(); i++) {
                    sb.append(intentFilter.getAction(i) + ",");
                }
                if (!sb.toString().contains(BuildConfig.APPLICATION_ID)) {
                    XposedBridge.log("Inspeckage_IPC:registerReceiver: " + sb.toString().substring(0, sb.length() - 1));
                }
            }
        }});
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "registerReceiver", new Object[]{BroadcastReceiver.class, IntentFilter.class, String.class, Handler.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                IntentFilter intentFilter = param.args[1];
                StringBuffer sb = new StringBuffer();
                sb.append("Actions: ");
                for (int i = 0; i < intentFilter.countActions(); i++) {
                    sb.append(intentFilter.getAction(i) + ",");
                }
                if (param.args[2] != null) {
                    sb.append(" Permissions: " + param.args[2]);
                }
                if (!sb.toString().contains(BuildConfig.APPLICATION_ID)) {
                    XposedBridge.log("Inspeckage_IPC:registerReceiver: " + sb.toString().substring(0, sb.length() - 1));
                }
            }
        }});
    }
}
