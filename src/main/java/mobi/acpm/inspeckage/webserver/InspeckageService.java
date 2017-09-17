package mobi.acpm.inspeckage.webserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import java.io.IOException;
import mobi.acpm.inspeckage.util.Config;

public class InspeckageService extends Service {
    private WebServer ws;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getApplicationContext();
        String host = null;
        int port = 8008;
        if (!(intent == null || intent.getExtras() == null)) {
            host = intent.getStringExtra(Config.SP_PROXY_HOST);
            port = intent.getIntExtra(Config.SP_PROXY_PORT, 8008);
        }
        try {
            this.ws = new WebServer(host, port, context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Service started on port " + port, 1).show();
        return 1;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.ws != null) {
            this.ws.stop();
        }
        Toast.makeText(this, "Service stopped", 1).show();
    }
}
