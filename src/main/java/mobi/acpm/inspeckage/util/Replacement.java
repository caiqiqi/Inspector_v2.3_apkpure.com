package mobi.acpm.inspeckage.util;

import com.google.gson.Gson;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XSharedPreferences;

public class Replacement {
    public static final String TAG = "Inspeckage_Replacement:";
    private static Gson gson = new Gson();

    public static boolean parameterReplace(MethodHookParam param, XSharedPreferences sPrefs) {
        if (!sPrefs.getString(Config.SP_USER_REPLACES, "").trim().equals("")) {
            for (ReplaceParamItem item : ((ReplaceParamList) gson.fromJson("{\"replaceParamItems\": " + sPrefs.getString(Config.SP_USER_REPLACES, "") + "}", ReplaceParamList.class)).replaceParamItems) {
                if (item.position > 0 && item.state) {
                    int p;
                    String v;
                    if (item.classMethod.equalsIgnoreCase(param.method.getDeclaringClass().getName() + "." + param.method.getName())) {
                        p = item.position - 1;
                        if (param.args[p] != null) {
                            if (item.paramType.equals("boolean")) {
                                if (param.args[p] instanceof Boolean) {
                                    param.args[p] = Boolean.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("String") && (param.args[p] instanceof String)) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = item.paramNewValue;
                                } else if (item.paramMatch.equals(param.args[p])) {
                                    param.args[p] = item.paramNewValue;
                                }
                            } else if (item.paramType.equals("int") && (param.args[p] instanceof Integer)) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = Integer.valueOf(item.paramNewValue.toString());
                                } else if (Integer.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                    param.args[p] = Integer.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("ByteArray") && param.args[p].getClass().equals(byte[].class)) {
                                v = Util.byteArrayToString((byte[]) param.args[p]);
                                if (item.paramMatch == null || item.paramMatch.toString().trim().equals("")) {
                                    param.args[p] = item.paramNewValue.toString().getBytes();
                                } else if (v.equals(item.paramMatch.toString())) {
                                    param.args[p] = v.getBytes();
                                }
                            } else if (item.paramType.equals("short") && (param.args[p] instanceof Short)) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = Short.valueOf(item.paramNewValue.toString());
                                } else if (Short.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                    param.args[p] = Short.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("long") && (param.args[p] instanceof Long)) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = Long.valueOf(item.paramNewValue.toString());
                                } else if (Long.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                    param.args[p] = Long.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("float") && (param.args[p] instanceof Float)) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = Float.valueOf(item.paramNewValue.toString());
                                } else if (Float.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                    param.args[p] = Float.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("double") && (param.args[p] instanceof Double)) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = Double.valueOf(item.paramNewValue.toString());
                                } else if (Double.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                    param.args[p] = Double.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("charArray") && (param.args[p] instanceof char[])) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = item.paramNewValue.toString().toCharArray();
                                } else if (item.paramMatch.toString().toCharArray().equals(param.args[p])) {
                                    param.args[p] = item.paramNewValue.toString().toCharArray();
                                }
                            }
                        }
                    } else if (item.classMethod.equalsIgnoreCase(param.method.getDeclaringClass().getName() + "." + param.method.getDeclaringClass().getSimpleName())) {
                        p = item.position - 1;
                        if (param.args[p] != null) {
                            if (item.paramType.equals("boolean")) {
                                if (param.args[p] instanceof Boolean) {
                                    param.args[p] = Boolean.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("String") && (param.args[p] instanceof String)) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = item.paramNewValue;
                                } else if (item.paramMatch.equals(param.args[p])) {
                                    param.args[p] = item.paramNewValue;
                                }
                            } else if (item.paramType.equals("int") && (param.args[p] instanceof Integer)) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = Integer.valueOf(item.paramNewValue.toString());
                                } else if (Integer.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                    param.args[p] = Integer.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("ByteArray") && param.args[p].getClass().equals(byte[].class)) {
                                v = Util.byteArrayToString((byte[]) param.args[p]);
                                if (item.paramMatch == null || item.paramMatch.toString().trim().equals("")) {
                                    param.args[p] = item.paramNewValue.toString().getBytes();
                                } else if (v.equals(item.paramMatch.toString())) {
                                    param.args[p] = v.getBytes();
                                }
                            } else if (item.paramType.equals("short") && (param.args[p] instanceof Short)) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = Short.valueOf(item.paramNewValue.toString());
                                } else if (Short.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                    param.args[p] = Short.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("long") && (param.args[p] instanceof Long)) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = Long.valueOf(item.paramNewValue.toString());
                                } else if (Long.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                    param.args[p] = Long.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("float") && (param.args[p] instanceof Float)) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = Float.valueOf(item.paramNewValue.toString());
                                } else if (Float.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                    param.args[p] = Float.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("double") && (param.args[p] instanceof Double)) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = Double.valueOf(item.paramNewValue.toString());
                                } else if (Double.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                    param.args[p] = Double.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("charArray") && (param.args[p] instanceof char[])) {
                                if (item.paramMatch == null || item.paramMatch.toString().trim() == "") {
                                    param.args[p] = item.paramNewValue.toString().toCharArray();
                                } else if (item.paramMatch.toString().toCharArray().equals(param.args[p])) {
                                    param.args[p] = item.paramNewValue.toString().toCharArray();
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean resultReplace(MethodHookParam param, XSharedPreferences sPrefs) {
        if (!sPrefs.getString(Config.SP_USER_RETURN_REPLACES, "").trim().equals("")) {
            for (ReplaceReturnItem item : ((ReplaceReturnList) gson.fromJson("{\"replaceReturnItems\": " + sPrefs.getString(Config.SP_USER_RETURN_REPLACES, "") + "}", ReplaceReturnList.class)).replaceReturnItems) {
                if (item.state && item.classMethod.equalsIgnoreCase(param.method.getDeclaringClass().getName() + "." + param.method.getName()) && item.returnNewValue != null && !item.returnNewValue.equals("void")) {
                    if (item.returnType.equals("boolean") && (param.getResult() instanceof Boolean)) {
                        param.setResult(Boolean.valueOf(item.returnNewValue.toString()));
                    } else if (item.returnType.equals("String") && (param.getResult() instanceof String)) {
                        if (item.returnMatch == null || item.returnMatch.toString().trim() == "") {
                            param.setResult(item.returnNewValue);
                        } else if (item.returnMatch.equals(param.getResult())) {
                            param.setResult(item.returnNewValue);
                        }
                    } else if (item.returnType.equals("int") && (param.getResult() instanceof Integer)) {
                        if (item.returnMatch == null || item.returnMatch.toString().trim() == "") {
                            param.setResult(Integer.valueOf(item.returnNewValue.toString()));
                        } else if (Integer.valueOf(item.returnNewValue.toString()).equals(param.getResult())) {
                            param.setResult(Integer.valueOf(item.returnNewValue.toString()));
                        }
                    } else if (item.returnType.equals("ByteArray") && param.getResult().getClass().equals(byte[].class)) {
                        String v = Util.byteArrayToString((byte[]) param.getResult());
                        if (item.returnMatch == null || item.returnMatch.toString().trim() == "") {
                            param.setResult(item.returnNewValue.toString().getBytes());
                        } else {
                            param.setResult(v.getBytes());
                        }
                    } else if (item.returnType.equals("short") && (param.getResult() instanceof Short)) {
                        if (item.returnMatch == null || item.returnMatch.toString().trim() == "") {
                            param.setResult(Short.valueOf(item.returnNewValue.toString()));
                        } else if (Short.valueOf(item.returnNewValue.toString()).equals(param.getResult())) {
                            param.setResult(Short.valueOf(item.returnNewValue.toString()));
                        }
                    } else if (item.returnType.equals("long") && (param.getResult() instanceof Long)) {
                        if (item.returnMatch == null || item.returnMatch.toString().trim() == "") {
                            param.setResult(Long.valueOf(item.returnNewValue.toString()));
                        } else if (Long.valueOf(item.returnNewValue.toString()).equals(param.getResult())) {
                            param.setResult(Long.valueOf(item.returnNewValue.toString()));
                        }
                    } else if (item.returnType.equals("float") && (param.getResult() instanceof Float)) {
                        if (item.returnMatch == null || item.returnMatch.toString().trim() == "") {
                            param.setResult(Float.valueOf(item.returnNewValue.toString()));
                        } else if (Float.valueOf(item.returnNewValue.toString()).equals(param.getResult())) {
                            param.setResult(Float.valueOf(item.returnNewValue.toString()));
                        }
                    } else if (item.returnType.equals("double") && (param.getResult() instanceof Double)) {
                        if (item.returnMatch == null || item.returnMatch.toString().trim() == "") {
                            param.setResult(Double.valueOf(item.returnNewValue.toString()));
                        } else if (Double.valueOf(item.returnNewValue.toString()).equals(param.getResult())) {
                            param.setResult(Double.valueOf(item.returnNewValue.toString()));
                        }
                    } else if (item.returnType.equals("charArray") && (param.getResult() instanceof char[])) {
                        if (item.returnMatch == null || item.returnMatch.toString().trim() == "") {
                            param.setResult(item.returnNewValue.toString().toCharArray());
                        } else if (item.returnNewValue.toString().toCharArray().equals(param.getResult())) {
                            param.setResult(item.returnNewValue.toString().toCharArray());
                        }
                    }
                }
            }
        }
        return true;
    }
}
