package mobi.acpm.inspeckage.hooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.util.Replacement;
import mobi.acpm.inspeckage.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserHooks extends XC_MethodHook {
    public static final String TAG = "Inspeckage_UserHooks:";
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static LoadPackageParam lpp;
    static XC_MethodHook methodHook = new XC_MethodHook() {
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            UserHooks.loadPrefs();
            Replacement.parameterReplace(param, UserHooks.sPrefs);
        }

        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            UserHooks.loadPrefs();
            Replacement.resultReplace(param, UserHooks.sPrefs);
            UserHooks.parseParam(param);
        }
    };
    private static XSharedPreferences sPrefs;

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        loadPrefs();
        lpp = loadPackageParam;
        String json = "{\"hookJson\": " + sPrefs.getString(Config.SP_USER_HOOKS, "") + "}";
        try {
            if (!json.trim().equals("{\"hookJson\":}")) {
                for (HookItem hookItem : ((HookList) gson.fromJson(json, HookList.class)).hookJson) {
                    if (hookItem.state) {
                        hook(hookItem, loadPackageParam.classLoader);
                    }
                }
            }
        } catch (JsonSyntaxException e) {
        }
    }

    static void hook(HookItem item, ClassLoader classLoader) {
        int i = 0;
        try {
            Class<?> hookClass = XposedHelpers.findClass(item.className, classLoader);
            if (hookClass != null) {
                if (item.method == null || item.method.equals("")) {
                    for (Method method : hookClass.getDeclaredMethods()) {
                        if (!Modifier.isAbstract(method.getModifiers())) {
                            XposedBridge.hookMethod(method, methodHook);
                        }
                    }
                } else {
                    for (Method method2 : hookClass.getDeclaredMethods()) {
                        if (method2.getName().equals(item.method) && !Modifier.isAbstract(method2.getModifiers())) {
                            XposedBridge.hookMethod(method2, methodHook);
                        }
                    }
                }
                if (item.constructor) {
                    Constructor[] declaredConstructors = hookClass.getDeclaredConstructors();
                    int length = declaredConstructors.length;
                    while (i < length) {
                        XposedBridge.hookMethod(declaredConstructors[i], methodHook);
                        i++;
                    }
                    return;
                }
                return;
            }
            XposedBridge.log("Inspeckage_UserHooks:class not found.");
        } catch (Error e) {
            Module.logError(e);
        }
    }

    static void parseParam(MethodHookParam param) {
        try {
            JSONObject hookData = new JSONObject();
            hookData.put("class", param.method.getDeclaringClass().getName());
            if (param.method != null) {
                hookData.put("method", param.method.getName());
            }
            JSONArray args = new JSONArray();
            if (param.args != null) {
                for (Object object : param.args) {
                    if (object != null) {
                        if (object.getClass().equals(byte[].class)) {
                            args.put(gson.toJson(Util.byteArrayToString((byte[]) object)));
                        } else {
                            args.put(gson.toJson(object));
                        }
                    }
                }
                hookData.put("args", args);
            }
            if (param.getResult() != null) {
                String str = "";
                if (param.getResult().getClass().equals(byte[].class)) {
                    hookData.put("result", gson.toJson(Util.byteArrayToString((byte[]) param.getResult())));
                } else {
                    hookData.put("result", gson.toJson(param.getResult()));
                }
            }
            XposedBridge.log(TAG + hookData.toString());
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
