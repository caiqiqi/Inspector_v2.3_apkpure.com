package mobi.acpm.inspeckage.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.media.TransportMediator;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import mobi.acpm.inspeckage.R;
import mobi.acpm.inspeckage.ui.MainActivity;

public class Util {
    private static final int BYTE_MSK = 255;
    private static final String HEX_DIGITS = "0123456789abcdef";
    private static final int HEX_DIGIT_BITS = 4;
    private static final int HEX_DIGIT_MASK = 15;
    public static StringBuilder sb = new StringBuilder();

    public static boolean isInt(String s) {
        try {
            int i = Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String byteArrayToString(byte[] input) {
        if (input == null) {
            return "";
        }
        String out = new String(input);
        int tmp = 0;
        for (int i = 0; i < out.length(); i++) {
            int c = out.charAt(i);
            if (c >= 32 && c < TransportMediator.KEYCODE_MEDIA_PAUSE) {
                tmp++;
            }
        }
        if (((double) tmp) <= ((double) out.length()) * 0.6d) {
            return Base64.encodeToString(input, 2);
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : input) {
            if (b < (byte) 32 || b >= Byte.MAX_VALUE) {
                sb.append('.');
            } else {
                sb.append(String.format("%c", new Object[]{Byte.valueOf(b)}));
            }
        }
        return sb.toString();
    }

    public static String toHexString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder(byteArray.length * 2);
        for (byte b : byteArray) {
            int b2 = b & 255;
            sb.append(HEX_DIGITS.charAt(b2 >>> 4)).append(HEX_DIGITS.charAt(b2 & 15));
        }
        return sb.toString();
    }

    public static byte[] getBytes(InputStream is) throws IOException {
        if (is instanceof ByteArrayInputStream) {
            int size = is.available();
            byte[] buf = new byte[size];
            int len = is.read(buf, 0, size);
            return buf;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        buf = new byte[1024];
        while (true) {
            len = is.read(buf, 0, 1024);
            if (len == -1) {
                return bos.toByteArray();
            }
            bos.write(buf, 0, len);
        }
    }

    public static void showNotification(Context mContext, String info) {
        Builder mBuilder = new NotificationCompat.Builder(mContext).setSmallIcon(R.drawable.inspectorw).setContentTitle("Inspeckage").setContentText(info);
        TaskStackBuilder.create(mContext).addParentStack(MainActivity.class);
        ((NotificationManager) mContext.getSystemService("notification")).notify(0, mBuilder.build());
    }

    public static void takeScreenshot(String fileName) {
        try {
            Process sh = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = sh.getOutputStream();
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            if (new File(path + Config.P_ROOT).exists() && new File("/storage/emulated/legacy").exists()) {
                path = "/storage/emulated/legacy";
            }
            os.write(("/system/bin/screencap -p " + path + Config.P_ROOT + "/" + fileName).getBytes("ASCII"));
            os.flush();
            os.close();
            sh.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
    }

    public static void setARPEntry(String ip, String mac) {
        try {
            Process sh = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = sh.getOutputStream();
            os.write(("su -c arp -s " + ip + " " + mac + "").getBytes("ASCII"));
            os.flush();
            os.close();
            sh.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
    }

    public static void copyFileRoot(String path, String dest) {
        try {
            Process sh = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = sh.getOutputStream();
            os.write(("su -c cat " + path + " > " + dest + "").getBytes("UTF-8"));
            os.flush();
            os.close();
            sh.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
    }

    public static String FileTree(String path, String ul) {
        File[] list = new File(path).listFiles();
        if (list == null) {
            return "";
        }
        for (File f : list) {
            if (!f.isDirectory()) {
                long fileSizeInBytes = f.length();
                long fileSizeInKB = 0;
                String lengh = String.valueOf(fileSizeInBytes) + " B";
                if (fileSizeInBytes >= PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
                    fileSizeInKB = fileSizeInBytes / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
                    lengh = String.valueOf(fileSizeInKB) + " KB";
                }
                if (fileSizeInKB >= PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
                    lengh = new DecimalFormat("#,##0.###", new DecimalFormatSymbols(new Locale("pt", "BR"))).format(fileSizeInBytes / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) + " MB";
                }
                sb.append("<li><span class=\"glyphicon glyphicon-file\" aria-hidden=\"true\"></span> <button type=\"button\" class=\"btn btn-link\" onclick=\"download_file('" + f.getAbsoluteFile() + "');\" >" + f.getAbsoluteFile().getName() + "    - " + lengh + "</button></li>");
            } else if (!f.getAbsoluteFile().getName().equals("Inspeckage")) {
                sb.append("<li> <span class=\"glyphicon glyphicon-folder-open\" aria-hidden=\"true\"> " + f.getAbsoluteFile().getName() + "</span>");
                sb.append("<ul>");
                FileTree(f.getAbsolutePath(), "</ul></li>");
            }
        }
        if (!ul.equals("")) {
            sb.append("</li></ul>");
        }
        return sb.toString();
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        for (RunningServiceInfo service : ((ActivityManager) context.getSystemService("activity")).getRunningServices(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String imageToBase64(Drawable drawable) {
        Bitmap image = drawableToBitmap(drawable);
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(CompressFormat.PNG, 70, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), 0);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static int inetAddressToInt(InetAddress inetAddr) throws IllegalArgumentException {
        byte[] addr = inetAddr.getAddress();
        return ((((addr[3] & 255) << 24) | ((addr[2] & 255) << 16)) | ((addr[1] & 255) << 8)) | (addr[0] & 255);
    }

    public static byte[] macAddressToByteArr(String mac) {
        String[] macAddressParts = mac.split(":");
        byte[] macAddressBytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            macAddressBytes[i] = Integer.valueOf(Integer.parseInt(macAddressParts[i], 16)).byteValue();
        }
        return macAddressBytes;
    }
}
