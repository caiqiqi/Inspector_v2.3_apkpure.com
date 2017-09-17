package mobi.acpm.inspeckage.hooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import mobi.acpm.inspeckage.Module;

public class FingerprintHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_DeviceData: ";
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static XSharedPreferences sPrefs;

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void initAllHooks(de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam r13) {
        /*
        loadPrefs();
        loadPrefs();	 Catch:{ ClassNotFoundError -> 0x009c }
        r9 = sPrefs;	 Catch:{ ClassNotFoundError -> 0x009c }
        r10 = "fingerprint_hooks";
        r11 = "";
        r6 = r9.getString(r10, r11);	 Catch:{ ClassNotFoundError -> 0x009c }
        r9 = "android.os.Build";
        r10 = r13.classLoader;	 Catch:{ ClassNotFoundError -> 0x009c }
        r0 = de.robv.android.xposed.XposedHelpers.findClass(r9, r10);	 Catch:{ ClassNotFoundError -> 0x009c }
        r9 = "android.os.Build.VERSION";
        r10 = r13.classLoader;	 Catch:{ ClassNotFoundError -> 0x009c }
        r1 = de.robv.android.xposed.XposedHelpers.findClass(r9, r10);	 Catch:{ ClassNotFoundError -> 0x009c }
        r9 = gson;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r10 = mobi.acpm.inspeckage.hooks.entities.FingerprintList.class;
        r5 = r9.fromJson(r6, r10);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r5 = (mobi.acpm.inspeckage.hooks.entities.FingerprintList) r5;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r9 = r5.fingerprintItems;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r10 = r9.iterator();	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
    L_0x0030:
        r9 = r10.hasNext();	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        if (r9 == 0) goto L_0x006d;
    L_0x0036:
        r4 = r10.next();	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r4 = (mobi.acpm.inspeckage.hooks.entities.FingerprintItem) r4;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r9 = r4.enable;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        if (r9 == 0) goto L_0x0030;
    L_0x0040:
        r9 = r4.type;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r11 = "BUILD";
        r9 = r9.equals(r11);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        if (r9 == 0) goto L_0x006e;
    L_0x004a:
        r9 = r4.name;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r11 = r4.newValue;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        de.robv.android.xposed.XposedHelpers.setStaticObjectField(r0, r9, r11);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        goto L_0x0030;
    L_0x0052:
        r3 = move-exception;
        r9 = new java.lang.StringBuilder;	 Catch:{ ClassNotFoundError -> 0x009c }
        r9.<init>();	 Catch:{ ClassNotFoundError -> 0x009c }
        r10 = "Inspeckage_DeviceData: ";
        r9 = r9.append(r10);	 Catch:{ ClassNotFoundError -> 0x009c }
        r10 = r3.getMessage();	 Catch:{ ClassNotFoundError -> 0x009c }
        r9 = r9.append(r10);	 Catch:{ ClassNotFoundError -> 0x009c }
        r9 = r9.toString();	 Catch:{ ClassNotFoundError -> 0x009c }
        de.robv.android.xposed.XposedBridge.log(r9);	 Catch:{ ClassNotFoundError -> 0x009c }
    L_0x006d:
        return;
    L_0x006e:
        r9 = r4.type;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r11 = "VERSION";
        r9 = r9.equals(r11);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        if (r9 == 0) goto L_0x00b8;
    L_0x0078:
        r9 = r4.name;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r11 = r4.newValue;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        de.robv.android.xposed.XposedHelpers.setStaticObjectField(r1, r9, r11);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        goto L_0x0030;
    L_0x0080:
        r3 = move-exception;
        r9 = new java.lang.StringBuilder;	 Catch:{ ClassNotFoundError -> 0x009c }
        r9.<init>();	 Catch:{ ClassNotFoundError -> 0x009c }
        r10 = "Inspeckage_DeviceData: ";
        r9 = r9.append(r10);	 Catch:{ ClassNotFoundError -> 0x009c }
        r10 = r3.getMessage();	 Catch:{ ClassNotFoundError -> 0x009c }
        r9 = r9.append(r10);	 Catch:{ ClassNotFoundError -> 0x009c }
        r9 = r9.toString();	 Catch:{ ClassNotFoundError -> 0x009c }
        de.robv.android.xposed.XposedBridge.log(r9);	 Catch:{ ClassNotFoundError -> 0x009c }
        goto L_0x006d;
    L_0x009c:
        r3 = move-exception;
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "Inspeckage_DeviceData: ";
        r9 = r9.append(r10);
        r10 = r3.getMessage();
        r9 = r9.append(r10);
        r9 = r9.toString();
        de.robv.android.xposed.XposedBridge.log(r9);
        goto L_0x006d;
    L_0x00b8:
        r9 = r4.type;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r11 = "TelephonyManager";
        r9 = r9.equals(r11);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        if (r9 == 0) goto L_0x01cc;
    L_0x00c2:
        r11 = r4.name;	 Catch:{ Exception -> 0x0145 }
        r9 = -1;
        r12 = r11.hashCode();	 Catch:{ Exception -> 0x0145 }
        switch(r12) {
            case -2075953448: goto L_0x019f;
            case -1747832408: goto L_0x01b5;
            case -956724853: goto L_0x0189;
            case -619626785: goto L_0x01aa;
            case 2250952: goto L_0x0168;
            case 2251386: goto L_0x0173;
            case 474898999: goto L_0x017e;
            case 899443941: goto L_0x0194;
            case 1655618740: goto L_0x01c0;
            default: goto L_0x00cc;
        };	 Catch:{ Exception -> 0x0145 }
    L_0x00cc:
        switch(r9) {
            case 0: goto L_0x00d1;
            case 1: goto L_0x00fb;
            case 2: goto L_0x0104;
            case 3: goto L_0x010d;
            case 4: goto L_0x0116;
            case 5: goto L_0x011f;
            case 6: goto L_0x0128;
            case 7: goto L_0x0131;
            case 8: goto L_0x013a;
            default: goto L_0x00cf;
        };	 Catch:{ Exception -> 0x0145 }
    L_0x00cf:
        goto L_0x0030;
    L_0x00d1:
        r9 = "android.telephony.TelephonyManager";
        r11 = "getDeviceId";
        r12 = r4.newValue;	 Catch:{ Exception -> 0x0145 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ Exception -> 0x0145 }
        r9 = "com.android.internal.telephony.PhoneSubInfo";
        r11 = "getDeviceId";
        r12 = r4.newValue;	 Catch:{ Exception -> 0x0145 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ Exception -> 0x0145 }
        r9 = "com.android.internal.telephony.PhoneProxy";
        r11 = "getDeviceId";
        r12 = r4.newValue;	 Catch:{ Exception -> 0x0145 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ Exception -> 0x0145 }
        r9 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x0145 }
        r11 = 22;
        if (r9 >= r11) goto L_0x00fb;
    L_0x00f2:
        r9 = "com.android.internal.telephony.gsm.GSMPhone";
        r11 = "getDeviceId";
        r12 = r4.newValue;	 Catch:{ Exception -> 0x0145 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ Exception -> 0x0145 }
    L_0x00fb:
        r9 = "android.telephony.TelephonyManager";
        r11 = "getSubscriberId";
        r12 = r4.newValue;	 Catch:{ Exception -> 0x0145 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ Exception -> 0x0145 }
    L_0x0104:
        r9 = "android.telephony.TelephonyManager";
        r11 = "getLine1Number";
        r12 = r4.newValue;	 Catch:{ Exception -> 0x0145 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ Exception -> 0x0145 }
    L_0x010d:
        r9 = "android.telephony.TelephonyManager";
        r11 = "getSimSerialNumber";
        r12 = r4.newValue;	 Catch:{ Exception -> 0x0145 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ Exception -> 0x0145 }
    L_0x0116:
        r9 = "android.telephony.TelephonyManager";
        r11 = "getNetworkOperator";
        r12 = r4.newValue;	 Catch:{ Exception -> 0x0145 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ Exception -> 0x0145 }
    L_0x011f:
        r9 = "android.telephony.TelephonyManager";
        r11 = "getNetworkOperatorName";
        r12 = r4.newValue;	 Catch:{ Exception -> 0x0145 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ Exception -> 0x0145 }
    L_0x0128:
        r9 = "android.telephony.TelephonyManager";
        r11 = "getSimCountryIso";
        r12 = r4.newValue;	 Catch:{ Exception -> 0x0145 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ Exception -> 0x0145 }
    L_0x0131:
        r9 = "android.telephony.TelephonyManager";
        r11 = "getNetworkCountryIso";
        r12 = r4.newValue;	 Catch:{ Exception -> 0x0145 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ Exception -> 0x0145 }
    L_0x013a:
        r9 = "android.telephony.TelephonyManager";
        r11 = "getSimSerialNumber";
        r12 = r4.newValue;	 Catch:{ Exception -> 0x0145 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ Exception -> 0x0145 }
        goto L_0x0030;
    L_0x0145:
        r3 = move-exception;
        r9 = new java.lang.StringBuilder;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r9.<init>();	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r11 = "Inspeckage_DeviceData: ";
        r9 = r9.append(r11);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r11 = r4.name;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r9 = r9.append(r11);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r11 = r3.getMessage();	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r9 = r9.append(r11);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r9 = r9.toString();	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        de.robv.android.xposed.XposedBridge.log(r9);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        goto L_0x0030;
    L_0x0168:
        r12 = "IMEI";
        r11 = r11.equals(r12);	 Catch:{ Exception -> 0x0145 }
        if (r11 == 0) goto L_0x00cc;
    L_0x0170:
        r9 = 0;
        goto L_0x00cc;
    L_0x0173:
        r12 = "IMSI";
        r11 = r11.equals(r12);	 Catch:{ Exception -> 0x0145 }
        if (r11 == 0) goto L_0x00cc;
    L_0x017b:
        r9 = 1;
        goto L_0x00cc;
    L_0x017e:
        r12 = "PhoneNumber";
        r11 = r11.equals(r12);	 Catch:{ Exception -> 0x0145 }
        if (r11 == 0) goto L_0x00cc;
    L_0x0186:
        r9 = 2;
        goto L_0x00cc;
    L_0x0189:
        r12 = "SimSerial";
        r11 = r11.equals(r12);	 Catch:{ Exception -> 0x0145 }
        if (r11 == 0) goto L_0x00cc;
    L_0x0191:
        r9 = 3;
        goto L_0x00cc;
    L_0x0194:
        r12 = "CarrierCode";
        r11 = r11.equals(r12);	 Catch:{ Exception -> 0x0145 }
        if (r11 == 0) goto L_0x00cc;
    L_0x019c:
        r9 = 4;
        goto L_0x00cc;
    L_0x019f:
        r12 = "Carrier";
        r11 = r11.equals(r12);	 Catch:{ Exception -> 0x0145 }
        if (r11 == 0) goto L_0x00cc;
    L_0x01a7:
        r9 = 5;
        goto L_0x00cc;
    L_0x01aa:
        r12 = "SimCountry";
        r11 = r11.equals(r12);	 Catch:{ Exception -> 0x0145 }
        if (r11 == 0) goto L_0x00cc;
    L_0x01b2:
        r9 = 6;
        goto L_0x00cc;
    L_0x01b5:
        r12 = "NetworkCountry";
        r11 = r11.equals(r12);	 Catch:{ Exception -> 0x0145 }
        if (r11 == 0) goto L_0x00cc;
    L_0x01bd:
        r9 = 7;
        goto L_0x00cc;
    L_0x01c0:
        r12 = "SimSerialNumber";
        r11 = r11.equals(r12);	 Catch:{ Exception -> 0x0145 }
        if (r11 == 0) goto L_0x00cc;
    L_0x01c8:
        r9 = 8;
        goto L_0x00cc;
    L_0x01cc:
        r9 = r4.type;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r11 = "Advertising";
        r9 = r9.equals(r11);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        if (r9 == 0) goto L_0x01e4;
    L_0x01d6:
        r9 = "com.google.android.gms.ads.identifier.AdvertisingIdClient$Info";
        r11 = "getId";
        r12 = r4.newValue;	 Catch:{ ClassNotFoundError -> 0x01e1, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ ClassNotFoundError -> 0x01e1, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        goto L_0x0030;
    L_0x01e1:
        r9 = move-exception;
        goto L_0x0030;
    L_0x01e4:
        r9 = r4.type;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r11 = "Wi-Fi";
        r9 = r9.equals(r11);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        if (r9 == 0) goto L_0x026a;
    L_0x01ee:
        r11 = r4.name;	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r9 = -1;
        r12 = r11.hashCode();	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        switch(r12) {
            case 2343: goto L_0x021f;
            case 2554747: goto L_0x0215;
            case 63507133: goto L_0x020b;
            case 803262031: goto L_0x0229;
            default: goto L_0x01f8;
        };	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
    L_0x01f8:
        switch(r9) {
            case 0: goto L_0x01fd;
            case 1: goto L_0x0233;
            case 2: goto L_0x023e;
            case 3: goto L_0x025b;
            default: goto L_0x01fb;
        };	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
    L_0x01fb:
        goto L_0x0030;
    L_0x01fd:
        r9 = "android.net.wifi.WifiInfo";
        r11 = "getBSSID";
        r12 = r4.newValue;	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        goto L_0x0030;
    L_0x0208:
        r9 = move-exception;
        goto L_0x0030;
    L_0x020b:
        r12 = "BSSID";
        r11 = r11.equals(r12);	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        if (r11 == 0) goto L_0x01f8;
    L_0x0213:
        r9 = 0;
        goto L_0x01f8;
    L_0x0215:
        r12 = "SSID";
        r11 = r11.equals(r12);	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        if (r11 == 0) goto L_0x01f8;
    L_0x021d:
        r9 = 1;
        goto L_0x01f8;
    L_0x021f:
        r12 = "IP";
        r11 = r11.equals(r12);	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        if (r11 == 0) goto L_0x01f8;
    L_0x0227:
        r9 = 2;
        goto L_0x01f8;
    L_0x0229:
        r12 = "Android";
        r11 = r11.equals(r12);	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        if (r11 == 0) goto L_0x01f8;
    L_0x0231:
        r9 = 3;
        goto L_0x01f8;
    L_0x0233:
        r9 = "android.net.wifi.WifiInfo";
        r11 = "getSSID";
        r12 = r4.newValue;	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        goto L_0x0030;
    L_0x023e:
        r8 = 0;
        r9 = r4.newValue;	 Catch:{ UnknownHostException -> 0x0256 }
        r9 = java.net.InetAddress.getByName(r9);	 Catch:{ UnknownHostException -> 0x0256 }
        r8 = mobi.acpm.inspeckage.util.Util.inetAddressToInt(r9);	 Catch:{ UnknownHostException -> 0x0256 }
    L_0x0249:
        r9 = "android.net.wifi.WifiInfo";
        r11 = "getIpAddress";
        r12 = java.lang.Integer.valueOf(r8);	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        HookFingerprintItem(r9, r13, r11, r12);	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        goto L_0x0030;
    L_0x0256:
        r2 = move-exception;
        r2.printStackTrace();	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        goto L_0x0249;
    L_0x025b:
        r9 = r4.newValue;	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r7 = mobi.acpm.inspeckage.util.Util.macAddressToByteArr(r9);	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r9 = "java.net.NetworkInterface";
        r11 = "getHardwareAddress";
        HookFingerprintItem(r9, r13, r11, r7);	 Catch:{ ClassNotFoundError -> 0x0208, JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        goto L_0x0030;
    L_0x026a:
        r9 = r4.type;	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        r11 = "Wi-Fi";
        r9 = r9.equals(r11);	 Catch:{ JsonSyntaxException -> 0x0052, NoSuchMethodError -> 0x0080 }
        if (r9 == 0) goto L_0x0030;
    L_0x0274:
        goto L_0x0030;
        */
        throw new UnsupportedOperationException("Method not decompiled: mobi.acpm.inspeckage.hooks.FingerprintHook.initAllHooks(de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam):void");
    }

    private static void HookFingerprintItem(String hookClass, LoadPackageParam loadPkgParam, String methodName, final Object value) {
        try {
            XposedHelpers.findAndHookMethod(hookClass, loadPkgParam.classLoader, methodName, new Object[]{new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    param.setResult(value);
                }
            }});
        } catch (Exception e) {
            XposedBridge.log(TAG + methodName + " ERROR: " + e.getMessage());
        }
    }
}
