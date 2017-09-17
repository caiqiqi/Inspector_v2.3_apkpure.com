package mobi.acpm.inspeckage.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import mobi.acpm.inspeckage.Module;

public class SSLPinningHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_SSLPinning:";
    private static XSharedPreferences sPrefs;

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void initAllHooks(de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam r13) {
        /*
        r12 = 4;
        r11 = 3;
        r10 = 2;
        r9 = 1;
        r8 = 0;
        r3 = "javax.net.ssl.TrustManagerFactory";
        r4 = r13.classLoader;
        r5 = "getTrustManagers";
        r6 = new java.lang.Object[r9];
        r7 = new mobi.acpm.inspeckage.hooks.SSLPinningHook$1;
        r7.<init>();
        r6[r8] = r7;
        de.robv.android.xposed.XposedHelpers.findAndHookMethod(r3, r4, r5, r6);
        r3 = "javax.net.ssl.SSLContext";
        r4 = r13.classLoader;
        r5 = "init";
        r6 = new java.lang.Object[r12];
        r7 = javax.net.ssl.KeyManager[].class;
        r6[r8] = r7;
        r7 = javax.net.ssl.TrustManager[].class;
        r6[r9] = r7;
        r7 = java.security.SecureRandom.class;
        r6[r10] = r7;
        r7 = new mobi.acpm.inspeckage.hooks.SSLPinningHook$2;
        r7.<init>();
        r6[r11] = r7;
        de.robv.android.xposed.XposedHelpers.findAndHookMethod(r3, r4, r5, r6);
        r3 = "javax.net.ssl.HttpsURLConnection";
        r4 = r13.classLoader;
        r5 = "setSSLSocketFactory";
        r6 = new java.lang.Object[r10];
        r7 = javax.net.ssl.SSLSocketFactory.class;
        r6[r8] = r7;
        r7 = new mobi.acpm.inspeckage.hooks.SSLPinningHook$3;
        r7.<init>();
        r6[r9] = r7;
        de.robv.android.xposed.XposedHelpers.findAndHookMethod(r3, r4, r5, r6);
        r3 = "org.apache.http.conn.ssl.HttpsURLConnection";
        r4 = r13.classLoader;	 Catch:{ Error -> 0x0104 }
        r1 = de.robv.android.xposed.XposedHelpers.findClass(r3, r4);	 Catch:{ Error -> 0x0104 }
        if (r1 == 0) goto L_0x00e4;
    L_0x0055:
        r3 = "setDefaultHostnameVerifier";
        r4 = 2;
        r4 = new java.lang.Object[r4];	 Catch:{ Error -> 0x00fe }
        r5 = 0;
        r6 = javax.net.ssl.HostnameVerifier.class;
        r4[r5] = r6;	 Catch:{ Error -> 0x00fe }
        r5 = 1;
        r6 = new mobi.acpm.inspeckage.hooks.SSLPinningHook$4;	 Catch:{ Error -> 0x00fe }
        r6.<init>();	 Catch:{ Error -> 0x00fe }
        r4[r5] = r6;	 Catch:{ Error -> 0x00fe }
        de.robv.android.xposed.XposedHelpers.findAndHookMethod(r1, r3, r4);	 Catch:{ Error -> 0x00fe }
    L_0x006a:
        r3 = "org.apache.http.conn.ssl.HttpsURLConnection";
        r4 = r13.classLoader;	 Catch:{ Error -> 0x0109 }
        r5 = "setHostnameVerifier";
        r6 = 2;
        r6 = new java.lang.Object[r6];	 Catch:{ Error -> 0x0109 }
        r7 = 0;
        r8 = javax.net.ssl.HostnameVerifier.class;
        r6[r7] = r8;	 Catch:{ Error -> 0x0109 }
        r7 = 1;
        r8 = new mobi.acpm.inspeckage.hooks.SSLPinningHook$5;	 Catch:{ Error -> 0x0109 }
        r8.<init>();	 Catch:{ Error -> 0x0109 }
        r6[r7] = r8;	 Catch:{ Error -> 0x0109 }
        de.robv.android.xposed.XposedHelpers.findAndHookMethod(r3, r4, r5, r6);	 Catch:{ Error -> 0x0109 }
    L_0x0083:
        r3 = "org.apache.http.conn.ssl.SSLSocketFactory";
        r4 = r13.classLoader;	 Catch:{ Error -> 0x010f }
        r5 = "getSocketFactory";
        r6 = 1;
        r6 = new java.lang.Object[r6];	 Catch:{ Error -> 0x010f }
        r7 = 0;
        r8 = new mobi.acpm.inspeckage.hooks.SSLPinningHook$6;	 Catch:{ Error -> 0x010f }
        r8.<init>();	 Catch:{ Error -> 0x010f }
        r6[r7] = r8;	 Catch:{ Error -> 0x010f }
        de.robv.android.xposed.XposedHelpers.findAndHookMethod(r3, r4, r5, r6);	 Catch:{ Error -> 0x010f }
    L_0x0097:
        r3 = "org.apache.http.conn.ssl.SSLSocketFactory";
        r4 = r13.classLoader;	 Catch:{ Error -> 0x0114 }
        r2 = de.robv.android.xposed.XposedHelpers.findClass(r3, r4);	 Catch:{ Error -> 0x0114 }
        r3 = 7;
        r3 = new java.lang.Object[r3];	 Catch:{ Error -> 0x0114 }
        r4 = 0;
        r5 = java.lang.String.class;
        r3[r4] = r5;	 Catch:{ Error -> 0x0114 }
        r4 = 1;
        r5 = java.security.KeyStore.class;
        r3[r4] = r5;	 Catch:{ Error -> 0x0114 }
        r4 = 2;
        r5 = java.lang.String.class;
        r3[r4] = r5;	 Catch:{ Error -> 0x0114 }
        r4 = 3;
        r5 = java.security.KeyStore.class;
        r3[r4] = r5;	 Catch:{ Error -> 0x0114 }
        r4 = 4;
        r5 = java.security.SecureRandom.class;
        r3[r4] = r5;	 Catch:{ Error -> 0x0114 }
        r4 = 5;
        r5 = org.apache.http.conn.scheme.HostNameResolver.class;
        r3[r4] = r5;	 Catch:{ Error -> 0x0114 }
        r4 = 6;
        r5 = new mobi.acpm.inspeckage.hooks.SSLPinningHook$7;	 Catch:{ Error -> 0x0114 }
        r5.<init>();	 Catch:{ Error -> 0x0114 }
        r3[r4] = r5;	 Catch:{ Error -> 0x0114 }
        de.robv.android.xposed.XposedHelpers.findAndHookConstructor(r2, r3);	 Catch:{ Error -> 0x0114 }
    L_0x00cb:
        r3 = "org.apache.http.conn.ssl.SSLSocketFactory";
        r4 = r13.classLoader;	 Catch:{ Error -> 0x0119 }
        r5 = "isSecure";
        r6 = 2;
        r6 = new java.lang.Object[r6];	 Catch:{ Error -> 0x0119 }
        r7 = 0;
        r8 = java.net.Socket.class;
        r6[r7] = r8;	 Catch:{ Error -> 0x0119 }
        r7 = 1;
        r8 = new mobi.acpm.inspeckage.hooks.SSLPinningHook$8;	 Catch:{ Error -> 0x0119 }
        r8.<init>();	 Catch:{ Error -> 0x0119 }
        r6[r7] = r8;	 Catch:{ Error -> 0x0119 }
        de.robv.android.xposed.XposedHelpers.findAndHookMethod(r3, r4, r5, r6);	 Catch:{ Error -> 0x0119 }
    L_0x00e4:
        r3 = "okhttp3.CertificatePinner";
        r4 = r13.classLoader;	 Catch:{ Error -> 0x011e }
        r5 = "findMatchingPins";
        r6 = 2;
        r6 = new java.lang.Object[r6];	 Catch:{ Error -> 0x011e }
        r7 = 0;
        r8 = java.lang.String.class;
        r6[r7] = r8;	 Catch:{ Error -> 0x011e }
        r7 = 1;
        r8 = new mobi.acpm.inspeckage.hooks.SSLPinningHook$9;	 Catch:{ Error -> 0x011e }
        r8.<init>();	 Catch:{ Error -> 0x011e }
        r6[r7] = r8;	 Catch:{ Error -> 0x011e }
        de.robv.android.xposed.XposedHelpers.findAndHookMethod(r3, r4, r5, r6);	 Catch:{ Error -> 0x011e }
    L_0x00fd:
        return;
    L_0x00fe:
        r0 = move-exception;
        mobi.acpm.inspeckage.Module.logError(r0);	 Catch:{ Error -> 0x0104 }
        goto L_0x006a;
    L_0x0104:
        r0 = move-exception;
        mobi.acpm.inspeckage.Module.logError(r0);
        goto L_0x00e4;
    L_0x0109:
        r0 = move-exception;
        mobi.acpm.inspeckage.Module.logError(r0);	 Catch:{ Error -> 0x0104 }
        goto L_0x0083;
    L_0x010f:
        r0 = move-exception;
        mobi.acpm.inspeckage.Module.logError(r0);	 Catch:{ Error -> 0x0104 }
        goto L_0x0097;
    L_0x0114:
        r0 = move-exception;
        mobi.acpm.inspeckage.Module.logError(r0);	 Catch:{ Error -> 0x0104 }
        goto L_0x00cb;
    L_0x0119:
        r0 = move-exception;
        mobi.acpm.inspeckage.Module.logError(r0);	 Catch:{ Error -> 0x0104 }
        goto L_0x00e4;
    L_0x011e:
        r0 = move-exception;
        mobi.acpm.inspeckage.Module.logError(r0);
        goto L_0x00fd;
        */
        throw new UnsupportedOperationException("Method not decompiled: mobi.acpm.inspeckage.hooks.SSLPinningHook.initAllHooks(de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam):void");
    }
}
