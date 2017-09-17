package mobi.acpm.inspeckage.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {
    public static void writeToFile(SharedPreferences prefs, String data, FileType ft, String name) {
        try {
            String absolutePath;
            if (prefs.getBoolean(Config.SP_HAS_W_PERMISSION, false)) {
                absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath() + Config.P_ROOT + "/" + prefs.getString(Config.SP_PACKAGE, "");
            } else {
                absolutePath = prefs.getString(Config.SP_DATA_DIR, null) + Config.P_ROOT;
            }
            boolean append = true;
            if (ft != null) {
                switch (ft) {
                    case SERIALIZATION:
                        absolutePath = absolutePath + Config.P_SERIALIZATION;
                        data = data + "</br>";
                        break;
                    case CLIPB:
                        absolutePath = absolutePath + Config.P_CLIPB;
                        data = data + "</br>";
                        break;
                    case HASH:
                        absolutePath = absolutePath + Config.P_HASH;
                        data = data + "</br>";
                        break;
                    case CRYPTO:
                        absolutePath = absolutePath + Config.P_CRYPTO;
                        data = data + "</br>";
                        break;
                    case IPC:
                        absolutePath = absolutePath + Config.P_IPC;
                        data = data + "</br>";
                        break;
                    case PREFS:
                        absolutePath = absolutePath + Config.P_PREFS;
                        data = data + "</br>";
                        break;
                    case PREFS_BKP:
                        absolutePath = absolutePath + Config.PREFS_BKP + name;
                        File conf = new File(absolutePath);
                        if (conf.exists()) {
                            conf.delete();
                            break;
                        }
                        break;
                    case LOG:
                        absolutePath = absolutePath + Config.P_LOG;
                        break;
                    case PACKAGE:
                        absolutePath = absolutePath + Config.P_PACKAGE_DETAIL;
                        break;
                    case SQLITE:
                        absolutePath = absolutePath + Config.P_SQLITE;
                        data = data + "</br>";
                        break;
                    case WEBVIEW:
                        absolutePath = absolutePath + Config.P_WEBVIEW;
                        data = data + "</br>";
                        break;
                    case FILESYSTEM:
                        absolutePath = absolutePath + Config.P_FILESYSTEM;
                        data = data + "</br>";
                        break;
                    case MISC:
                        absolutePath = absolutePath + Config.P_MISC;
                        data = data + "</br>";
                        break;
                    case HTTP:
                        absolutePath = absolutePath + Config.P_HTTP;
                        data = data + "</br>";
                        break;
                    case USERHOOKS:
                        absolutePath = absolutePath + Config.P_USERHOOKS;
                        data = data + "</br>";
                        break;
                    case APP_STRUCT:
                        absolutePath = absolutePath + Config.P_APP_STRUCT;
                        append = false;
                        break;
                    case REPLACEMENT:
                        absolutePath = absolutePath + Config.P_REPLACEMENT;
                        break;
                }
                File file = new File(absolutePath);
                if (!file.exists()) {
                    File path = new File(String.valueOf(file.getParentFile()));
                    path.setReadable(true, false);
                    path.setExecutable(true, false);
                    path.setWritable(true, false);
                    path.mkdirs();
                    path.setReadable(true, false);
                    path.setExecutable(true, false);
                    path.setWritable(true, false);
                    file.createNewFile();
                    file.setReadable(true, false);
                    file.setExecutable(true, false);
                    file.setWritable(true, false);
                }
                FileOutputStream fOut = new FileOutputStream(file, append);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.write(data);
                myOutWriter.close();
                fOut.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(SharedPreferences prefs, FileType ft) {
        String text = "";
        try {
            String absolutePath;
            if (prefs.getBoolean(Config.SP_HAS_W_PERMISSION, false)) {
                absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath() + Config.P_ROOT + "/" + prefs.getString(Config.SP_PACKAGE, "");
            } else {
                absolutePath = prefs.getString(Config.SP_DATA_DIR, null) + Config.P_ROOT;
            }
            switch (ft) {
                case SERIALIZATION:
                    absolutePath = absolutePath + Config.P_SERIALIZATION;
                    break;
                case CLIPB:
                    absolutePath = absolutePath + Config.P_CLIPB;
                    break;
                case HASH:
                    absolutePath = absolutePath + Config.P_HASH;
                    break;
                case CRYPTO:
                    absolutePath = absolutePath + Config.P_CRYPTO;
                    break;
                case IPC:
                    absolutePath = absolutePath + Config.P_IPC;
                    break;
                case PREFS:
                    absolutePath = absolutePath + Config.P_PREFS;
                    break;
                case LOG:
                    absolutePath = absolutePath + Config.P_LOG;
                    break;
                case PACKAGE:
                    absolutePath = absolutePath + Config.P_PACKAGE_DETAIL;
                    break;
                case SQLITE:
                    absolutePath = absolutePath + Config.P_SQLITE;
                    break;
                case WEBVIEW:
                    absolutePath = absolutePath + Config.P_WEBVIEW;
                    break;
                case FILESYSTEM:
                    absolutePath = absolutePath + Config.P_FILESYSTEM;
                    break;
                case MISC:
                    absolutePath = absolutePath + Config.P_MISC;
                    break;
                case HTTP:
                    absolutePath = absolutePath + Config.P_HTTP;
                    break;
                case USERHOOKS:
                    absolutePath = absolutePath + Config.P_USERHOOKS;
                    break;
                case APP_STRUCT:
                    absolutePath = absolutePath + Config.P_APP_STRUCT;
                    break;
            }
            File file = new File(absolutePath);
            if (file.exists()) {
                if (file.length() > 1048576) {
                    RandomAccessFile aFile = new RandomAccessFile(absolutePath, "r");
                    FileChannel inChannel = aFile.getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate(AccessibilityEventCompat.TYPE_TOUCH_INTERACTION_START);
                    while (inChannel.read(buffer) > 0) {
                        buffer.flip();
                        text = Charset.forName("UTF-8").decode(buffer).toString();
                        buffer.clear();
                    }
                    inChannel.close();
                    aFile.close();
                } else {
                    FileChannel ch = new FileInputStream(absolutePath).getChannel();
                    ByteBuffer mbb = ch.map(MapMode.READ_ONLY, 0, ch.size());
                    while (mbb.hasRemaining()) {
                        text = Charset.forName("UTF-8").decode(mbb).toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    public static Map<String, String> readMultiFile(SharedPreferences prefs, String folderName) {
        Map<String, String> files = new HashMap();
        try {
            String absolutePath;
            if (prefs.getBoolean(Config.SP_HAS_W_PERMISSION, false)) {
                absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath() + Config.P_ROOT + "/" + prefs.getString(Config.SP_PACKAGE, "") + "/" + folderName;
            } else {
                absolutePath = prefs.getString(Config.SP_DATA_DIR, null) + Config.P_ROOT + "/" + folderName;
            }
            File folder = new File(absolutePath);
            if (folder.listFiles() != null && folder.length() > 0) {
                for (File fileEntry : folder.listFiles()) {
                    if (fileEntry.exists() && fileEntry.isFile()) {
                        FileChannel ch = new FileInputStream(fileEntry.getAbsolutePath()).getChannel();
                        ByteBuffer mbb = ch.map(MapMode.READ_ONLY, 0, ch.size());
                        String text = "";
                        while (mbb.hasRemaining()) {
                            text = Charset.forName("UTF-8").decode(mbb).toString();
                        }
                        files.put(fileEntry.getName(), text);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    public static String readHtmlFile(Context context, String fileName) {
        String htmlFile = "";
        try {
            StringBuilder buf = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(context.getAssets().open("HTMLFiles" + fileName), "UTF-8"));
            while (true) {
                String str = in.readLine();
                if (str == null) {
                    break;
                }
                buf.append(str);
            }
            in.close();
            htmlFile = buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return htmlFile;
    }

    public static void zipFolder(String inputFolderPath, String outZipPath) {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outZipPath));
            addDir(new File(inputFolderPath), zos);
            zos.close();
        } catch (IOException ioe) {
            Log.e("ZIPFILE", ioe.getMessage());
        }
    }

    static void addDir(File srcFile, ZipOutputStream zos) throws IOException {
        byte[] buffer = new byte[1024];
        for (File file : srcFile.listFiles()) {
            if (file.isDirectory()) {
                addDir(file, zos);
            } else {
                FileInputStream fis = new FileInputStream(file);
                zos.putNextEntry(new ZipEntry(file.getName()));
                while (true) {
                    int length = fis.read(buffer);
                    if (length <= 0) {
                        break;
                    }
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    public static void deleteFile(File file) {
        file.delete();
    }

    public static void writeJsonFile(SharedPreferences prefs, String data, String name) {
        try {
            String absolutePath;
            if (prefs.getBoolean(Config.SP_HAS_W_PERMISSION, false)) {
                absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath() + Config.P_ROOT + "/" + prefs.getString(Config.SP_PACKAGE, "");
            } else {
                absolutePath = prefs.getString(Config.SP_DATA_DIR, null) + Config.P_ROOT;
            }
            File file = new File(absolutePath + "/" + name);
            if (!file.exists()) {
                File path = new File(String.valueOf(file.getParentFile()));
                path.setReadable(true, false);
                path.setExecutable(true, false);
                path.setWritable(true, false);
                path.mkdirs();
                path.setReadable(true, false);
                path.setExecutable(true, false);
                path.setWritable(true, false);
                file.createNewFile();
                file.setReadable(true, false);
                file.setExecutable(true, false);
                file.setWritable(true, false);
            }
            OutputStreamWriter myOutWriter = new OutputStreamWriter(new FileOutputStream(file, false));
            myOutWriter.write(data);
            myOutWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
