package mobi.acpm.inspeckage;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import mobi.acpm.inspeckage.hooks.ClipboardHook;
import mobi.acpm.inspeckage.hooks.CryptoHook;
import mobi.acpm.inspeckage.hooks.FileSystemHook;
import mobi.acpm.inspeckage.hooks.FingerprintHook;
import mobi.acpm.inspeckage.hooks.FlagSecureHook;
import mobi.acpm.inspeckage.hooks.HashHook;
import mobi.acpm.inspeckage.hooks.HttpHook;
import mobi.acpm.inspeckage.hooks.IPCHook;
import mobi.acpm.inspeckage.hooks.MiscHook;
import mobi.acpm.inspeckage.hooks.ProxyHook;
import mobi.acpm.inspeckage.hooks.SQLiteHook;
import mobi.acpm.inspeckage.hooks.SSLPinningHook;
import mobi.acpm.inspeckage.hooks.SerializationHook;
import mobi.acpm.inspeckage.hooks.SharedPrefsHook;
import mobi.acpm.inspeckage.hooks.UIHook;
import mobi.acpm.inspeckage.hooks.UserHooks;
import mobi.acpm.inspeckage.hooks.WebViewHook;
import mobi.acpm.inspeckage.hooks.entities.LocationHook;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.util.DexUtil;
import mobi.acpm.inspeckage.util.FileType;
import mobi.acpm.inspeckage.util.FileUtil;

public class Module extends XC_MethodHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    public static final String ERROR = "Inspeckage_Error";
    public static final String MY_PACKAGE_NAME = Module.class.getPackage().getName();
    public static final String PREFS = "InspeckagePrefs";
    public static final String TAG = "Inspeckage_Module:";
    public static XSharedPreferences sPrefs;

    public void initZygote(StartupParam startupParam) throws Throwable {
        sPrefs = new XSharedPreferences(MY_PACKAGE_NAME, PREFS);
        sPrefs.makeWorldReadable();
    }

    public void handleLoadPackage(LoadPackageParam loadPackageParam) throws Throwable {
        sPrefs.reload();
        if (loadPackageParam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            XposedHelpers.findAndHookMethod("mobi.acpm.inspeckage.webserver.WebServer", loadPackageParam.classLoader, "isModuleEnabled", new Object[]{XC_MethodReplacement.returnConstant(Boolean.valueOf(true))});
        }
        if (!loadPackageParam.packageName.equals(BuildConfig.APPLICATION_ID) && loadPackageParam.packageName.equals(sPrefs.getString(Config.SP_PACKAGE, ""))) {
            XposedHelpers.findAndHookMethod("android.util.Log", loadPackageParam.classLoader, "i", new Object[]{String.class, String.class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[0] == "Xposed") {
                        String log = param.args[1];
                        FileType ft = null;
                        if (log.contains(SharedPrefsHook.TAG)) {
                            ft = FileType.PREFS;
                        } else if (log.contains(CryptoHook.TAG)) {
                            ft = FileType.CRYPTO;
                        } else if (log.contains(HashHook.TAG)) {
                            ft = FileType.HASH;
                        } else if (log.contains(SQLiteHook.TAG)) {
                            ft = FileType.SQLITE;
                        } else if (log.contains(ClipboardHook.TAG)) {
                            ft = FileType.CLIPB;
                        } else if (log.contains(IPCHook.TAG)) {
                            ft = FileType.IPC;
                        } else if (log.contains(WebViewHook.TAG)) {
                            ft = FileType.WEBVIEW;
                        } else if (log.contains(FileSystemHook.TAG)) {
                            ft = FileType.FILESYSTEM;
                        } else if (log.contains(MiscHook.TAG)) {
                            ft = FileType.MISC;
                        } else if (log.contains(SerializationHook.TAG)) {
                            ft = FileType.SERIALIZATION;
                        } else if (log.contains(HttpHook.TAG)) {
                            ft = FileType.HTTP;
                        } else if (log.contains(UserHooks.TAG)) {
                            ft = FileType.USERHOOKS;
                        }
                        if (ft != null) {
                            FileUtil.writeToFile(Module.sPrefs, log, ft, "");
                        }
                    }
                }
            }});
            UIHook.initAllHooks(loadPackageParam);
            if (sPrefs.getBoolean(Config.SP_TAB_ENABLE_HTTP, true)) {
                HttpHook.initAllHooks(loadPackageParam);
            }
            if (sPrefs.getBoolean(Config.SP_TAB_ENABLE_MISC, true)) {
                MiscHook.initAllHooks(loadPackageParam);
                ClipboardHook.initAllHooks(loadPackageParam);
            }
            if (sPrefs.getBoolean(Config.SP_TAB_ENABLE_WV, true)) {
                WebViewHook.initAllHooks(loadPackageParam);
            }
            if (sPrefs.getBoolean(Config.SP_TAB_ENABLE_CRYPTO, true)) {
                CryptoHook.initAllHooks(loadPackageParam);
            }
            if (sPrefs.getBoolean(Config.SP_TAB_ENABLE_FS, true)) {
                FileSystemHook.initAllHooks(loadPackageParam);
            }
            FlagSecureHook.initAllHooks(loadPackageParam);
            if (sPrefs.getBoolean(Config.SP_TAB_ENABLE_HASH, true)) {
                HashHook.initAllHooks(loadPackageParam);
            }
            if (sPrefs.getBoolean(Config.SP_TAB_ENABLE_IPC, true)) {
                IPCHook.initAllHooks(loadPackageParam);
            }
            ProxyHook.initAllHooks(loadPackageParam);
            if (sPrefs.getBoolean(Config.SP_TAB_ENABLE_SHAREDP, true)) {
                SharedPrefsHook.initAllHooks(loadPackageParam);
            }
            if (sPrefs.getBoolean(Config.SP_TAB_ENABLE_SQLITE, true)) {
                SQLiteHook.initAllHooks(loadPackageParam);
            }
            SSLPinningHook.initAllHooks(loadPackageParam);
            if (sPrefs.getBoolean(Config.SP_TAB_ENABLE_SERIALIZATION, true)) {
                SerializationHook.initAllHooks(loadPackageParam);
            }
            if (sPrefs.getBoolean(Config.SP_TAB_ENABLE_PHOOKS, true)) {
                UserHooks.initAllHooks(loadPackageParam);
            }
            if (sPrefs.getBoolean(Config.SP_GEOLOCATION_SW, false)) {
                LocationHook.initAllHooks(loadPackageParam);
            }
            FingerprintHook.initAllHooks(loadPackageParam);
            DexUtil.saveClassesWithMethodsJson(loadPackageParam, sPrefs);
        }
    }

    public static void logError(Error e) {
        XposedBridge.log("Inspeckage_Error " + e.getMessage());
    }
}
