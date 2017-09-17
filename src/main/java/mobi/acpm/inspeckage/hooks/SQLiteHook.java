package mobi.acpm.inspeckage.hooks;

import android.app.Activity;
import android.content.ContentValues;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Base64;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.io.File;
import java.util.Map.Entry;

public class SQLiteHook extends XC_MethodHook {
    public static final String TAG = "Inspeckage_SQLite:";

    public static void initAllHooks(LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(SQLiteDatabase.class, "execSQL", new Object[]{String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_SQLite:execSQL(" + param.args[0] + ")");
            }
        }});
        XposedHelpers.findAndHookMethod(SQLiteDatabase.class, "execSQL", new Object[]{String.class, Object[].class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object[] obj = (Object[]) param.args[1];
                int obj_c = 0;
                if (obj != null && obj.length > 0) {
                    obj_c = obj.length;
                }
                XposedBridge.log("Inspeckage_SQLite:execSQL(" + param.args[0] + ") with " + String.valueOf(obj_c) + " args.");
            }
        }});
        XposedHelpers.findAndHookMethod(SQLiteDatabase.class, "update", new Object[]{String.class, ContentValues.class, String.class, String[].class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                SQLiteDatabase sqlitedb = param.thisObject;
                ContentValues contentValues = param.args[1];
                StringBuffer sb = new StringBuffer();
                for (Entry<String, Object> entry : contentValues.valueSet()) {
                    sb.append(((String) entry.getKey()) + "=" + String.valueOf(entry.getValue()) + ",");
                }
                StringBuffer sbuff = new StringBuffer();
                if (param.args[3] != null) {
                    for (String str : (String[]) param.args[3]) {
                        sbuff.append(str + ",");
                    }
                }
                String set = "";
                if (sb.toString().length() > 1) {
                    set = sb.toString().substring(0, sb.length() - 1);
                }
                String whereArgs = "";
                if (sbuff.toString().length() > 1) {
                    whereArgs = sbuff.toString().substring(0, sbuff.length() - 1);
                }
                XposedBridge.log("Inspeckage_SQLite:\nUPDATE " + param.args[0] + " SET " + set + " WHERE " + param.args[2] + "" + whereArgs);
            }
        }});
        XposedHelpers.findAndHookMethod(SQLiteDatabase.class, "insert", new Object[]{String.class, String.class, ContentValues.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                SQLiteDatabase sqlitedb = param.thisObject;
                ContentValues contentValues = param.args[2];
                StringBuffer sb = new StringBuffer();
                for (Entry<String, Object> entry : contentValues.valueSet()) {
                    sb.append(((String) entry.getKey()) + "=" + String.valueOf(entry.getValue()) + ",");
                }
                XposedBridge.log("Inspeckage_SQLite:INSERT INTO " + param.args[0] + " VALUES(" + sb.toString().substring(0, sb.length() - 1) + ")");
            }
        }});
        XposedHelpers.findAndHookMethod(Activity.class, "managedQuery", new Object[]{Uri.class, String[].class, String.class, String[].class, String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Uri uri = param.args[0];
                StringBuffer projection = new StringBuffer();
                if (param.args[1] != null) {
                    for (String str : (String[]) param.args[1]) {
                        projection.append(str + ",");
                    }
                }
                String selection = "";
                if (param.args[2] != null) {
                    selection = " WHERE " + ((String) param.args[2]) + " = ";
                }
                StringBuffer selectionArgs = new StringBuffer();
                if (param.args[3] != null) {
                    for (String str2 : (String[]) param.args[3]) {
                        selectionArgs.append(str2 + ",");
                    }
                }
                String sortOrder = "";
                if (param.args[4] != null) {
                    sortOrder = " ORDER BY " + ((String) param.args[4]);
                }
                String projec = "";
                if (projection.toString().equals("")) {
                    projec = "*";
                } else {
                    projec = projection.toString().substring(0, projection.length() - 1);
                }
                Cursor cursor = (Cursor) param.getResult();
                StringBuffer result = new StringBuffer();
                if (cursor == null || !cursor.moveToFirst()) {
                    XposedBridge.log("Inspeckage_SQLite:SELECT " + projec + " FROM " + uri.getAuthority() + uri.getPath() + selection + selectionArgs.toString() + sortOrder + "\n   [" + result.toString() + "]");
                }
                do {
                    int x = cursor.getColumnCount();
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < x; i++) {
                        if (cursor.getType(i) == 4) {
                            sb.append(cursor.getColumnName(i) + "=" + Base64.encodeToString(cursor.getBlob(i), 2) + ",");
                        } else {
                            sb.append(cursor.getColumnName(i) + "=" + cursor.getString(i) + ",");
                        }
                    }
                    result.append(sb.toString().substring(0, sb.length() - 1) + "\n");
                } while (cursor.moveToNext());
                XposedBridge.log("Inspeckage_SQLite:SELECT " + projec + " FROM " + uri.getAuthority() + uri.getPath() + selection + selectionArgs.toString() + sortOrder + "\n   [" + result.toString() + "]");
            }
        }});
        XposedHelpers.findAndHookMethod(SQLiteDatabase.class, "query", new Object[]{String.class, String[].class, String.class, String[].class, String.class, String.class, String.class, String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String table = param.args[0];
                String[] columns = (String[]) param.args[1];
                String having = param.args[5];
                String limit = param.args[6];
                StringBuffer csb = new StringBuffer();
                if (param.args[1] != null) {
                    for (String str : (String[]) param.args[1]) {
                        csb.append(str + ",");
                    }
                }
                String selection = "";
                if (param.args[2] != null) {
                    selection = " WHERE " + ((String) param.args[2]) + " = ";
                }
                StringBuffer selectionArgs = new StringBuffer();
                if (param.args[3] != null) {
                    for (String str2 : (String[]) param.args[3]) {
                        selectionArgs.append(str2 + ",");
                    }
                }
                String groupBy = "";
                if (param.args[4] != null) {
                    groupBy = " GROUP BY " + ((String) param.args[4]);
                }
                String sortOrder = "";
                if (param.args[6] != null) {
                    sortOrder = " ORDER BY " + ((String) param.args[6]);
                }
                if (csb.toString().equals("")) {
                    csb.append("*");
                }
                Cursor cursor = (Cursor) param.getResult();
                StringBuffer result = new StringBuffer();
                if (cursor == null || !cursor.moveToFirst()) {
                    XposedBridge.log("Inspeckage_SQLite:SELECT " + csb.toString() + " FROM " + table + selection + selectionArgs.toString() + sortOrder + "\n" + result.toString() + "");
                }
                do {
                    int x = cursor.getColumnCount();
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < x; i++) {
                        if (cursor.getType(i) == 4) {
                            sb.append(cursor.getColumnName(i) + "=" + Base64.encodeToString(cursor.getBlob(i), 2) + ",");
                        } else {
                            sb.append(cursor.getColumnName(i) + "=" + cursor.getString(i) + ",");
                        }
                    }
                    result.append(sb.toString().substring(0, sb.length() - 1) + "\n");
                } while (cursor.moveToNext());
                XposedBridge.log("Inspeckage_SQLite:SELECT " + csb.toString() + " FROM " + table + selection + selectionArgs.toString() + sortOrder + "\n" + result.toString() + "");
            }
        }});
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "getDatabasePath", new Object[]{String.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Inspeckage_SQLite:[Context] getDatabasePath(" + param.args[0] + ")");
            }
        }});
        try {
            XposedHelpers.findAndHookMethod("net.sqlcipher.database.SQLiteDatabase", loadPackageParam.classLoader, "execSQL", new Object[]{String.class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("Inspeckage_SQLite:[SQLCipher] execSQL(" + param.args[0] + ")");
                }
            }});
            XposedHelpers.findAndHookMethod("net.sqlcipher.database.SQLiteDatabase", loadPackageParam.classLoader, "execSQL", new Object[]{String.class, Object[].class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object[] obj = (Object[]) param.args[1];
                    int obj_c = 0;
                    if (obj != null && obj.length > 0) {
                        obj_c = obj.length;
                    }
                    XposedBridge.log("Inspeckage_SQLite:[SQLCipher] execSQL(" + param.args[0] + ") with " + String.valueOf(obj_c) + " args.");
                }
            }});
            XposedHelpers.findAndHookMethod("net.sqlcipher.database.SQLiteDatabase", loadPackageParam.classLoader, "openOrCreateDatabase", new Object[]{File.class, String.class, "net.sqlcipher.database.SQLiteDatabase.CursorFactory", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String passwd = param.args[1];
                    XposedBridge.log("Inspeckage_SQLite:[SQLCipher] Open or Create:" + param.args[0].getName() + " with password: " + passwd);
                }
            }});
            XposedHelpers.findAndHookMethod("net.sqlcipher.database.SQLiteDatabase", loadPackageParam.classLoader, "rawQuery", new Object[]{String.class, String[].class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String[] obj = (String[]) param.args[1];
                    int obj_c = 0;
                    if (obj != null && obj.length > 0) {
                        obj_c = obj.length;
                    }
                    XposedBridge.log("Inspeckage_SQLite:[SQLCipher] rawQuery(" + param.args[0] + ") with " + String.valueOf(obj_c) + " args.");
                }
            }});
        } catch (ClassNotFoundError e) {
        } catch (NoSuchMethodError e2) {
        }
    }
}
