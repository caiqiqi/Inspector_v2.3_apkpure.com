package mobi.acpm.inspeckage.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.hooks.entities.FingerprintItem;
import mobi.acpm.inspeckage.hooks.entities.FingerprintList;

public class Fingerprint {
    private static Fingerprint instance;
    private static Context mContext;
    private static SharedPreferences mPrefs;

    public Fingerprint(Context context) {
        mContext = context;
        mPrefs = context.getSharedPreferences(Module.PREFS, 1);
    }

    public static Fingerprint getInstance(Context context) {
        if (instance == null) {
            instance = new Fingerprint(context);
        }
        return instance;
    }

    public static void load() {
        FingerprintList list = new FingerprintList();
        List<FingerprintItem> li = new ArrayList();
        String ads_id = mPrefs.getString(Config.SP_ADS_ID, "");
        li.add(new FingerprintItem("Advertising", "ID", ads_id, ads_id, false));
        String mac = getMacAddress(mContext);
        li.add(new FingerprintItem("Wi-Fi", "Android", mac, mac, false));
        WifiInfo wi = ((WifiManager) mContext.getSystemService("wifi")).getConnectionInfo();
        String bssid = wi.getBSSID();
        li.add(new FingerprintItem("Wi-Fi", "BSSID", bssid, bssid, false));
        String ssid = wi.getSSID();
        List<FingerprintItem> list2 = li;
        list2.add(new FingerprintItem("Wi-Fi", "SSID", ssid.substring(1, ssid.length() - 1), ssid.substring(1, ssid.length() - 1), false));
        String ipAddress = Formatter.formatIpAddress(wi.getIpAddress());
        li.add(new FingerprintItem("Wi-Fi", "IP", ipAddress, ipAddress, false));
        TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService("phone");
        li.add(new FingerprintItem("TelephonyManager", "IMEI", mTelephonyManager.getDeviceId(), mTelephonyManager.getDeviceId(), false));
        li.add(new FingerprintItem("TelephonyManager", "IMSI", mTelephonyManager.getSubscriberId(), mTelephonyManager.getSubscriberId(), false));
        li.add(new FingerprintItem("TelephonyManager", "PhoneNumber", mTelephonyManager.getLine1Number(), mTelephonyManager.getLine1Number(), false));
        li.add(new FingerprintItem("TelephonyManager", "SimSerial", mTelephonyManager.getSimSerialNumber(), mTelephonyManager.getSimSerialNumber(), false));
        li.add(new FingerprintItem("TelephonyManager", "CarrierCode", mTelephonyManager.getNetworkOperator(), mTelephonyManager.getNetworkOperator(), false));
        li.add(new FingerprintItem("TelephonyManager", "Carrier", mTelephonyManager.getNetworkOperatorName(), mTelephonyManager.getNetworkOperatorName(), false));
        li.add(new FingerprintItem("TelephonyManager", "SimCountry", mTelephonyManager.getSimCountryIso(), mTelephonyManager.getSimCountryIso(), false));
        li.add(new FingerprintItem("TelephonyManager", "NetworkCountry", mTelephonyManager.getNetworkCountryIso(), mTelephonyManager.getNetworkCountryIso(), false));
        li.add(new FingerprintItem("TelephonyManager", "SimSerialNumber", mTelephonyManager.getSimSerialNumber(), mTelephonyManager.getSimSerialNumber(), false));
        li.add(new FingerprintItem("VERSION", "RELEASE", VERSION.RELEASE, VERSION.RELEASE, false));
        li.add(new FingerprintItem("VERSION", "CODENAME", VERSION.CODENAME, VERSION.CODENAME, false));
        li.add(new FingerprintItem("VERSION", "INCREMENTAL", VERSION.INCREMENTAL, VERSION.INCREMENTAL, false));
        li.add(new FingerprintItem("VERSION", "SDK", VERSION.SDK, VERSION.SDK, false));
        li.add(new FingerprintItem("VERSION", "SDK_INT", String.valueOf(VERSION.SDK_INT), String.valueOf(VERSION.SDK_INT), false));
        if (VERSION.SDK_INT >= 23) {
            li.add(new FingerprintItem("VERSION", "BASE_OS", VERSION.BASE_OS, VERSION.BASE_OS, false));
            li.add(new FingerprintItem("VERSION", "PREVIEW_SDK_INT", String.valueOf(VERSION.PREVIEW_SDK_INT), String.valueOf(VERSION.PREVIEW_SDK_INT), false));
            li.add(new FingerprintItem("VERSION", "SECURITY_PATCH", VERSION.SECURITY_PATCH, VERSION.SECURITY_PATCH, false));
        }
        li.add(new FingerprintItem("BUILD", "BOARD", Build.BOARD, Build.BOARD, false));
        li.add(new FingerprintItem("BUILD", "BOOTLOADER", Build.BOOTLOADER, Build.BOOTLOADER, false));
        li.add(new FingerprintItem("BUILD", "BRAND", Build.BRAND, Build.BRAND, false));
        li.add(new FingerprintItem("BUILD", "CPU_ABI", Build.CPU_ABI, Build.CPU_ABI, false));
        li.add(new FingerprintItem("BUILD", "CPU_ABI2", Build.CPU_ABI2, Build.CPU_ABI2, false));
        li.add(new FingerprintItem("BUILD", "DEVICE", Build.DEVICE, Build.DEVICE, false));
        li.add(new FingerprintItem("BUILD", "DISPLAY", Build.DISPLAY, Build.DISPLAY, false));
        li.add(new FingerprintItem("BUILD", "FINGERPRINT", Build.FINGERPRINT, Build.FINGERPRINT, false));
        li.add(new FingerprintItem("BUILD", "HARDWARE", Build.HARDWARE, Build.HARDWARE, false));
        li.add(new FingerprintItem("BUILD", "HOST", Build.HOST, Build.HOST, false));
        li.add(new FingerprintItem("BUILD", "ID", Build.ID, Build.ID, false));
        li.add(new FingerprintItem("BUILD", "MANUFACTURER", Build.MANUFACTURER, Build.MANUFACTURER, false));
        li.add(new FingerprintItem("BUILD", "MODEL", Build.MODEL, Build.MODEL, false));
        li.add(new FingerprintItem("BUILD", "PRODUCT", Build.PRODUCT, Build.PRODUCT, false));
        li.add(new FingerprintItem("BUILD", "RADIO", Build.RADIO, Build.RADIO, false));
        li.add(new FingerprintItem("BUILD", "SERIAL", Build.SERIAL, Build.SERIAL, false));
        li.add(new FingerprintItem("BUILD", "TAGS", Build.TAGS, Build.TAGS, false));
        li.add(new FingerprintItem("BUILD", "TYPE", Build.TYPE, Build.TYPE, false));
        li.add(new FingerprintItem("BUILD", "USER", Build.USER, Build.USER, false));
        list.fingerprintItems = li;
        Gson gson = new GsonBuilder().create();
        Editor editor = mPrefs.edit();
        Editor editor2 = editor;
        editor2.putString(Config.SP_FINGERPRINT_HOOKS, gson.toJson((Object) list));
        editor.apply();
    }

    private static String getMacAddress(Context context) {
        try {
            for (NetworkInterface interfaces : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (interfaces.getName().equalsIgnoreCase("wlan0")) {
                    byte[] address = interfaces.getHardwareAddress();
                    if (address != null) {
                        StringBuilder sb = new StringBuilder();
                        int length = address.length;
                        for (int i = 0; i < length; i++) {
                            sb.append(String.format("%02X:", new Object[]{Byte.valueOf(address[i])}));
                        }
                        int length2 = sb.length();
                        if (length2 > 0) {
                            sb.deleteCharAt(length2 - 1);
                        }
                        return sb.toString();
                    }
                }
            }
        } catch (SocketException e) {
        }
        return "";
    }
}
