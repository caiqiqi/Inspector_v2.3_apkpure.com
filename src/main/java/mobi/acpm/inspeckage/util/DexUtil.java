package mobi.acpm.inspeckage.util;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.GsonBuilder;
import dalvik.system.DexFile;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DexUtil {

    public static class ClassMethod {
        private List<ClassMethod> children = new ArrayList();
        private String icon;
        private String id;
        private String text;

        public String getID() {
            return this.id;
        }

        public void setID(String id) {
            this.id = id;
        }

        public String getName() {
            return this.text;
        }

        public void setName(String name) {
            this.text = name;
        }

        public String getIcon() {
            return this.icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public List<ClassMethod> getClassMethods() {
            return this.children;
        }

        public void setClassMethods(List<ClassMethod> children) {
            this.children = children;
        }

        public boolean contains(ClassMethod cm) {
            boolean x = false;
            for (ClassMethod c : getClassMethods()) {
                if (c.getID().equals(cm.getID())) {
                    x = true;
                }
            }
            return x;
        }

        public boolean update(ClassMethod cm) {
            boolean x = false;
            for (ClassMethod c : getClassMethods()) {
                if (c.getID().equals(cm.getID())) {
                    for (ClassMethod cm2 : cm.getClassMethods()) {
                        if (!c.contains(cm2)) {
                            c.getClassMethods().add(cm2);
                        }
                    }
                    x = true;
                }
            }
            return x;
        }
    }

    public static Map<String, ArrayList<String>> getClassesWithMethods(LoadPackageParam loadPackageParam, String packageName) throws Throwable {
        Map<String, ArrayList<String>> classes = new HashMap();
        if (!packageName.trim().equals("") && loadPackageParam.appInfo.sourceDir.contains(packageName)) {
            Enumeration<String> classNames = new DexFile(loadPackageParam.appInfo.sourceDir).entries();
            while (classNames.hasMoreElements()) {
                String className = (String) classNames.nextElement();
                boolean subMethod = false;
                if (className.contains("$") && TextUtils.isDigitsOnly(className.split("\\$")[1])) {
                    subMethod = true;
                }
                if (!(subMethod || className.contains(".R$"))) {
                    try {
                        Class cls = Class.forName(className, false, loadPackageParam.classLoader);
                        if (cls != null && cls.getDeclaredMethods().length > 0) {
                            ArrayList<String> methods = new ArrayList();
                            for (Method method : cls.getDeclaredMethods()) {
                                if (!(Modifier.isAbstract(method.getModifiers()) || methods.contains(method.getName()))) {
                                    methods.add(method.getName());
                                }
                            }
                            classes.put(className, methods);
                        }
                    } catch (NoClassDefFoundError ex) {
                        Log.e("Error", ex.getMessage());
                    } catch (ClassNotFoundException ex2) {
                        Log.e("Error", ex2.getMessage());
                    }
                }
            }
        }
        return classes;
    }

    public static void saveClassesWithMethodsJson(LoadPackageParam loadPackageParam, SharedPreferences prefs) throws Throwable {
        String packageName = prefs.getString(Config.SP_PACKAGE, "");
        Map<String, ArrayList<String>> classes = getClassesWithMethods(loadPackageParam, packageName);
        ClassMethod root = new ClassMethod();
        root.setID("p_" + packageName);
        root.setName(packageName);
        int c_id = 0;
        for (String classNameComplete : classes.keySet()) {
            if (classNameComplete.contains(packageName)) {
                c_id++;
                String pack_name = classNameComplete.substring(0, classNameComplete.lastIndexOf("."));
                String class_name = classNameComplete.substring(classNameComplete.lastIndexOf(".") + 1);
                ClassMethod package_class = new ClassMethod();
                package_class.setID(pack_name);
                package_class.setName(pack_name);
                if (!root.contains(package_class)) {
                    root.getClassMethods().add(package_class);
                }
                ClassMethod class_leaf = new ClassMethod();
                class_leaf.setID(classNameComplete);
                class_leaf.setName(class_name);
                int m_id = 0;
                Iterator it = ((ArrayList) classes.get(classNameComplete)).iterator();
                while (it.hasNext()) {
                    String method = (String) it.next();
                    m_id++;
                    ClassMethod m = new ClassMethod();
                    m.setID("m_" + c_id + "_" + m_id);
                    m.setName(method);
                    m.setIcon("jstree-file");
                    if (!class_leaf.contains(m)) {
                        class_leaf.getClassMethods().add(m);
                    }
                }
                package_class.getClassMethods().add(class_leaf);
                root.update(package_class);
            }
        }
        FileUtil.writeToFile(prefs, new GsonBuilder().create().toJsonTree(root).getAsJsonObject().toString(), FileType.APP_STRUCT, "");
    }
}
