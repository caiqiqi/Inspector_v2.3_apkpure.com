package mobi.acpm.inspeckage.webserver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec.Builder;
import android.text.Html;
import android.util.Log;
import com.google.android.gms.drive.DriveFile;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import javax.net.ssl.KeyManagerFactory;
import javax.security.auth.x500.X500Principal;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.hooks.CryptoHook;
import mobi.acpm.inspeckage.hooks.FileSystemHook;
import mobi.acpm.inspeckage.hooks.HashHook;
import mobi.acpm.inspeckage.hooks.HttpHook;
import mobi.acpm.inspeckage.hooks.IPCHook;
import mobi.acpm.inspeckage.hooks.MiscHook;
import mobi.acpm.inspeckage.hooks.SQLiteHook;
import mobi.acpm.inspeckage.hooks.SerializationHook;
import mobi.acpm.inspeckage.hooks.SharedPrefsHook;
import mobi.acpm.inspeckage.hooks.UserHooks;
import mobi.acpm.inspeckage.hooks.WebViewHook;
import mobi.acpm.inspeckage.log.LogService;
import mobi.acpm.inspeckage.receivers.InspeckageWebReceiver;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.util.FileType;
import mobi.acpm.inspeckage.util.FileUtil;
import mobi.acpm.inspeckage.util.Fingerprint;
import mobi.acpm.inspeckage.util.PackageDetail;
import mobi.acpm.inspeckage.util.Util;
import org.java_websocket.util.Base64;

public class WebServer extends NanoHTTPD {
    private KeyStore keyStore;
    private Context mContext;
    private SharedPreferences mPrefs;

    public WebServer(String host, int port, Context context) throws IOException {
        GeneralSecurityException e;
        super(host, port);
        this.mContext = context;
        Context context2 = this.mContext;
        String str = Module.PREFS;
        Context context3 = this.mContext;
        this.mPrefs = context2.getSharedPreferences(str, 1);
        try {
            this.keyStore = KeyStore.getInstance("AndroidKeyStore");
            this.keyStore.load(null);
            Enumeration<String> aliases = this.keyStore.aliases();
            List<String> keyAliases = new ArrayList();
            while (aliases.hasMoreElements()) {
                keyAliases.add(aliases.nextElement());
            }
            if (this.mPrefs.getString(Config.KEYPAIR_ALIAS, "").equals("")) {
                Editor edit = this.mPrefs.edit();
                edit.putString(Config.KEYPAIR_ALIAS, UUID.randomUUID().toString());
                edit.apply();
            }
            String alias = this.mPrefs.getString(Config.KEYPAIR_ALIAS, "");
            boolean genNewKey = true;
            for (String key : keyAliases) {
                if (key.equals(alias)) {
                    genNewKey = false;
                }
            }
            if (genNewKey) {
                KeyPair keyPair = generateKeys(alias);
                this.keyStore = KeyStore.getInstance("AndroidKeyStore");
                this.keyStore.load(null);
            }
        } catch (Exception e2) {
            Log.e("Error", e2.getMessage());
        }
        if (this.mPrefs.getBoolean(Config.SP_SWITCH_AUTH, false)) {
            KeyManagerFactory keyManagerFactory = null;
            try {
                keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(this.keyStore, "".toCharArray());
            } catch (NoSuchAlgorithmException e3) {
                e = e3;
                e.printStackTrace();
                makeSecure(NanoHTTPD.makeSSLSocketFactory(this.keyStore, keyManagerFactory), null);
                this.mContext.registerReceiver(new InspeckageWebReceiver(this.mContext), new IntentFilter("mobi.acpm.inspeckage.INSPECKAGE_WEB"));
                start(10000);
            } catch (UnrecoverableKeyException e4) {
                e = e4;
                e.printStackTrace();
                makeSecure(NanoHTTPD.makeSSLSocketFactory(this.keyStore, keyManagerFactory), null);
                this.mContext.registerReceiver(new InspeckageWebReceiver(this.mContext), new IntentFilter("mobi.acpm.inspeckage.INSPECKAGE_WEB"));
                start(10000);
            } catch (KeyStoreException e5) {
                e = e5;
                e.printStackTrace();
                makeSecure(NanoHTTPD.makeSSLSocketFactory(this.keyStore, keyManagerFactory), null);
                this.mContext.registerReceiver(new InspeckageWebReceiver(this.mContext), new IntentFilter("mobi.acpm.inspeckage.INSPECKAGE_WEB"));
                start(10000);
            }
            makeSecure(NanoHTTPD.makeSSLSocketFactory(this.keyStore, keyManagerFactory), null);
        }
        this.mContext.registerReceiver(new InspeckageWebReceiver(this.mContext), new IntentFilter("mobi.acpm.inspeckage.INSPECKAGE_WEB"));
        start(10000);
    }

    public KeyPair generateKeys(String alias) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(1, 1);
            if (VERSION.SDK_INT > 23) {
                keyGen.initialize(new Builder(alias, 12).setCertificateSubject(new X500Principal("CN=Inspeckage, OU=ACPM, O=ACPM, C=BR")).setDigests(new String[]{"SHA-256", "SHA-512"}).setSignaturePaddings(new String[]{"PKCS1"}).setCertificateNotBefore(start.getTime()).setCertificateNotAfter(end.getTime()).setKeyValidityStart(start.getTime()).setKeyValidityEnd(end.getTime()).setKeySize(2048).setCertificateSerialNumber(BigInteger.valueOf(1)).build());
            } else {
                keyGen.initialize(new KeyPairGeneratorSpec.Builder(this.mContext).setAlias(alias).setSubject(new X500Principal("CN=Inspeckage, OU=ACPM, O=ACPM, C=BR")).setSerialNumber(BigInteger.valueOf(12345)).setStartDate(start.getTime()).setEndDate(end.getTime()).build());
            }
            return keyGen.generateKeyPair();
        } catch (GeneralSecurityException e) {
            Log.d("Inspeckage_Exception: ", e.getMessage());
            return null;
        }
    }

    private Response ok(String type, String html, String cacheTime) {
        Response response = NanoHTTPD.newFixedLengthResponse(Status.OK, type, html);
        response.addHeader("Cache-Control", "public");
        response.addHeader("Cache-Control", "max-age=" + cacheTime);
        return response;
    }

    private Response ok(String type, String html) {
        return NanoHTTPD.newFixedLengthResponse(Status.OK, type, html);
    }

    private Response ok(String html) {
        return NanoHTTPD.newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_HTML, html);
    }

    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if (this.mPrefs.getBoolean(Config.SP_SWITCH_AUTH, false)) {
            String authorization = (String) session.getHeaders().get("authorization");
            String base64 = "";
            if (authorization != null) {
                base64 = authorization.substring(6);
            }
            boolean logged = false;
            if (base64.equals(Base64.encodeBytes(this.mPrefs.getString(Config.SP_USER_PASS, "").getBytes()))) {
                logged = true;
            }
            if (!logged) {
                Response res = NanoHTTPD.newFixedLengthResponse(Status.UNAUTHORIZED, NanoHTTPD.MIME_HTML, "Denied!");
                res.addHeader("WWW-Authenticate", "Basic realm=\"Server\"");
                res.addHeader("Content-Length", "0");
                return res;
            }
        }
        if (this.mPrefs.getString(Config.SP_PROXY_HOST, "").equals("")) {
            String ip = (String) session.getHeaders().get("http-client-ip");
            if (!(ip == null || ip.trim().equals(""))) {
                Editor edit = this.mPrefs.edit();
                edit.putString(Config.SP_PROXY_HOST, ip);
                edit.putString(Config.SP_PROXY_PORT, "4443");
                edit.apply();
            }
        }
        Map<String, String> parms = session.getParms();
        String type = (String) parms.get("type");
        String html = new String();
        if (uri.equals("/")) {
            if (type != null) {
                Object obj = -1;
                switch (type.hashCode()) {
                    case -1926036653:
                        if (type.equals(Config.SP_EXPORTED)) {
                            obj = 14;
                            break;
                        }
                        break;
                    case -1897185602:
                        if (type.equals("startWS")) {
                            obj = null;
                            break;
                        }
                        break;
                    case -1794173116:
                        if (type.equals("adduserhooks")) {
                            obj = 19;
                            break;
                        }
                        break;
                    case -1775178318:
                        if (type.equals("restartapp")) {
                            obj = 10;
                            break;
                        }
                        break;
                    case -1665876945:
                        if (type.equals("getuserhooks")) {
                            obj = 22;
                            break;
                        }
                        break;
                    case -1600397930:
                        if (type.equals("clipboard")) {
                            obj = 29;
                            break;
                        }
                        break;
                    case -1588284363:
                        if (type.equals("resetfingerprint")) {
                            obj = 32;
                            break;
                        }
                        break;
                    case -1430926716:
                        if (type.equals(Config.SP_UNPINNING)) {
                            obj = 18;
                            break;
                        }
                        break;
                    case -1233024403:
                        if (type.equals("addbuild")) {
                            obj = 26;
                            break;
                        }
                        break;
                    case -1232376778:
                        if (type.equals("getparamreplaces")) {
                            obj = 23;
                            break;
                        }
                        break;
                    case -968838587:
                        if (type.equals("getreturnreplaces")) {
                            obj = 24;
                            break;
                        }
                        break;
                    case -905786691:
                        if (type.equals("setarp")) {
                            obj = 6;
                            break;
                        }
                        break;
                    case -892069282:
                        if (type.equals("stopWS")) {
                            obj = 1;
                            break;
                        }
                        break;
                    case -779192987:
                        if (type.equals("flagsec")) {
                            obj = 15;
                            break;
                        }
                        break;
                    case -734573798:
                        if (type.equals("filetree")) {
                            obj = 2;
                            break;
                        }
                        break;
                    case -631664878:
                        if (type.equals("enableTab")) {
                            obj = 28;
                            break;
                        }
                        break;
                    case -416447130:
                        if (type.equals("screenshot")) {
                            obj = 5;
                            break;
                        }
                        break;
                    case -177720117:
                        if (type.equals("addparamreplaces")) {
                            obj = 20;
                            break;
                        }
                        break;
                    case 3143036:
                        if (type.equals("file")) {
                            obj = 13;
                            break;
                        }
                        break;
                    case 106941038:
                        if (type.equals("proxy")) {
                            obj = 16;
                            break;
                        }
                        break;
                    case 109757538:
                        if (type.equals("start")) {
                            obj = 12;
                            break;
                        }
                        break;
                    case 591347886:
                        if (type.equals("finishapp")) {
                            obj = 9;
                            break;
                        }
                        break;
                    case 1109604868:
                        if (type.equals("downloadfile")) {
                            obj = 4;
                            break;
                        }
                        break;
                    case 1316799103:
                        if (type.equals("startapp")) {
                            obj = 11;
                            break;
                        }
                        break;
                    case 1536890905:
                        if (type.equals("checkapp")) {
                            obj = 3;
                            break;
                        }
                        break;
                    case 1561122010:
                        if (type.equals("geolocationSwitch")) {
                            obj = 31;
                            break;
                        }
                        break;
                    case 1660746832:
                        if (type.equals("addreturnreplaces")) {
                            obj = 21;
                            break;
                        }
                        break;
                    case 1764356602:
                        if (type.equals("deleteLogs")) {
                            obj = 27;
                            break;
                        }
                        break;
                    case 1847163327:
                        if (type.equals("downall")) {
                            obj = 8;
                            break;
                        }
                        break;
                    case 1847163450:
                        if (type.equals("downapk")) {
                            obj = 7;
                            break;
                        }
                        break;
                    case 1901043637:
                        if (type.equals("location")) {
                            obj = 30;
                            break;
                        }
                        break;
                    case 1979473112:
                        if (type.equals("getbuild")) {
                            obj = 25;
                            break;
                        }
                        break;
                    case 2145022906:
                        if (type.equals("switchproxy")) {
                            obj = 17;
                            break;
                        }
                        break;
                }
                switch (obj) {
                    case null:
                        return startWS(parms);
                    case 1:
                        return stopWS();
                    case 2:
                        return fileTreeHtml();
                    case 3:
                        return checkApp();
                    case 4:
                        return downloadFile(parms);
                    case 5:
                        return takeScreenshot();
                    case 6:
                        return setArp(parms);
                    case 7:
                        return downloadApk();
                    case 8:
                        return downloadAll();
                    case 9:
                        finishApp();
                        return ok("OK");
                    case 10:
                        finishApp();
                        startApp();
                        return ok("OK");
                    case 11:
                        startApp();
                        return ok("OK");
                    case 12:
                        return startComponent(parms);
                    case 13:
                        html = fileHtml(parms);
                        break;
                    case 14:
                        html = spExported(parms);
                        break;
                    case 15:
                        html = flagSecure(parms);
                        break;
                    case 16:
                        html = proxy(parms);
                        break;
                    case 17:
                        html = switchProxy(parms);
                        break;
                    case 18:
                        html = sslUnpinning(parms);
                        break;
                    case 19:
                        return addUserHooks(parms);
                    case 20:
                        return addUserReplaces(parms);
                    case 21:
                        return addUserReturnReplaces(parms);
                    case 22:
                        return getUserHooks();
                    case 23:
                        return getUserReplaces();
                    case 24:
                        return getUserReturnReplaces();
                    case 25:
                        return getBuild();
                    case 26:
                        return addBuild(parms);
                    case 27:
                        return clearHooksLog(parms);
                    case 28:
                        html = tabsCheckbox(parms);
                        break;
                    case 29:
                        return addToClipboard(parms);
                    case 30:
                        return addLocation(parms);
                    case 31:
                        return geoLocSwitch(parms);
                    case 32:
                        return resetFingerprint();
                }
            }
            html = setDefaultOptions();
        } else if (uri.equals("/index.html")) {
            html = FileUtil.readHtmlFile(this.mContext, uri);
        } else if (uri.equals("/logcat.html")) {
            return ok(FileUtil.readHtmlFile(this.mContext, uri).replace("#ip_ws#", this.mPrefs.getString(Config.SP_SERVER_IP, "127.0.0.1")).replace("#port_ws#", String.valueOf(this.mPrefs.getInt(Config.SP_WSOCKET_PORT, 8887))));
        } else if (uri.contains("/content/")) {
            html = FileUtil.readHtmlFile(this.mContext, uri);
            if (uri.contains("location.html")) {
                html = html.replace("#savedLoc#", this.mPrefs.getString(Config.SP_GEOLOCATION, ""));
                if (this.mPrefs.getBoolean(Config.SP_GEOLOCATION_SW, false)) {
                    html = html.replace("#switchLoc#", "<input type='checkbox' name='savedLoc' data-size='mini' checked>");
                } else {
                    html = html.replace("#switchLoc#", "<input type='checkbox' name='savedLoc' data-size='mini' unchecked>");
                }
            }
        } else if (uri.equals(Config.P_APP_STRUCT)) {
            return ok("text/json", FileUtil.readFromFile(this.mPrefs, FileType.APP_STRUCT));
        } else {
            String fname = FileUtil.readHtmlFile(this.mContext, uri);
            if (uri.contains(".css")) {
                return ok("text/css", fname, "86400");
            }
            if (uri.contains(".js")) {
                return ok("text/javascript", fname, "86400");
            }
            if (uri.contains(".png")) {
                try {
                    return NanoHTTPD.newChunkedResponse(Status.OK, "image/png", this.mContext.getAssets().open("HTMLFiles" + uri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (uri.contains(".ico")) {
                return ok("image/vnd.microsoft.icon", fname);
            }
            if (uri.contains(".eot")) {
                return ok("application/vnd.ms-fontobject", fname);
            }
            if (uri.contains(".svg")) {
                return ok("image/svg+xml", fname);
            }
            if (uri.contains(".ttf")) {
                return ok("application/x-font-ttf", fname);
            }
            if (uri.contains(".woff")) {
                return ok("application/font-woff", fname);
            }
            if (uri.contains(".woff2")) {
                return ok("font/woff2", fname);
            }
            return ok(fname);
        }
        if (this.mPrefs.getBoolean(Config.SP_EXPORTED, false)) {
            PackageDetail packageDetail = new PackageDetail(this.mContext, this.mPrefs.getString(Config.SP_PACKAGE, ""));
            edit = this.mPrefs.edit();
            edit.putString(Config.SP_EXP_ACTIVITIES, packageDetail.getExportedActivities());
            edit.putString(Config.SP_N_EXP_ACTIVITIES, packageDetail.getNonExportedActivities());
            edit.apply();
        }
        if (!this.mPrefs.getString(Config.SP_DATA_DIR_TREE, "").equals("")) {
            html = html.replace("#filetree#", this.mPrefs.getString(Config.SP_DATA_DIR_TREE, ""));
        }
        String moduleEnable = "true";
        if (!isModuleEnabled()) {
            moduleEnable = "<font style=\"color:red; background:yellow;\">false</font>";
        }
        html = replaceHtmlVariables(html.replace("#moduleEnable#", moduleEnable));
        try {
            html = html.replace("#inspeckageVersion#", this.mContext.getPackageManager().getPackageInfo(this.mContext.getPackageName(), 0).versionName);
        } catch (NameNotFoundException e2) {
            e2.printStackTrace();
        }
        return ok(html);
    }

    private String setDefaultOptions() {
        Editor edit = this.mPrefs.edit();
        edit.putBoolean(Config.SP_APP_IS_RUNNING, false);
        edit.putString(Config.SP_DATA_DIR_TREE, "");
        edit.apply();
        isRunning();
        fileTree();
        return FileUtil.readHtmlFile(this.mContext, "/index.html");
    }

    private String tabsCheckbox(Map<String, String> parms) {
        String tab = (String) parms.get("tab");
        if (tab != null) {
            String state = (String) parms.get("value");
            Editor edit = this.mPrefs.edit();
            Object obj = -1;
            switch (tab.hashCode()) {
                case -1572513109:
                    if (tab.equals("filesystem")) {
                        obj = 6;
                        break;
                    }
                    break;
                case -1351683903:
                    if (tab.equals("crypto")) {
                        obj = 2;
                        break;
                    }
                    break;
                case -989039296:
                    if (tab.equals("phooks")) {
                        obj = 10;
                        break;
                    }
                    break;
                case -903566235:
                    if (tab.equals("shared")) {
                        obj = null;
                        break;
                    }
                    break;
                case -894935028:
                    if (tab.equals("sqlite")) {
                        obj = 4;
                        break;
                    }
                    break;
                case 104476:
                    if (tab.equals("ipc")) {
                        obj = 9;
                        break;
                    }
                    break;
                case 3195150:
                    if (tab.equals("hash")) {
                        obj = 3;
                        break;
                    }
                    break;
                case 3213448:
                    if (tab.equals("http")) {
                        obj = 5;
                        break;
                    }
                    break;
                case 3351788:
                    if (tab.equals("misc")) {
                        obj = 7;
                        break;
                    }
                    break;
                case 922807280:
                    if (tab.equals("serialization")) {
                        obj = 1;
                        break;
                    }
                    break;
                case 1224424441:
                    if (tab.equals("webview")) {
                        obj = 8;
                        break;
                    }
                    break;
            }
            switch (obj) {
                case null:
                    edit.putBoolean(Config.SP_TAB_ENABLE_SHAREDP, Boolean.valueOf(state).booleanValue());
                    break;
                case 1:
                    edit.putBoolean(Config.SP_TAB_ENABLE_SERIALIZATION, Boolean.valueOf(state).booleanValue());
                    break;
                case 2:
                    edit.putBoolean(Config.SP_TAB_ENABLE_CRYPTO, Boolean.valueOf(state).booleanValue());
                    break;
                case 3:
                    edit.putBoolean(Config.SP_TAB_ENABLE_HASH, Boolean.valueOf(state).booleanValue());
                    break;
                case 4:
                    edit.putBoolean(Config.SP_TAB_ENABLE_SQLITE, Boolean.valueOf(state).booleanValue());
                    break;
                case 5:
                    edit.putBoolean(Config.SP_TAB_ENABLE_HTTP, Boolean.valueOf(state).booleanValue());
                    break;
                case 6:
                    edit.putBoolean(Config.SP_TAB_ENABLE_FS, Boolean.valueOf(state).booleanValue());
                    break;
                case 7:
                    edit.putBoolean(Config.SP_TAB_ENABLE_MISC, Boolean.valueOf(state).booleanValue());
                    break;
                case 8:
                    edit.putBoolean(Config.SP_TAB_ENABLE_WV, Boolean.valueOf(state).booleanValue());
                    break;
                case 9:
                    edit.putBoolean(Config.SP_TAB_ENABLE_IPC, Boolean.valueOf(state).booleanValue());
                    break;
                case 10:
                    edit.putBoolean(Config.SP_TAB_ENABLE_PHOOKS, Boolean.valueOf(state).booleanValue());
                    break;
            }
            edit.apply();
        }
        return "#tab_scheckbox#";
    }

    private String sslUnpinning(Map<String, String> parms) {
        String ssl_switch = (String) parms.get("sslswitch");
        if (ssl_switch != null) {
            Editor edit = this.mPrefs.edit();
            edit.putBoolean(Config.SP_UNPINNING, Boolean.valueOf(ssl_switch).booleanValue());
            edit.apply();
            if (Boolean.valueOf(ssl_switch).booleanValue()) {
                Util.showNotification(this.mContext, "Disable SSL");
            }
        }
        return "#sslunpinning#";
    }

    private String switchProxy(Map<String, String> parms) {
        String pswitch = (String) parms.get("value");
        if (pswitch != null) {
            String host = this.mPrefs.getString(Config.SP_PROXY_HOST, "");
            String port = this.mPrefs.getString(Config.SP_PROXY_PORT, "");
            Editor edit = this.mPrefs.edit();
            if (!Boolean.valueOf(pswitch).booleanValue() || host.length() <= 1 || port.length() <= 0) {
                edit.putBoolean(Config.SP_SWITCH_PROXY, false);
            } else {
                edit.putBoolean(Config.SP_SWITCH_PROXY, true);
                Util.showNotification(this.mContext, "Proxy Enable");
            }
            edit.apply();
        }
        return "#proxy#";
    }

    private String proxy(Map<String, String> parms) {
        String host = (String) parms.get(Config.SP_PROXY_HOST);
        String port = (String) parms.get(Config.SP_PROXY_PORT);
        if (!(host == null || port == null || !Util.isInt(port))) {
            Editor edit = this.mPrefs.edit();
            edit.putString(Config.SP_PROXY_PORT, port);
            edit.putString(Config.SP_PROXY_HOST, host);
            edit.apply();
            Util.showNotification(this.mContext, "Save Proxy: " + host + ":" + port);
        }
        return "#proxy#";
    }

    private String flagSecure(Map<String, String> parms) {
        String fs_switch = (String) parms.get("fsswitch");
        if (fs_switch != null) {
            Editor edit = this.mPrefs.edit();
            edit.putBoolean(Config.SP_FLAG_SECURE, Boolean.valueOf(fs_switch).booleanValue());
            edit.apply();
            if (Boolean.valueOf(fs_switch).booleanValue()) {
                Util.showNotification(this.mContext, "Disable all FLAG_SECURE");
            }
        }
        return "#flags#";
    }

    private String spExported(Map<String, String> parms) {
        String value = (String) parms.get("value");
        if (value != null) {
            Editor edit = this.mPrefs.edit();
            edit.putBoolean(Config.SP_EXPORTED, Boolean.valueOf(value).booleanValue());
            edit.apply();
            if (Boolean.valueOf(value).booleanValue()) {
                Util.showNotification(this.mContext, "Export all activities");
            }
        }
        return "#exported#";
    }

    private String fileHtml(Map<String, String> parms) {
        String value = (String) parms.get("value");
        String c = (String) parms.get("count");
        if (c == null || c.equals("")) {
            c = "0";
        }
        int count = Integer.valueOf(c).intValue();
        if (value == null || value.trim().equals("")) {
            return "";
        }
        return hooksContent(value, count);
    }

    private Response startComponent(Map<String, String> parms) {
        String component = (String) parms.get("component");
        if (component.equals("activity")) {
            startActivity((String) parms.get("activity"), (String) parms.get("action"), (String) parms.get("category"), (String) parms.get("datauri"), (String) parms.get("extra"), (String) parms.get("flags"), (String) parms.get("mimetype"));
        } else if (!(component.equals("service") || component.equals("broadcast") || !component.equals("provider"))) {
            return queryProvider((String) parms.get("uri"));
        }
        return ok("");
    }

    private Response setArp(Map<String, String> parms) {
        String ip = (String) parms.get("ip");
        String mac = (String) parms.get("mac");
        Util.setARPEntry(ip, mac);
        Util.showNotification(this.mContext, "arp -s " + ip + " " + mac + "");
        return ok("OK");
    }

    private Response addToClipboard(Map<String, String> parms) {
        String value = (String) parms.get("value");
        Intent intent = new Intent("mobi.acpm.inspeckage.INSPECKAGE_WEB");
        intent.putExtra(Config.SP_PACKAGE, this.mPrefs.getString(Config.SP_PACKAGE, ""));
        intent.putExtra("value", value);
        intent.putExtra("action", "clipboard");
        this.mContext.sendBroadcast(intent, null);
        return ok("OK");
    }

    private Response downloadFile(Map<String, String> parms) {
        return downloadFileRoot((String) parms.get("value"));
    }

    private Response checkApp() {
        String isRunning = "App is running: true";
        if (!this.mPrefs.getBoolean(Config.SP_APP_IS_RUNNING, false)) {
            isRunning = "App is running: <font style=\"color:red; background:yellow;\">false</font>";
        }
        return ok(isRunning);
    }

    private Response fileTreeHtml() {
        String tree = this.mPrefs.getString(Config.SP_DATA_DIR_TREE, "");
        if (tree.equals("")) {
            tree = "<p class=\"text-danger\">The app is running?</p>";
        }
        return ok(tree);
    }

    private Response startWS(Map<String, String> parms) {
        String selected = (String) parms.get("selected");
        Intent i = new Intent(this.mContext, LogService.class);
        i.putExtra("filter", selected);
        i.putExtra(Config.SP_PROXY_PORT, this.mPrefs.getInt(Config.SP_WSOCKET_PORT, 8887));
        this.mContext.startService(i);
        return ok("OK");
    }

    private Response stopWS() {
        this.mContext.stopService(new Intent(this.mContext, LogService.class));
        return ok("OK");
    }

    private Response addUserHooks(Map<String, String> parms) {
        String json = (String) parms.get("jhooks");
        Editor edit = this.mPrefs.edit();
        edit.putString(Config.SP_USER_HOOKS, json);
        edit.apply();
        return ok("OK");
    }

    private Response addUserReplaces(Map<String, String> parms) {
        String json = (String) parms.get("data");
        Editor edit = this.mPrefs.edit();
        edit.putString(Config.SP_USER_REPLACES, json);
        edit.apply();
        return ok("OK");
    }

    private Response addUserReturnReplaces(Map<String, String> parms) {
        String json = (String) parms.get("data");
        Editor edit = this.mPrefs.edit();
        edit.putString(Config.SP_USER_RETURN_REPLACES, json);
        edit.apply();
        return ok("OK");
    }

    private Response getUserHooks() {
        return ok("text/json", this.mPrefs.getString(Config.SP_USER_HOOKS, ""));
    }

    private Response getUserReplaces() {
        return ok("text/json", this.mPrefs.getString(Config.SP_USER_REPLACES, ""));
    }

    private Response getUserReturnReplaces() {
        return ok("text/json", this.mPrefs.getString(Config.SP_USER_RETURN_REPLACES, ""));
    }

    private Response getBuild() {
        if (this.mPrefs.getString(Config.SP_FINGERPRINT_HOOKS, "").equals("")) {
            Fingerprint.getInstance(this.mContext);
            Fingerprint.load();
        }
        return ok("text/json", this.mPrefs.getString(Config.SP_FINGERPRINT_HOOKS, "").replace("{\"fingerprintItems\":[{", "[{").replace("\"}]}", "\"}]"));
    }

    private Response resetFingerprint() {
        Fingerprint.getInstance(this.mContext);
        Fingerprint.load();
        return ok("text/json", this.mPrefs.getString(Config.SP_FINGERPRINT_HOOKS, "").replace("{\"fingerprintItems\":[{", "[{").replace("\"}]}", "\"}]"));
    }

    private Response addBuild(Map<String, String> parms) {
        String json = "{\"fingerprintItems\":" + ((String) parms.get("build")) + "}";
        Editor edit = this.mPrefs.edit();
        edit.putString(Config.SP_FINGERPRINT_HOOKS, json);
        edit.apply();
        return ok("OK");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private fi.iki.elonen.NanoHTTPD.Response clearHooksLog(java.util.Map<java.lang.String, java.lang.String> r9) {
        /*
        r8 = this;
        r4 = 0;
        r5 = "value";
        r1 = r9.get(r5);
        r1 = (java.lang.String) r1;
        r5 = android.os.Environment.getExternalStorageDirectory();
        r0 = r5.getAbsolutePath();
        r5 = r8.mPrefs;
        r6 = "write_permission";
        r5 = r5.getBoolean(r6, r4);
        if (r5 != 0) goto L_0x0025;
    L_0x001b:
        r5 = r8.mPrefs;
        r6 = "path";
        r7 = "";
        r0 = r5.getString(r6, r7);
    L_0x0025:
        r2 = "";
        r5 = -1;
        r6 = r1.hashCode();
        switch(r6) {
            case -1351683903: goto L_0x00a8;
            case -894935028: goto L_0x0094;
            case 3277: goto L_0x0080;
            case 104476: goto L_0x008a;
            case 3195150: goto L_0x009e;
            case 3213448: goto L_0x0076;
            case 3351788: goto L_0x0062;
            case 106930864: goto L_0x00c0;
            case 347695813: goto L_0x0059;
            case 922807280: goto L_0x00b4;
            case 1224424441: goto L_0x006c;
            default: goto L_0x002f;
        };
    L_0x002f:
        r4 = r5;
    L_0x0030:
        switch(r4) {
            case 0: goto L_0x00cc;
            case 1: goto L_0x00d0;
            case 2: goto L_0x00d4;
            case 3: goto L_0x00d8;
            case 4: goto L_0x00dc;
            case 5: goto L_0x00e0;
            case 6: goto L_0x00e4;
            case 7: goto L_0x00e8;
            case 8: goto L_0x00ec;
            case 9: goto L_0x00f0;
            case 10: goto L_0x00f4;
            default: goto L_0x0033;
        };
    L_0x0033:
        r3 = new java.io.File;
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r0);
        r5 = "/Inspeckage";
        r4 = r4.append(r5);
        r4 = r4.append(r2);
        r4 = r4.toString();
        r3.<init>(r4);
        mobi.acpm.inspeckage.util.FileUtil.deleteFile(r3);
        r4 = "ok";
        r4 = r8.ok(r4);
        return r4;
    L_0x0059:
        r6 = "userhooks";
        r6 = r1.equals(r6);
        if (r6 == 0) goto L_0x002f;
    L_0x0061:
        goto L_0x0030;
    L_0x0062:
        r4 = "misc";
        r4 = r1.equals(r4);
        if (r4 == 0) goto L_0x002f;
    L_0x006a:
        r4 = 1;
        goto L_0x0030;
    L_0x006c:
        r4 = "webview";
        r4 = r1.equals(r4);
        if (r4 == 0) goto L_0x002f;
    L_0x0074:
        r4 = 2;
        goto L_0x0030;
    L_0x0076:
        r4 = "http";
        r4 = r1.equals(r4);
        if (r4 == 0) goto L_0x002f;
    L_0x007e:
        r4 = 3;
        goto L_0x0030;
    L_0x0080:
        r4 = "fs";
        r4 = r1.equals(r4);
        if (r4 == 0) goto L_0x002f;
    L_0x0088:
        r4 = 4;
        goto L_0x0030;
    L_0x008a:
        r4 = "ipc";
        r4 = r1.equals(r4);
        if (r4 == 0) goto L_0x002f;
    L_0x0092:
        r4 = 5;
        goto L_0x0030;
    L_0x0094:
        r4 = "sqlite";
        r4 = r1.equals(r4);
        if (r4 == 0) goto L_0x002f;
    L_0x009c:
        r4 = 6;
        goto L_0x0030;
    L_0x009e:
        r4 = "hash";
        r4 = r1.equals(r4);
        if (r4 == 0) goto L_0x002f;
    L_0x00a6:
        r4 = 7;
        goto L_0x0030;
    L_0x00a8:
        r4 = "crypto";
        r4 = r1.equals(r4);
        if (r4 == 0) goto L_0x002f;
    L_0x00b0:
        r4 = 8;
        goto L_0x0030;
    L_0x00b4:
        r4 = "serialization";
        r4 = r1.equals(r4);
        if (r4 == 0) goto L_0x002f;
    L_0x00bc:
        r4 = 9;
        goto L_0x0030;
    L_0x00c0:
        r4 = "prefs";
        r4 = r1.equals(r4);
        if (r4 == 0) goto L_0x002f;
    L_0x00c8:
        r4 = 10;
        goto L_0x0030;
    L_0x00cc:
        r2 = "/user_hooks";
        goto L_0x0033;
    L_0x00d0:
        r2 = "/miscellaneous";
        goto L_0x0033;
    L_0x00d4:
        r2 = "/webview";
        goto L_0x0033;
    L_0x00d8:
        r2 = "/http";
        goto L_0x0033;
    L_0x00dc:
        r2 = "/filesystem";
        goto L_0x0033;
    L_0x00e0:
        r2 = "/ipc";
        goto L_0x0033;
    L_0x00e4:
        r2 = "/sqlite";
        goto L_0x0033;
    L_0x00e8:
        r2 = "/hash";
        goto L_0x0033;
    L_0x00ec:
        r2 = "/crypto";
        goto L_0x0033;
    L_0x00f0:
        r2 = "/serialization";
        goto L_0x0033;
    L_0x00f4:
        r2 = "/prefs";
        goto L_0x0033;
        */
        throw new UnsupportedOperationException("Method not decompiled: mobi.acpm.inspeckage.webserver.WebServer.clearHooksLog(java.util.Map):fi.iki.elonen.NanoHTTPD$Response");
    }

    private String replaceHtmlVariables(String html) {
        return html.replace("#proxy#", htmlProxy()).replace("#flags#", flagSecureCheckbox()).replace("#sslunpinning#", SSLUnpinningCheckbox()).replace("#exported#", exportedCheckbox()).replace("#tab_scheckbox#", tabsCheckbox()).replace("#exported_act#", htmlExportedActivities()).replace("#activities_list#", htmlActivityList()).replace("#exported_provider#", htmlExportedProviders()).replace("#non_exported_provider#", htmlNonExportedProviders()).replace("#exported_services#", htmlExportedServices()).replace("#exported_broadcast#", htmlExportedBroadcasts()).replace("#appName#", this.mPrefs.getString(Config.SP_APP_NAME, "AppName")).replace("#appIcon#", "<img src=\"data:image/png;base64, " + this.mPrefs.getString(Config.SP_APP_ICON_BASE64, "AppIcon") + "\" width=\"80\" height=\"80\" />").replace("#appVersion#", this.mPrefs.getString(Config.SP_APP_VERSION, "Version")).replace("#uid#", this.mPrefs.getString(Config.SP_UID, Config.SP_UID)).replace("#gids#", this.mPrefs.getString(Config.SP_GIDS, "GIDs")).replace("#package#", this.mPrefs.getString(Config.SP_PACKAGE, Config.SP_PACKAGE)).replace("#data_dir#", this.mPrefs.getString(Config.SP_DATA_DIR, "Data Path")).replace("#isdebuggable#", this.mPrefs.getString(Config.SP_DEBUGGABLE, "?")).replace("#allowbackup#", this.mPrefs.getString(Config.SP_ALLOW_BACKUP, "?")).replace("#non_exported_act#", this.mPrefs.getString(Config.SP_N_EXP_ACTIVITIES, "Non Exported Activities").replace("\n", "</br>")).replace("#non_exported_services#", this.mPrefs.getString(Config.SP_N_EXP_SERVICES, "Services").replace("\n", "</br>")).replace("#non_exported_broadcast#", this.mPrefs.getString(Config.SP_N_EXP_BROADCAST, "Broadcast Receiver").replace("\n", "</br>")).replace("#req_permissions#", this.mPrefs.getString(Config.SP_REQ_PERMISSIONS, "Permissions").replace("\n", "</br>")).replace("#app_permissions#", this.mPrefs.getString(Config.SP_APP_PERMISSIONS, "Permissions").replace("\n", "</br>")).replace("#shared_libraries#", this.mPrefs.getString(Config.SP_SHARED_LIB, "Shared Libraries").replace("\n", "</br>"));
    }

    private Response addLocation(Map<String, String> parms) {
        String loc = (String) parms.get("geolocation");
        Editor edit = this.mPrefs.edit();
        edit.putString(Config.SP_GEOLOCATION, loc);
        edit.apply();
        return ok("OK");
    }

    private Response getLocation() {
        return ok(this.mPrefs.getString(Config.SP_GEOLOCATION, ""));
    }

    private Response geoLocSwitch(Map<String, String> parms) {
        String geo_switch = (String) parms.get("geolocationSwitch");
        if (geo_switch != null) {
            Editor edit = this.mPrefs.edit();
            edit.putBoolean(Config.SP_GEOLOCATION_SW, Boolean.valueOf(geo_switch).booleanValue());
            edit.apply();
            if (Boolean.valueOf(geo_switch).booleanValue()) {
                Util.showNotification(this.mContext, "Geolocation ON");
            }
        }
        return ok("OK");
    }

    public String htmlNonExportedProviders() {
        String[] providers = this.mPrefs.getString(Config.SP_N_EXP_PROVIDER, "Non Exported Providers").split("\n");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String provider : providers) {
            if (!provider.trim().equals("")) {
                if (provider.contains("GRANT:")) {
                    String[] actInfo = provider.split("GRANT:");
                    String info = "<a data-toggle=\"collapse\" href=\"#collapsenprovider" + i + "\" aria-expanded=\"false\" aria-controls=\"collapsenprovider" + i + "\">" + actInfo[0] + "</a><div class=\"collapse\" id=\"collapsenprovider" + i + "\"><div class=\"well\">Grant URI Permission: " + actInfo[1].replace("|", "</br>") + "</div></div>";
                    i++;
                    sb.append(info + "</br>");
                } else {
                    sb.append(provider + "</br>");
                }
            }
        }
        return sb.toString();
    }

    public String htmlExportedProviders() {
        String[] providers = this.mPrefs.getString(Config.SP_EXP_PROVIDER, "Exported Providers").split("\n");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String provider : providers) {
            if (!provider.trim().equals("")) {
                if (provider.contains("GRANT:")) {
                    String[] actInfo = provider.split("GRANT:");
                    String info = "<a data-toggle=\"collapse\" href=\"#collapseprovider" + i + "\" aria-expanded=\"false\" aria-controls=\"collapseprovider" + i + "\">" + actInfo[0] + "</a><div class=\"collapse\" id=\"collapseprovider" + i + "\"><div class=\"well\">Grant URI Permission: " + actInfo[1].replace("|", "</br>") + "</div></div>";
                    i++;
                    sb.append(info + "</br>");
                } else {
                    sb.append(provider + "</br>");
                }
            }
        }
        return sb.toString();
    }

    public String htmlExportedBroadcasts() {
        String[] broadcasts = this.mPrefs.getString(Config.SP_EXP_BROADCAST, "Exported Broadcasts").split("\n");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String broadcast : broadcasts) {
            if (!broadcast.trim().equals("")) {
                if (broadcast.contains("PERM:")) {
                    String[] actInfo = broadcast.split("PERM:");
                    i++;
                    sb.append("<a data-toggle=\"collapse\" href=\"#collapsebroadcast" + i + "\" aria-expanded=\"false\" aria-controls=\"collapsebroadcast" + i + "\">" + actInfo[0] + "</a><div class=\"collapse\" id=\"collapsebroadcast" + i + "\"><div class=\"well\">PERMISSION: " + actInfo[1] + "</div></div></br>");
                } else {
                    sb.append(broadcast + "</br>");
                }
            }
        }
        return sb.toString();
    }

    public String htmlExportedServices() {
        String[] services = this.mPrefs.getString(Config.SP_EXP_SERVICES, "Exported Services").split("\n");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String service : services) {
            if (!service.trim().equals("")) {
                if (service.contains("PERM:")) {
                    String[] actInfo = service.split("PERM:");
                    i++;
                    sb.append("<a data-toggle=\"collapse\" href=\"#collapseservice" + i + "\" aria-expanded=\"false\" aria-controls=\"collapseservice" + i + "\">" + actInfo[0] + "</a><div class=\"collapse\" id=\"collapseservice" + i + "\"><div class=\"well\">PERMISSION: " + actInfo[1] + "</div></div></br>");
                } else {
                    sb.append(service + "</br>");
                }
            }
        }
        return sb.toString();
    }

    public String htmlExportedActivities() {
        String[] activities = this.mPrefs.getString(Config.SP_EXP_ACTIVITIES, "Exported Activities").split("\n");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String activity : activities) {
            if (!activity.trim().equals("")) {
                if (activity.contains("PERM:")) {
                    String[] actInfo = activity.split("PERM:");
                    String info = "<a data-toggle=\"collapse\" href=\"#collapseActivity" + i + "\" aria-expanded=\"false\" aria-controls=\"collapseActivity" + i + "\">" + actInfo[0] + "</a><div class=\"collapse\" id=\"collapseActivity" + i + "\"><div class=\"well\">PERMISSION: " + actInfo[1] + "</div></div>";
                    i++;
                    sb.append(info + "</br>");
                } else {
                    sb.append(activity + "</br>");
                }
            }
        }
        return sb.toString();
    }

    public String htmlActivityList() {
        String[] activities = this.mPrefs.getString(Config.SP_EXP_ACTIVITIES, "Exported Activities").split("\n");
        StringBuilder sb = new StringBuilder();
        for (String activity : activities) {
            if (!activity.trim().equals("")) {
                if (activity.contains("PERM:")) {
                    String[] actInfo = activity.split("PERM:");
                    sb.append("<li><a href=\"#\" onclick=\"selectAct('" + actInfo[0].trim() + "');\">" + actInfo[0].trim() + "</a></li>");
                } else {
                    sb.append("<li><a href=\"#\" onclick=\"selectAct('" + activity + "');\">" + activity + "</a></li>");
                }
            }
        }
        String[] nactivities = this.mPrefs.getString(Config.SP_N_EXP_ACTIVITIES, "N Exported Activities").split("\n");
        if (nactivities.length > 0) {
            sb.append("<li role='separator' class='divider'></li>");
        }
        String disabled = "";
        if (!this.mPrefs.getBoolean(Config.SP_APP_IS_RUNNING, false)) {
            disabled = "class='disabled'";
        }
        for (String activity2 : nactivities) {
            if (!activity2.trim().equals("")) {
                if (activity2.contains("PERM:")) {
                    actInfo = activity2.split("PERM:");
                    sb.append("<li " + disabled + "><a href=\"#\" onclick=\"selectAct('" + actInfo[0].trim() + "');\">N " + actInfo[0].trim() + "</a></li>");
                } else {
                    sb.append("<li " + disabled + "><a href=\"#\" onclick=\"selectAct('" + activity2 + "');\">N " + activity2 + "</a></li>");
                }
            }
        }
        return sb.toString();
    }

    public String htmlProxy() {
        String host = this.mPrefs.getString(Config.SP_PROXY_HOST, "");
        String port = this.mPrefs.getString(Config.SP_PROXY_PORT, "");
        String flag_s = "<input type='checkbox' name='switch_proxy' data-size='mini' unchecked>";
        if (Boolean.valueOf(this.mPrefs.getBoolean(Config.SP_SWITCH_PROXY, false)).booleanValue()) {
            flag_s = "<input type='checkbox' name='switch_proxy' data-size='mini' checked>";
        }
        return "<input type='text' class='form-control input-sm' id='host' value='" + host + "' placeholder='192.168.1.337'><input type='text' class='form-control input-sm' id='port' value='" + port + "' placeholder='8081'>" + flag_s;
    }

    public String htmlPrefsAccordion() {
        String prefs_files = "";
        int i = 0;
        for (Entry<String, String> e : FileUtil.readMultiFile(this.mPrefs, Config.PREFS_BKP).entrySet()) {
            String v = (String) e.getValue();
            i++;
            prefs_files = prefs_files + "<div class='panel panel-default'><div class='panel-heading' role='tab' id='heading" + i + "'><h4 class='panel-title'><a role='button' data-toggle='collapse' data-parent='#accordion' href='#collapse" + i + "' aria-expanded='true' aria-controls='collapse" + i + "'> " + ((String) e.getKey()) + " </a> </h4> </div> <div id='collapse" + i + "' class='panel-collapse collapse in' role='tabpanel' aria-labelledby='heading" + i + "'> <div class='panel-body'><textarea rows='" + countLines(v) + "' style=\"border:none;width:100%\" readonly>" + v + "</textarea></div> </div> </div>";
        }
        return prefs_files;
    }

    private static int countLines(String str) {
        return str.split("\r\n|\r|\n").length + 1;
    }

    public String flagSecureCheckbox() {
        String flag_s = "<input type='checkbox' name='flag_sec' data-size='mini' unchecked>";
        if (this.mPrefs.getBoolean(Config.SP_FLAG_SECURE, false)) {
            return "<input type='checkbox' name='flag_sec' data-size='mini' checked>";
        }
        return flag_s;
    }

    public String SSLUnpinningCheckbox() {
        String flag_s = "<input type='checkbox' name='ssl_uncheck' data-size='mini' unchecked>";
        if (this.mPrefs.getBoolean(Config.SP_UNPINNING, false)) {
            return "<input type='checkbox' name='ssl_uncheck' data-size='mini' checked>";
        }
        return flag_s;
    }

    public String exportedCheckbox() {
        String flag_s = "<input type='checkbox' name='exported' data-size='mini' unchecked>";
        if (this.mPrefs.getBoolean(Config.SP_EXPORTED, false)) {
            return "<input type='checkbox' name='exported' data-size='mini' checked>";
        }
        return flag_s;
    }

    public String tabsCheckbox() {
        String shared = "<input type='checkbox' name='shared' data-size='mini' checked> Shared Preferences</br>";
        String serialization = "<input type='checkbox' name='serialization' data-size='mini' checked> Serialization</br>";
        String crypto = "<input type='checkbox' name='crypto' data-size='mini' checked> Crypto</br>";
        String hash = "<input type='checkbox' name='hash' data-size='mini' checked> Hash</br>";
        String sqlite = "<input type='checkbox' name='sqlite' data-size='mini' checked> SQLite</br>";
        String http = "<input type='checkbox' name='http' data-size='mini' checked> HTTP</br>";
        String filesystem = "<input type='checkbox' name='filesystem' data-size='mini' checked> File System</br>";
        String misc = "<input type='checkbox' name='misc' data-size='mini' checked> Misc.</br>";
        String webview = "<input type='checkbox' name='webview' data-size='mini' checked> WebView</br>";
        String ipc = "<input type='checkbox' name='ipc' data-size='mini' checked> IPC</br>";
        String phooks = "<input type='checkbox' name='phooks' data-size='mini' checked> + Hooks</br>";
        StringBuilder sb = new StringBuilder();
        if (!this.mPrefs.getBoolean(Config.SP_TAB_ENABLE_SHAREDP, true)) {
            shared = "<input type='checkbox' name='shared' data-size='mini' unchecked>  Shared Preferences</br>";
        }
        if (!this.mPrefs.getBoolean(Config.SP_TAB_ENABLE_SERIALIZATION, true)) {
            serialization = "<input type='checkbox' name='serialization' data-size='mini' unchecked> Serialization</br>";
        }
        if (!this.mPrefs.getBoolean(Config.SP_TAB_ENABLE_CRYPTO, true)) {
            crypto = "<input type='checkbox' name='crypto' data-size='mini' unchecked> Crypto</br>";
        }
        if (!this.mPrefs.getBoolean(Config.SP_TAB_ENABLE_HASH, true)) {
            hash = "<input type='checkbox' name='hash' data-size='mini' unchecked> Hash</br>";
        }
        if (!this.mPrefs.getBoolean(Config.SP_TAB_ENABLE_SQLITE, true)) {
            sqlite = "<input type='checkbox' name='sqlite' data-size='mini' unchecked> SQLite</br>";
        }
        if (!this.mPrefs.getBoolean(Config.SP_TAB_ENABLE_HTTP, true)) {
            http = "<input type='checkbox' name='http' data-size='mini' unchecked> HTTP</br>";
        }
        if (!this.mPrefs.getBoolean(Config.SP_TAB_ENABLE_FS, true)) {
            filesystem = "<input type='checkbox' name='filesystem' data-size='mini' unchecked> File System</br>";
        }
        if (!this.mPrefs.getBoolean(Config.SP_TAB_ENABLE_MISC, true)) {
            misc = "<input type='checkbox' name='misc' data-size='mini' unchecked> Misc.</br>";
        }
        if (!this.mPrefs.getBoolean(Config.SP_TAB_ENABLE_WV, true)) {
            webview = "<input type='checkbox' name='webview' data-size='mini' unchecked> WebView</br>";
        }
        if (!this.mPrefs.getBoolean(Config.SP_TAB_ENABLE_IPC, true)) {
            ipc = "<input type='checkbox' name='ipc' data-size='mini' unchecked> IPC</br>";
        }
        if (!this.mPrefs.getBoolean(Config.SP_TAB_ENABLE_PHOOKS, true)) {
            phooks = "<input type='checkbox' name='phooks' data-size='mini' unchecked> + Hooks</br>";
        }
        return sb.append("<div class=\"col-md-6\" style=\"line-height:200%;\">").append(shared).append(serialization).append(crypto).append(hash).append(sqlite).append(http).append("</div><div class=\"col-md-6\" style=\"line-height:200%;\">").append(filesystem).append(misc).append(webview).append(ipc).append(phooks).append("</div>").toString();
    }

    public Response takeScreenshot() {
        String fileName = String.valueOf((int) Calendar.getInstance().getTimeInMillis()) + ".png";
        Util.takeScreenshot(fileName);
        String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath() + Config.P_ROOT + "/" + fileName;
        try {
            FileInputStream f = new FileInputStream(absolutePath);
            File file = new File(absolutePath);
            Response res = NanoHTTPD.newChunkedResponse(Status.OK, "image/png", f);
            res.addHeader("Content-Disposition", "attachment;filename=" + fileName);
            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Response downloadFileRoot(String path) {
        String filename = path.substring(path.lastIndexOf("/") + 1);
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (new File(sdcardPath + Config.P_ROOT).exists() && new File("/storage/emulated/legacy").exists()) {
            sdcardPath = "/storage/emulated/legacy";
        }
        String absolutePath = sdcardPath + Config.P_ROOT + "/" + filename;
        Util.copyFileRoot(path, absolutePath);
        try {
            FileInputStream f = new FileInputStream(absolutePath);
            File file = new File(absolutePath);
            Response res = NanoHTTPD.newChunkedResponse(Status.OK, "application/octet-stream", f);
            res.addHeader("Content-Disposition", "attachment;filename=" + filename);
            file.delete();
            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Response downloadAll() {
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            String fullZipPath = sdcardPath + "/" + Config.P_ROOT + "/" + (this.mPrefs.getString(Config.SP_PACKAGE, "") + "-" + String.valueOf((int) Calendar.getInstance().getTimeInMillis()) + ".zip");
            String fullPath = this.mPrefs.getString(Config.SP_DATA_DIR, "") + Config.P_ROOT;
            if (this.mPrefs.getBoolean(Config.SP_HAS_W_PERMISSION, false)) {
                fullPath = sdcardPath + "/" + Config.P_ROOT + "/" + this.mPrefs.getString(Config.SP_PACKAGE, "");
            }
            FileUtil.zipFolder(fullPath, fullZipPath);
            FileInputStream f = new FileInputStream(fullZipPath);
            File file = new File(fullZipPath);
            Response res = NanoHTTPD.newChunkedResponse(Status.OK, "application/zip", f);
            res.addHeader("Content-Disposition", "attachment;filename=" + file.getName());
            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Response downloadApk() {
        String absolutePath = this.mPrefs.getString(Config.SP_APK_DIR, "");
        try {
            FileInputStream f = new FileInputStream(absolutePath);
            File file = new File(absolutePath);
            Response res = NanoHTTPD.newChunkedResponse(Status.OK, "application/vnd.android.package-archive", f);
            res.addHeader("Content-Disposition", "attachment;filename=" + (this.mPrefs.getString(Config.SP_PACKAGE, "") + ".apk"));
            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void startApp() {
        this.mContext.startActivity(new PackageDetail(this.mContext, this.mPrefs.getString(Config.SP_PACKAGE, "")).getLaunchIntent());
    }

    public void finishApp() {
        Intent intent = new Intent("mobi.acpm.inspeckage.INSPECKAGE_FILTER");
        intent.putExtra(Config.SP_PACKAGE, this.mPrefs.getString(Config.SP_PACKAGE, ""));
        intent.putExtra("action", "finish");
        this.mContext.sendBroadcast(intent, null);
    }

    public void fileTree() {
        Intent intent = new Intent("mobi.acpm.inspeckage.INSPECKAGE_FILTER");
        intent.putExtra(Config.SP_PACKAGE, this.mPrefs.getString(Config.SP_PACKAGE, ""));
        intent.putExtra("action", "fileTree");
        this.mContext.sendBroadcast(intent, null);
    }

    public void isRunning() {
        Intent intent = new Intent("mobi.acpm.inspeckage.INSPECKAGE_FILTER");
        intent.putExtra(Config.SP_PACKAGE, this.mPrefs.getString(Config.SP_PACKAGE, ""));
        intent.putExtra("action", "checkApp");
        this.mContext.sendBroadcast(intent, null);
    }

    public void startActivity(String activity, String action, String category, String data_uri, String extra, String flags, String mimetype) {
        Intent intent = new Intent("mobi.acpm.inspeckage.INSPECKAGE_FILTER");
        intent.putExtra(Config.SP_PACKAGE, this.mPrefs.getString(Config.SP_PACKAGE, ""));
        intent.putExtra("action", "startAct");
        intent.putExtra("activity", activity);
        intent.putExtra("intent_action", action);
        intent.putExtra("data_uri", data_uri);
        intent.putExtra("extra", extra);
        intent.putExtra("flags", flags);
        intent.putExtra("mimetype", mimetype);
        intent.putExtra("category", category);
        if (this.mPrefs.getBoolean(Config.SP_APP_IS_RUNNING, false)) {
            this.mContext.sendBroadcast(intent, null);
            return;
        }
        Intent i = new Intent();
        i.setClassName(this.mPrefs.getString(Config.SP_PACKAGE, ""), activity);
        if (flags.trim().equals("")) {
            i.addFlags(DriveFile.MODE_READ_ONLY);
        } else {
            for (Field f : Intent.class.getFields()) {
                try {
                    Object value = f.get(i);
                    if (flags.trim().contains(f.getName())) {
                        i.addFlags(((Integer) value).intValue());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!data_uri.trim().equals("")) {
            i.setData(Uri.parse(data_uri));
        }
        if (!category.trim().equals("")) {
            i.addCategory(category);
        }
        if (!mimetype.trim().equals("")) {
            Intent.normalizeMimeType(mimetype);
        }
        if (!extra.trim().equals("")) {
            String[] extras = new String[]{extra};
            if (extra.contains(";")) {
                extras = extra.split(";");
            }
            for (String e2 : extras) {
                String[] values = e2.split(",");
                if (values.length == 3) {
                    if (values[0].trim().toLowerCase().equals("string")) {
                        i.putExtra(values[1], values[2]);
                    }
                    if (values[0].trim().toLowerCase().equals("boolean")) {
                        i.putExtra(values[1], Boolean.valueOf(values[2]));
                    }
                    if (values[0].trim().toLowerCase().equals("int")) {
                        i.putExtra(values[1], Integer.valueOf(values[2]));
                    }
                    if (values[0].trim().toLowerCase().equals("float")) {
                        i.putExtra(values[1], Float.valueOf(values[2]));
                    }
                    if (values[0].trim().toLowerCase().equals("double")) {
                        i.putExtra(values[1], Double.valueOf(values[2]));
                    }
                }
            }
        }
        this.mContext.startActivity(i);
    }

    public Response queryProvider(String uri) {
        Intent intent = new Intent("mobi.acpm.inspeckage.INSPECKAGE_FILTER");
        intent.putExtra(Config.SP_PACKAGE, this.mPrefs.getString(Config.SP_PACKAGE, ""));
        intent.putExtra("action", "query");
        intent.putExtra("uri", uri);
        this.mContext.sendBroadcast(intent, null);
        return NanoHTTPD.newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_HTML, "OK");
    }

    public String hooksContent(String type, int count) {
        String html = "";
        int countTmp = 0;
        Object obj = -1;
        switch (type.hashCode()) {
            case -1351683903:
                if (type.equals("crypto")) {
                    obj = 6;
                    break;
                }
                break;
            case -894935028:
                if (type.equals("sqlite")) {
                    obj = 9;
                    break;
                }
                break;
            case 3277:
                if (type.equals("fs")) {
                    obj = 1;
                    break;
                }
                break;
            case 3807:
                if (type.equals("wv")) {
                    obj = 4;
                    break;
                }
                break;
            case 104476:
                if (type.equals("ipc")) {
                    obj = 5;
                    break;
                }
                break;
            case 3195150:
                if (type.equals("hash")) {
                    obj = 8;
                    break;
                }
                break;
            case 3213448:
                if (type.equals("http")) {
                    obj = 3;
                    break;
                }
                break;
            case 3351788:
                if (type.equals("misc")) {
                    obj = 2;
                    break;
                }
                break;
            case 106930864:
                if (type.equals("prefs")) {
                    obj = 7;
                    break;
                }
                break;
            case 347695813:
                if (type.equals("userhooks")) {
                    obj = 10;
                    break;
                }
                break;
            case 922807280:
                if (type.equals("serialization")) {
                    obj = null;
                    break;
                }
                break;
        }
        String[] x;
        int i;
        List<String> ls;
        StringBuilder sb;
        String color;
        switch (obj) {
            case null:
                html = FileUtil.readFromFile(this.mPrefs, FileType.SERIALIZATION).replace(SerializationHook.TAG, "");
                if (!html.equals("")) {
                    x = html.split("</br>");
                    for (i = 0; i < x.length; i++) {
                        if (i + 1 > count) {
                            countTmp = i + 1;
                        } else {
                            countTmp = count;
                        }
                        if (x[i].length() > 170) {
                            x[i] = "<div class=\"collapse-group\"> <span class=\"label label-info\">" + (i + 1) + "</span>  " + x[i].substring(0, 135) + "<div class=\"collapse\"><div class=\"breakWord\">" + x[i].substring(135) + "</div></div><a class=\"a\" href=\"#\"> &raquo;</a></div>";
                        } else {
                            x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + x[i] + "</br>";
                        }
                    }
                    ls = Arrays.asList(x);
                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    sb = new StringBuilder();
                    for (i = 0; i < x.length; i++) {
                        if (i < 500) {
                            sb.append(x[i]);
                        }
                    }
                    sb.append("<script>$(document).ready(function() {$('a').on('click', function(e) {                    e.preventDefault();                    var $this = $(this);                    var $collapse = $this.closest('.collapse-group').find('.collapse');                    $collapse.collapse('toggle');                });});</script>");
                    if (count != -1) {
                        html = "" + countTmp;
                        break;
                    }
                    html = sb.toString();
                    break;
                }
                break;
            case 1:
                html = FileUtil.readFromFile(this.mPrefs, FileType.FILESYSTEM).replace(FileSystemHook.TAG, "");
                if (!html.equals("")) {
                    x = html.split("</br>");
                    for (i = 0; i < x.length; i++) {
                        x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + x[i];
                        if (i + 1 > count) {
                            countTmp = i + 1;
                        } else {
                            countTmp = count;
                        }
                    }
                    ls = Arrays.asList(x);
                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    sb = new StringBuilder();
                    for (i = 0; i < x.length; i++) {
                        if (i < 500) {
                            sb.append(x[i] + "</br>");
                        }
                    }
                    if (count != -1) {
                        html = "" + countTmp;
                        break;
                    }
                    html = sb.toString();
                    break;
                }
                break;
            case 2:
                html = FileUtil.readFromFile(this.mPrefs, FileType.MISC).replace(MiscHook.TAG, "");
                if (!html.equals("")) {
                    x = html.split("</br>");
                    for (i = 0; i < x.length; i++) {
                        if (x[i].length() > 170) {
                            x[i] = "<div class=\"breakWord\"><span class=\"label label-info\"> " + (i + 1) + "</span>  " + x[i] + "</div>";
                        } else {
                            x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</br>";
                        }
                        if (i + 1 > count) {
                            countTmp = i + 1;
                        } else {
                            countTmp = count;
                        }
                    }
                    ls = Arrays.asList(x);
                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    sb = new StringBuilder();
                    for (i = 0; i < x.length; i++) {
                        if (i < 500) {
                            sb.append(x[i]);
                        }
                    }
                    if (count != -1) {
                        html = "" + countTmp;
                        break;
                    }
                    html = sb.toString();
                    break;
                }
                break;
            case 3:
                html = FileUtil.readFromFile(this.mPrefs, FileType.HTTP).replace(HttpHook.TAG, "");
                if (!html.equals("")) {
                    x = html.split("</br>");
                    for (i = 0; i < x.length; i++) {
                        if (x[i].length() > 170) {
                            x[i] = "<div class=\"breakWord\"><span class=\"label label-info\">" + (i + 1) + "</span> " + x[i] + "</div>";
                        } else {
                            color = "label-info";
                            if (x[i].contains("Possible pinning")) {
                                color = "label-danger";
                            }
                            x[i] = "<span class=\"label " + color + "\">" + (i + 1) + "</span> " + Html.escapeHtml(x[i]) + "</br>";
                        }
                        if (i + 1 > count) {
                            countTmp = i + 1;
                        } else {
                            countTmp = count;
                        }
                    }
                    ls = Arrays.asList(x);
                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    sb = new StringBuilder();
                    for (i = 0; i < x.length; i++) {
                        if (i < 500) {
                            sb.append(x[i]);
                        }
                    }
                    if (count != -1) {
                        html = "" + countTmp;
                        break;
                    }
                    html = sb.toString();
                    break;
                }
                break;
            case 4:
                html = FileUtil.readFromFile(this.mPrefs, FileType.WEBVIEW).replace(WebViewHook.TAG, "");
                if (!html.equals("")) {
                    x = html.split("</br>");
                    for (i = 0; i < x.length; i++) {
                        if (x[i].contains("addJavascriptInterface(Object, ")) {
                            x[i] = "<a href=\"#\" role=\"button\" class=\"btn popovers\" data-toggle=\"popover\" title=\"\" data-content=\"Injects the supplied Java object into this WebView. The object is injected into the JavaScript context of the main frame, using the supplied name. This allows the Java object's methods to be accessed from JavaScript. <a href='http://developer.android.com/intl/pt-br/reference/android/webkit/WebView.html#addJavascriptInterface(java.lang.Object, java.lang.String)' target='_blank' title='link'> read more.</a>\">" + x[i] + " </a>";
                        } else {
                            x[i] = x[i];
                        }
                        if (i + 1 > count) {
                            countTmp = i + 1;
                        } else {
                            countTmp = count;
                        }
                    }
                    ls = Arrays.asList(x);
                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    sb = new StringBuilder();
                    for (String aX : x) {
                        sb.append(aX + "</br>");
                    }
                    sb.append("<script>$(document).ready(function() {$('[data-toggle=popover]').popover({html:true})});</script>");
                    if (count != -1) {
                        html = "" + countTmp;
                        break;
                    }
                    html = sb.toString();
                    break;
                }
                break;
            case 5:
                html = FileUtil.readFromFile(this.mPrefs, FileType.IPC).replace(IPCHook.TAG, "");
                if (!html.equals("")) {
                    x = html.split("</br>");
                    for (i = 0; i < x.length; i++) {
                        x[i] = "<span class=\"label label-default\">" + (i + 1) + "</span>   " + x[i];
                        if (i + 1 > count) {
                            countTmp = i + 1;
                        } else {
                            countTmp = count;
                        }
                    }
                    ls = Arrays.asList(x);
                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    sb = new StringBuilder();
                    for (i = 0; i < x.length; i++) {
                        if (i < 500) {
                            sb.append(x[i] + "</br>");
                        }
                    }
                    if (count != -1) {
                        html = "" + countTmp;
                        break;
                    }
                    html = sb.toString();
                    break;
                }
                break;
            case 6:
                html = FileUtil.readFromFile(this.mPrefs, FileType.CRYPTO).replace(CryptoHook.TAG, "");
                if (!html.equals("")) {
                    x = html.split("</br>");
                    for (i = 0; i < x.length; i++) {
                        if (i + 1 > count) {
                            countTmp = i + 1;
                        } else {
                            countTmp = count;
                        }
                        if (x[i].length() > 170) {
                            x[i] = "<div class=\"collapse-group\"> <span class=\"label label-info\">" + (i + 1) + "</span>  " + x[i].substring(0, 135) + "<div class=\"collapse\"><p class=\"breakWord\">" + x[i].substring(135) + "</p></div><a class=\"a\" href=\"#\"> &raquo;</a></div>";
                        } else {
                            x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</br>";
                        }
                    }
                    ls = Arrays.asList(x);
                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    sb = new StringBuilder();
                    for (i = 0; i < x.length; i++) {
                        if (i < 500) {
                            sb.append(x[i]);
                        }
                    }
                    sb.append("<script>$(document).ready(function() {$('a').on('click', function(e) {                    e.preventDefault();                    var $this = $(this);                    var $collapse = $this.closest('.collapse-group').find('.collapse');                    $collapse.collapse('toggle');                });});</script>");
                    if (count != -1) {
                        html = "" + countTmp;
                        break;
                    }
                    html = sb.toString();
                    break;
                }
                break;
            case 7:
                html = FileUtil.readFromFile(this.mPrefs, FileType.PREFS).replace(SharedPrefsHook.TAG, "");
                if (!html.equals("")) {
                    x = html.split("</br>");
                    for (i = 0; i < x.length; i++) {
                        color = "danger";
                        if (x[i].contains("GET[")) {
                            color = "info";
                        } else if (x[i].contains("CONTAINS[")) {
                            color = "warning";
                        } else if (x[i].contains("PUT[")) {
                            color = "danger";
                        }
                        if (x[i].length() > 170) {
                            x[i] = "<tr><td><div class=\"breakWord\"><span class=\"label label-" + color + "\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</div></td></tr>";
                        } else {
                            x[i] = "<tr><td><span class=\"label label-" + color + "\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</br></td></tr>";
                        }
                        if (i + 1 > count) {
                            countTmp = i + 1;
                        } else {
                            countTmp = count;
                        }
                    }
                    ls = Arrays.asList(x);
                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    sb = new StringBuilder();
                    String tableBefore = "<table class=\"table\"><tbody>";
                    String tableAfter = "</tbody></table>";
                    for (i = 0; i < x.length; i++) {
                        if (i < 500) {
                            sb.append(x[i]);
                        }
                    }
                    if (count != -1) {
                        html = "" + countTmp;
                        break;
                    }
                    html = sb.toString();
                    break;
                }
                break;
            case 8:
                html = FileUtil.readFromFile(this.mPrefs, FileType.HASH).replace(HashHook.TAG, "");
                if (!html.equals("")) {
                    x = html.split("</br>");
                    for (i = 0; i < x.length; i++) {
                        if (i + 1 > count) {
                            countTmp = i + 1;
                        } else {
                            countTmp = count;
                        }
                        if (x[i].length() > 170) {
                            x[i] = "<div class=\"collapse-group\"> <span class=\"label label-info\">" + (i + 1) + "</span>  " + x[i].substring(0, 135) + "<div class=\"collapse\"><p class=\"breakWord\">" + x[i].substring(135) + "</p></div><a class=\"a\" href=\"#\"> &raquo;</a></div>";
                        } else {
                            x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</br>";
                        }
                    }
                    ls = Arrays.asList(x);
                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    sb = new StringBuilder();
                    for (i = 0; i < x.length; i++) {
                        if (i < 500) {
                            sb.append(x[i]);
                        }
                    }
                    sb.append("<script>$(document).ready(function() {$('a').on('click', function(e) {                    e.preventDefault();                    var $this = $(this);                    var $collapse = $this.closest('.collapse-group').find('.collapse');                    $collapse.collapse('toggle');                });});</script>");
                    if (count != -1) {
                        html = "" + countTmp;
                        break;
                    }
                    html = sb.toString();
                    break;
                }
                break;
            case 9:
                html = FileUtil.readFromFile(this.mPrefs, FileType.SQLITE).replace(SQLiteHook.TAG, "");
                if (!html.equals("")) {
                    x = html.split("</br>");
                    for (i = 0; i < x.length; i++) {
                        if (x[i].contains("INSERT INTO")) {
                            color = "label-info";
                        } else if (x[i].contains("UPDATE")) {
                            color = "label-warning";
                        } else if (x[i].contains("execSQL(")) {
                            color = "label-danger";
                        } else if (x[i].contains("SELECT")) {
                            color = "label-success";
                            x[i].replace("\n", "</br>");
                        } else {
                            color = "label-default";
                        }
                        if (x[i].length() > 170) {
                            x[i] = "<div class=\"breakWord\"><span class=\"label " + color + "\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</div>";
                        } else {
                            x[i] = "<span class=\"label " + color + "\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</br>";
                        }
                        if (i + 1 > count) {
                            countTmp = i + 1;
                        } else {
                            countTmp = count;
                        }
                    }
                    ls = Arrays.asList(x);
                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    sb = new StringBuilder();
                    for (i = 0; i < x.length; i++) {
                        if (i < 500) {
                            sb.append(x[i]);
                        }
                    }
                    if (count != -1) {
                        html = "" + countTmp;
                        break;
                    }
                    html = sb.toString().replace("</br></br>", "</br>");
                    break;
                }
                break;
            case 10:
                html = FileUtil.readFromFile(this.mPrefs, FileType.USERHOOKS).replace(UserHooks.TAG, "");
                if (!html.equals("")) {
                    x = html.split("</br>");
                    for (i = 0; i < x.length; i++) {
                        if (x[i].length() > 470) {
                            x[i] = "<div class=\"collapse-group\"> <div class=\"breakWord\"><span class=\"label label-default\">" + (i + 1) + "</span>   " + x[i].substring(0, 400) + "</div><div class=\"collapse\"><p class=\"breakWord\">" + x[i].substring(400) + "</p></div><a class=\"a\" href=\"#\"> &raquo;</a></div>";
                        } else {
                            x[i] = "<span class=\"label label-default\">" + (i + 1) + "</span>   " + x[i];
                        }
                        if (i + 1 > count) {
                            countTmp = i + 1;
                        } else {
                            countTmp = count;
                        }
                    }
                    ls = Arrays.asList(x);
                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    sb = new StringBuilder();
                    for (i = 0; i < x.length; i++) {
                        if (i < 500) {
                            sb.append(x[i] + "</br>");
                        }
                    }
                    sb.append("<script>$(document).ready(function() {$('a').on('click', function(e) {                    e.preventDefault();                    var $this = $(this);                    var $collapse = $this.closest('.collapse-group').find('.collapse');                    $collapse.collapse('toggle');                });});</script>");
                    if (count != -1) {
                        html = "" + countTmp;
                        break;
                    }
                    html = sb.toString();
                    break;
                }
                break;
        }
        if (type.equals("pfiles")) {
            return htmlPrefsAccordion();
        }
        return html;
    }

    public static boolean isModuleEnabled() {
        return false;
    }
}
