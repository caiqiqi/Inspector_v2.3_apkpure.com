package mobi.acpm.inspeckage.hooks;

import android.os.Build.VERSION;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import mobi.acpm.inspeckage.Module;

public class HttpHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_Http:";

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        try {
            XposedBridge.hookAllConstructors(XposedHelpers.findClass("java.net.HttpURLConnection", loadPackageParam.classLoader), new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args.length == 1 && param.args[0].getClass() == URL.class) {
                        XposedBridge.log("Inspeckage_Http:HttpURLConnection: " + param.args[0] + "");
                    }
                }
            });
        } catch (Error e) {
            Module.logError(e);
        }
        XC_MethodHook RequestHook = new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                HttpURLConnection urlConn = param.thisObject;
                if (urlConn != null) {
                    StringBuilder sb = new StringBuilder();
                    if (!((Boolean) XposedHelpers.getObjectField(param.thisObject, "connected")).booleanValue()) {
                        Map<String, List<String>> properties = urlConn.getRequestProperties();
                        if (properties != null && properties.size() > 0) {
                            for (Entry<String, List<String>> entry : properties.entrySet()) {
                                sb.append(((String) entry.getKey()) + ": " + entry.getValue() + ", ");
                            }
                        }
                        DataOutputStream dos = (DataOutputStream) param.getResult();
                        XposedBridge.log("Inspeckage_Http:REQUEST: method=" + urlConn.getRequestMethod() + " URL=" + urlConn.getURL().toString() + " Params=" + sb.toString());
                    }
                }
            }
        };
        XC_MethodHook ResponseHook = new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                HttpURLConnection urlConn = param.thisObject;
                if (urlConn != null) {
                    StringBuilder sb = new StringBuilder();
                    if (urlConn.getResponseCode() == 200) {
                        Map<String, List<String>> properties = urlConn.getHeaderFields();
                        if (properties != null && properties.size() > 0) {
                            for (Entry<String, List<String>> entry : properties.entrySet()) {
                                sb.append(((String) entry.getKey()) + ": " + entry.getValue() + ", ");
                            }
                        }
                    }
                    XposedBridge.log("Inspeckage_Http:RESPONSE: method=" + urlConn.getRequestMethod() + " URL=" + urlConn.getURL().toString() + " Params=" + sb.toString());
                }
            }
        };
        try {
            Class<?> okHttpClient = XposedHelpers.findClass("com.android.okhttp.OkHttpClient", loadPackageParam.classLoader);
            if (okHttpClient != null) {
                XposedHelpers.findAndHookMethod(okHttpClient, "open", new Object[]{URI.class, new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        URI uri = null;
                        if (param.args[0] != null) {
                            uri = param.args[0];
                        }
                        XposedBridge.log("Inspeckage_Http:OkHttpClient: " + uri.toString() + "");
                    }
                }});
            }
        } catch (Error e2) {
            Module.logError(e2);
        }
        try {
            if (VERSION.SDK_INT <= 19) {
                XposedHelpers.findAndHookMethod("libcore.net.http.HttpURLConnectionImpl", loadPackageParam.classLoader, "getOutputStream", new Object[]{RequestHook});
            } else if (XposedHelpers.findClass("com.android.okhttp.internal.http.HttpURLConnectionImpl", loadPackageParam.classLoader) != null) {
                XposedHelpers.findAndHookMethod("com.android.okhttp.internal.http.HttpURLConnectionImpl", loadPackageParam.classLoader, "getOutputStream", new Object[]{RequestHook});
                XposedHelpers.findAndHookMethod("com.android.okhttp.internal.http.HttpURLConnectionImpl", loadPackageParam.classLoader, "getInputStream", new Object[]{ResponseHook});
            }
        } catch (Error e22) {
            Module.logError(e22);
        }
        XposedHelpers.findAndHookMethod(SSLContext.class, "init", new Object[]{KeyManager[].class, TrustManager[].class, SecureRandom.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                KeyManager[] km = (KeyManager[]) param.args[0];
                TrustManager[] tm_ = (TrustManager[]) param.args[1];
                if (tm_ != null && tm_[0] != null) {
                    X509TrustManager tm = tm_[0];
                    X509Certificate[] chain = new X509Certificate[0];
                    XposedBridge.log("Inspeckage_Http:Possible pinning.");
                }
            }
        }});
    }
}
