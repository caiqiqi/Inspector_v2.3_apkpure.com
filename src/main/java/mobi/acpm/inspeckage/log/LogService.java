package mobi.acpm.inspeckage.log;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.NotYetConnectedException;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.util.Config;

public class LogService extends Service {
    public static final String TAG = "Inspeckage_Log";
    private boolean isStarted = false;
    private Process logProcess;
    private Thread logThread;
    private SharedPreferences mPrefs;
    private String pid = "";
    private Thread pidThread;
    private WSocketServer wss;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getApplicationContext();
        int port = 8887;
        try {
            String filter = "";
            if (!(intent == null || intent.getExtras() == null)) {
                port = intent.getIntExtra(Config.SP_PROXY_PORT, 8887);
                filter = intent.getStringExtra("filter");
            }
            this.mPrefs = context.getSharedPreferences(Module.PREFS, 1);
            this.wss = new WSocketServer(port);
            this.wss.start();
            startLogger(filter);
            Toast.makeText(this, "LogService started on port " + port, 1).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void onDestroy() {
        Exception e;
        super.onDestroy();
        if (this.wss != null) {
            try {
                stopLogger();
                this.wss.stop();
                Toast.makeText(this, "LogService stopped", 1).show();
                return;
            } catch (IOException e2) {
                e = e2;
            } catch (InterruptedException e3) {
                e = e3;
            }
        } else {
            return;
        }
        e.printStackTrace();
    }

    private void startLogger(final String filter) {
        if (!this.isStarted) {
            this.logThread = new Thread(new Runnable() {
                public void run() {
                    Exception e;
                    try {
                        Runtime.getRuntime().exec("su -c logcat -c");
                        String cmd = "su -c logcat |grep -v Xposed |grep -v Inspeckage |grep -v E/ |grep -v I/ |grep -v W/ |grep -v F/ |grep -v V/ |grep -v W/ |grep -v D/";
                        for (String filter1 : filter.split(",")) {
                            if (cmd.contains(filter1 + "/")) {
                                cmd = cmd.replace("|grep -v " + filter1 + "/", "");
                            }
                        }
                        LogService.this.logProcess = Runtime.getRuntime().exec(cmd);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(LogService.this.logProcess.getInputStream()));
                        while (LogService.this.isStarted) {
                            String line = bufferedReader.readLine();
                            if (line != null) {
                                if (LogService.this.pid.trim().length() > 2 && line.contains(LogService.this.pid.trim())) {
                                    LogService.this.wss.sendToClient(line);
                                }
                            }
                        }
                        LogService.this.logProcess.destroy();
                    } catch (IOException e2) {
                        e = e2;
                        Log.e(LogService.TAG, "LogService failed: " + e.getMessage());
                    } catch (NotYetConnectedException e3) {
                        e = e3;
                        Log.e(LogService.TAG, "LogService failed: " + e.getMessage());
                    }
                }
            }, "Logger_Thread");
            this.logThread.start();
            this.isStarted = true;
            this.pidThread = new Thread(new Runnable() {
                public void run() {
                    Exception e;
                    while (LogService.this.isStarted) {
                        try {
                            String name = LogService.this.mPrefs.getString(Config.SP_PACKAGE, "null");
                            BufferedReader br = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("ps").getInputStream()));
                            String var = "";
                            while (true) {
                                String psline = br.readLine();
                                if (psline == null) {
                                    break;
                                } else if (psline.contains(" " + name)) {
                                    var = psline;
                                }
                            }
                            if (var.length() > 10) {
                                LogService.this.pid = var.replaceAll("\\s+", " ").split(" ")[1];
                            }
                            synchronized (this) {
                                try {
                                    wait(3000);
                                } catch (InterruptedException e2) {
                                }
                            }
                        } catch (IOException e3) {
                            e = e3;
                        } catch (NotYetConnectedException e4) {
                            e = e4;
                        }
                    }
                    return;
                    Log.e(LogService.TAG, "LogService failed: " + e.getMessage());
                }
            }, "Logger_Thread");
            this.pidThread.start();
        }
    }

    public void stopLogger() {
        if (this.isStarted) {
            this.isStarted = false;
            try {
                this.logProcess.destroy();
                this.pidThread.join();
                this.logThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
