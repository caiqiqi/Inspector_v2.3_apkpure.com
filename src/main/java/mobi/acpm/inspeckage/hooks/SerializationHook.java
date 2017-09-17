package mobi.acpm.inspeckage.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import mobi.acpm.inspeckage.util.Util;

public class SerializationHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_Serialization:";
    static String f = "";

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookConstructor(FileInputStream.class, new Object[]{File.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                File file = param.args[0];
                if (file != null && !file.getPath().contains("inspeckage")) {
                    if (file.getPath().contains("data/data/") || file.getPath().contains("storage/emulated/") || file.getPath().contains("data/media/")) {
                        SerializationHook.f = file.getPath();
                    }
                }
            }
        }});
        XposedHelpers.findAndHookMethod(ObjectInputStream.class, "readObject", new Object[]{new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object paramObject = param.getResult();
                StringBuilder sb = new StringBuilder();
                if (paramObject != null) {
                    String name = paramObject.getClass().getCanonicalName();
                    if (name == null) {
                        return;
                    }
                    if ((name.length() <= 5 || !name.substring(0, 5).contains("java.")) && !name.substring(0, 5).contains("byte")) {
                        sb.append("Read Object[" + name + "] HEX = ");
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try {
                            new ObjectOutputStream(bos).writeObject(paramObject);
                            sb.append(Util.toHexString(bos.toByteArray()));
                            XposedBridge.log("Inspeckage_Serialization:Possible Path [" + SerializationHook.f + "] " + sb.toString());
                        } catch (NullPointerException e) {
                        } catch (IOException i) {
                            i.printStackTrace();
                        }
                    }
                }
            }
        }});
    }
}
