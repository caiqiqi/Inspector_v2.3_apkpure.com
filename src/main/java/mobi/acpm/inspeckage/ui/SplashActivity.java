package mobi.acpm.inspeckage.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.R;

public class SplashActivity extends AppCompatActivity {
    private static int TIME_OUT = 2000;
    private SharedPreferences mPrefs;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_splash);
        this.mPrefs = getSharedPreferences(Module.PREFS, 1);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                SplashActivity.this.startActivity(new Intent(SplashActivity.this, MainActivity.class));
                SplashActivity.this.finish();
            }
        }, (long) TIME_OUT);
    }
}
