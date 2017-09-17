package mobi.acpm.inspeckage.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.net.URI;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.util.Config;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.DefaultHttpClient;

public class ProxyHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_Proxy:";
    private static XSharedPreferences sPrefs;

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod("java.net.ProxySelectorImpl", loadPackageParam.classLoader, "select", new Object[]{URI.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                ProxyHook.loadPrefs();
                if (ProxyHook.sPrefs.getBoolean(Config.SP_SWITCH_PROXY, false)) {
                    System.setProperty("proxyHost", ProxyHook.sPrefs.getString(Config.SP_PROXY_HOST, null));
                    System.setProperty("proxyPort", ProxyHook.sPrefs.getString(Config.SP_PROXY_PORT, null));
                    System.setProperty("http.proxyHost", ProxyHook.sPrefs.getString(Config.SP_PROXY_HOST, null));
                    System.setProperty("http.proxyPort", ProxyHook.sPrefs.getString(Config.SP_PROXY_PORT, null));
                    System.setProperty("https.proxyHost", ProxyHook.sPrefs.getString(Config.SP_PROXY_HOST, null));
                    System.setProperty("https.proxyPort", ProxyHook.sPrefs.getString(Config.SP_PROXY_PORT, null));
                    System.setProperty("socksProxyHost", ProxyHook.sPrefs.getString(Config.SP_PROXY_HOST, null));
                    System.setProperty("socksProxyPort", ProxyHook.sPrefs.getString(Config.SP_PROXY_PORT, null));
                    XposedBridge.log("Inspeckage_Proxy: [P:" + ProxyHook.sPrefs.getString(Config.SP_PROXY_HOST, null) + ":" + ProxyHook.sPrefs.getString(Config.SP_PROXY_PORT, null) + "] - URI = " + param.args[0]);
                }
            }
        }});
        XposedHelpers.findAndHookMethod("java.net.ProxySelectorImpl", loadPackageParam.classLoader, "isNonProxyHost", new Object[]{String.class, String.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                ProxyHook.loadPrefs();
                if (ProxyHook.sPrefs.getBoolean(Config.SP_SWITCH_PROXY, false)) {
                    param.args[1] = "--inpeckage--";
                }
            }
        }});
        XposedBridge.hookAllConstructors(XposedHelpers.findClass("org.apache.http.impl.client.DefaultHttpClient", loadPackageParam.classLoader), new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ProxyHook.loadPrefs();
                if (ProxyHook.sPrefs.getBoolean(Config.SP_SWITCH_PROXY, false)) {
                    int proxyPort;
                    String proxyHost = ProxyHook.sPrefs.getString(Config.SP_PROXY_HOST, null);
                    try {
                        proxyPort = Integer.parseInt(ProxyHook.sPrefs.getString(Config.SP_PROXY_PORT, null));
                    } catch (NumberFormatException e) {
                        proxyPort = -1;
                    }
                    DefaultHttpClient httpClient = param.thisObject;
                    httpClient.getParams().setParameter("http.route.default-proxy", new HttpHost(proxyHost, proxyPort));
                }
            }
        });
    }
}
