package mobi.acpm.inspeckage.ui;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.R;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.util.FileUtil;
import mobi.acpm.inspeckage.webserver.InspeckageService;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {
    private SharedPreferences mPrefs;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        ((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(this);
        this.mPrefs = getSharedPreferences(Module.PREFS, 1);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, new MainFragment(this));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        if (VERSION.SDK_INT >= 23) {
            boolean granted = checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0;
            boolean grantedPhone = checkSelfPermission("android.permission.READ_PHONE_STATE") != 0;
            if (granted || grantedPhone) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_PHONE_STATE"}, 0);
            }
            new AsyncTask<Void, Void, String>() {
                protected String doInBackground(Void... params) {
                    Info idInfo = null;
                    try {
                        idInfo = AdvertisingIdClient.getAdvertisingIdInfo(MainActivity.this.getApplicationContext());
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    } catch (GooglePlayServicesRepairableException e2) {
                        e2.printStackTrace();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                    String advertId = null;
                    try {
                        advertId = idInfo.getId();
                    } catch (Exception e32) {
                        e32.printStackTrace();
                    }
                    return advertId;
                }

                protected void onPostExecute(String advertId) {
                    Editor editor = MainActivity.this.mPrefs.edit();
                    editor.putString(Config.SP_ADS_ID, advertId);
                    editor.apply();
                }
            }.execute(new Void[0]);
            return;
        }
        File inspeckage = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Config.P_ROOT);
        if (!inspeckage.exists()) {
            inspeckage.mkdirs();
        }
        hideItem();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == 0) {
                    File inspeckage = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Config.P_ROOT);
                    if (!inspeckage.exists()) {
                        inspeckage.mkdirs();
                        return;
                    }
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void hideItem() {
        ((NavigationView) findViewById(R.id.nav_view)).getMenu().findItem(R.id.nav_auth).setVisible(false);
    }

    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (getFragmentManager().getBackStackEntryCount() == 1) {
            stopService();
            super.onBackPressed();
            return;
        }
        getFragmentManager().popBackStack();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        int id = item.getItemId();
        if (id == R.id.nav_clear) {
            clearAll();
            TextView txtAppSelected = (TextView) findViewById(R.id.txtAppSelected);
            if (txtAppSelected != null) {
                txtAppSelected.setText("... ");
            }
        } else if (id == R.id.nav_close) {
            clearAll();
            stopService();
            super.finish();
            Process.killProcess(Process.myPid());
        } else if (id == R.id.nav_config) {
            fragmentTransaction.replace(R.id.container, new ConfigFragment(this));
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_auth) {
            fragmentTransaction.replace(R.id.container, new AuthFragment(this));
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction("android.intent.action.SEND");
            sendIntent.putExtra("android.intent.extra.TEXT", "https://github.com/ac-pm/Inspeckage");
            sendIntent.setType(NanoHTTPD.MIME_PLAINTEXT);
            startActivity(sendIntent);
        } else {
            fragmentTransaction.replace(R.id.container, new MainFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(8388611);
        return true;
    }

    public void stopService() {
        stopService(new Intent(this, InspeckageService.class));
    }

    private void clearAll() {
        Editor edit = this.mPrefs.edit();
        String appPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!this.mPrefs.getBoolean(Config.SP_HAS_W_PERMISSION, false)) {
            appPath = this.mPrefs.getString(Config.SP_DATA_DIR, "");
        }
        edit.putString(Config.SP_PROXY_HOST, "");
        edit.putString(Config.SP_PROXY_PORT, "");
        edit.putBoolean(Config.SP_SWITCH_PROXY, false);
        edit.putBoolean(Config.SP_FLAG_SECURE, false);
        edit.putBoolean(Config.SP_UNPINNING, false);
        edit.putBoolean(Config.SP_EXPORTED, false);
        edit.putBoolean(Config.SP_HAS_W_PERMISSION, true);
        edit.putString(Config.SP_SERVER_HOST, null);
        edit.putString(Config.SP_SERVER_PORT, null);
        edit.putString(Config.SP_SERVER_IP, null);
        edit.putString(Config.SP_SERVER_INTERFACES, "");
        edit.putString(Config.SP_PACKAGE, "");
        edit.putString(Config.SP_APP_NAME, "");
        edit.putString(Config.SP_APP_VERSION, "");
        edit.putString(Config.SP_DEBUGGABLE, "");
        edit.putString(Config.SP_APK_DIR, "");
        edit.putString(Config.SP_UID, "");
        edit.putString(Config.SP_GIDS, "");
        edit.putString(Config.SP_DATA_DIR, "");
        edit.putString(Config.SP_APP_ICON_BASE64, "iVBORw0KGgoAAAANSUhEUgAAABoAAAAbCAIAAADtdAg8AAAAA3NCSVQICAjb4U/gAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAAJUlEQVRIiWP8//8/A/UAExXNGjVu1LhR40aNGzVu1LhR44aScQDKygMz8IbG2QAAAABJRU5ErkJggg==");
        edit.putString(Config.SP_EXP_ACTIVITIES, "");
        edit.putString(Config.SP_N_EXP_ACTIVITIES, "");
        edit.putString(Config.SP_REQ_PERMISSIONS, "");
        edit.putString(Config.SP_APP_PERMISSIONS, "");
        edit.putString(Config.SP_N_EXP_PROVIDER, "");
        edit.putString(Config.SP_N_EXP_SERVICES, "");
        edit.putString(Config.SP_N_EXP_BROADCAST, "");
        edit.putString(Config.SP_EXP_SERVICES, "");
        edit.putString(Config.SP_EXP_BROADCAST, "");
        edit.putString(Config.SP_EXP_PROVIDER, "");
        edit.putString(Config.SP_SHARED_LIB, "");
        edit.putBoolean(Config.SP_APP_IS_RUNNING, false);
        edit.putString(Config.SP_DATA_DIR_TREE, "");
        edit.putString(Config.SP_USER_HOOKS, "");
        edit.apply();
        FileUtil.deleteRecursive(new File(appPath + Config.P_ROOT));
    }
}
