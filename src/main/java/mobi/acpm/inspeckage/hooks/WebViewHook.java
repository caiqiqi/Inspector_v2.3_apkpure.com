package mobi.acpm.inspeckage.hooks;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class WebViewHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_WebView:";
    static StringBuilder sb = null;

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(WebView.class, "addJavascriptInterface", new Object[]{Object.class, String.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_WebView:addJavascriptInterface(Object, " + param.args[1] + ");");
            }
        }});
        XposedHelpers.findAndHookMethod(WebView.class, "loadUrl", new Object[]{String.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                WebViewHook.sb = new StringBuilder();
                WebView wv = param.thisObject;
                WebViewHook.sb.append("Load URL: " + param.args[0]);
                WebViewHook.sb.append(WebViewHook.checkSettings(wv));
                XposedBridge.log(WebViewHook.TAG + WebViewHook.sb.toString());
            }
        }});
        XposedHelpers.findAndHookMethod(WebView.class, "loadData", new Object[]{String.class, String.class, String.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                WebViewHook.sb = new StringBuilder();
                WebView wv = param.thisObject;
                WebViewHook.sb.append("Load Data: " + param.args[0]);
                WebViewHook.sb.append(WebViewHook.checkSettings(wv));
                XposedBridge.log(WebViewHook.TAG + WebViewHook.sb.toString());
            }
        }});
        XposedHelpers.findAndHookMethod(WebView.class, "setWebChromeClient", new Object[]{WebChromeClient.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_WebView:Client: WebChrome");
            }
        }});
        XposedHelpers.findAndHookMethod(WebView.class, "setWebViewClient", new Object[]{WebViewClient.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            }
        }});
        XposedHelpers.findAndHookMethod(WebView.class, "setWebContentsDebuggingEnabled", new Object[]{"boolean", new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_WebView:Web Contents Debugging Enabled: " + String.valueOf(((Boolean) param.args[0]).booleanValue()));
            }
        }});
    }

    static String checkSettings(WebView wv) {
        String r = "</br>";
        if (wv.getSettings().getJavaScriptEnabled()) {
            r = r + " -- JavaScript: Enable</br>";
        } else {
            r = r + " -- JavaScript: Disable</br>";
        }
        if (wv.getSettings().getPluginState() == PluginState.OFF) {
            r = r + " -- Plugin State: OFF</br>";
        } else {
            r = r + " -- Plugin State: ON</br>";
        }
        if (wv.getSettings().getAllowFileAccess()) {
            return r + " -- Allow File Access: Enable</br>";
        }
        return r + " -- Allow File Access: Disable</br>";
    }
}
