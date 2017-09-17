package mobi.acpm.inspeckage.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import mobi.acpm.inspeckage.util.Util;

public class CryptoHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_Crypto:";
    private static StringBuffer sb;

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookConstructor(SecretKeySpec.class, new Object[]{byte[].class, String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                CryptoHook.sb = new StringBuffer();
                CryptoHook.sb.append("SecretKeySpec(" + Util.byteArrayToString((byte[]) param.args[0]) + "," + ((String) param.args[1]) + ")");
            }
        }});
        XposedHelpers.findAndHookMethod(Cipher.class, "doFinal", new Object[]{byte[].class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (CryptoHook.sb == null) {
                    CryptoHook.sb = new StringBuffer();
                }
                CryptoHook.sb.append(" (" + Util.byteArrayToString((byte[]) param.args[0]) + " , ");
                CryptoHook.sb.append(Util.byteArrayToString((byte[]) param.getResult()) + ")");
                XposedBridge.log(CryptoHook.TAG + CryptoHook.sb.toString());
                CryptoHook.sb = new StringBuffer();
            }
        }});
        XposedHelpers.findAndHookMethod(Cipher.class, "getIV", new Object[]{new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (CryptoHook.sb == null) {
                    CryptoHook.sb = new StringBuffer();
                }
                CryptoHook.sb.append(" IV:" + ((String) param.getResult()));
            }
        }});
        XposedHelpers.findAndHookConstructor(IvParameterSpec.class, new Object[]{byte[].class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (CryptoHook.sb == null) {
                    CryptoHook.sb = new StringBuffer();
                }
                CryptoHook.sb.append(" IV: " + Util.byteArrayToString((byte[]) param.args[0]));
            }
        }});
        XposedHelpers.findAndHookMethod(SecureRandom.class, "setSeed", new Object[]{byte[].class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (CryptoHook.sb == null) {
                    CryptoHook.sb = new StringBuffer();
                }
                CryptoHook.sb.append(" Seed:" + Util.byteArrayToString((byte[]) param.args[0]));
            }
        }});
        XposedHelpers.findAndHookMethod(Cipher.class, "getInstance", new Object[]{String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (CryptoHook.sb == null) {
                    CryptoHook.sb = new StringBuffer();
                }
                CryptoHook.sb.append(" , Cipher[" + ((String) param.args[0]) + "] ");
            }
        }});
        XposedHelpers.findAndHookConstructor(PBEKeySpec.class, new Object[]{char[].class, byte[].class, Integer.TYPE, Integer.TYPE, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (CryptoHook.sb == null) {
                    CryptoHook.sb = new StringBuffer();
                }
                CryptoHook.sb.append("[PBEKeySpec] - Password: " + String.valueOf((char[]) param.args[0]) + " || Salt: " + Util.byteArrayToString((byte[]) param.args[1]));
                XposedBridge.log(CryptoHook.TAG + CryptoHook.sb.toString());
                CryptoHook.sb = new StringBuffer();
            }
        }});
    }
}
