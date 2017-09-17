package mobi.acpm.inspeckage.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.R;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.util.PackageDetail;
import mobi.acpm.inspeckage.util.Util;
import mobi.acpm.inspeckage.webserver.InspeckageService;
import mobi.acpm.inspeckage.webserver.WebServer;

public class MainFragment extends Fragment {
    private Context context;
    private OnFragmentInteractionListener mListener;
    private SharedPreferences mPrefs;
    private Activity mainActivity;
    private PackageDetail pd;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @SuppressLint({"ValidFragment"})
    public MainFragment(Activity act) {
        this.mainActivity = act;
        this.context = this.mainActivity.getApplicationContext();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Context context = this.context;
            String str = Module.PREFS;
            Context context2 = this.context;
            this.mPrefs = context.getSharedPreferences(str, 1);
            String host = null;
            if (!this.mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces").equals("All interfaces")) {
                host = this.mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces");
            }
            startService(host, this.mPrefs.getInt(Config.SP_SERVER_PORT, 8008));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ExpandableListView mExpandableList = (ExpandableListView) view.findViewById(R.id.appsListView);
        loadListView(view);
        TextView txtModule = (TextView) view.findViewById(R.id.txtModule);
        if (WebServer.isModuleEnabled()) {
            txtModule.setText(R.string.module_enabled);
            txtModule.setBackgroundColor(0);
        }
        TextView txtServer = (TextView) view.findViewById(R.id.txtServer);
        if (Util.isMyServiceRunning(this.context, InspeckageService.class)) {
            txtServer.setText(R.string.server_started);
            txtServer.setBackgroundColor(0);
        }
        final View view2 = view;
        mExpandableList.setOnChildClickListener(new OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                TextView txtPackage = (TextView) v.findViewById(R.id.txtListPkg);
                TextView txtAppName = (TextView) v.findViewById(R.id.txtListItem);
                MainFragment.this.loadSelectedApp(txtPackage.getText().toString());
                ((TextView) view2.findViewById(R.id.txtAppSelected)).setText(">>> " + txtPackage.getText().toString());
                Toast.makeText(MainFragment.this.context, "" + txtAppName.getText().toString(), 0).show();
                MainFragment.this.loadListView(view2);
                return true;
            }
        });
        Switch mSwitch = (Switch) view.findViewById(R.id.only_user_app_switch);
        mSwitch.setChecked(Boolean.valueOf(this.mPrefs.getBoolean(Config.SP_SWITCH_OUA, true)).booleanValue());
        view2 = view;
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Editor edit = MainFragment.this.mPrefs.edit();
                if (isChecked) {
                    edit.putBoolean(Config.SP_SWITCH_OUA, true);
                } else {
                    edit.putBoolean(Config.SP_SWITCH_OUA, false);
                }
                edit.apply();
                MainFragment.this.loadListView(view2);
            }
        });
        ((Button) view.findViewById(R.id.btnLaunchApp)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (MainFragment.this.pd == null) {
                    MainFragment.this.pd = new PackageDetail(MainFragment.this.context, MainFragment.this.mPrefs.getString(Config.SP_PACKAGE, ""));
                }
                Intent i = MainFragment.this.pd.getLaunchIntent();
                if (i != null) {
                    MainFragment.this.startActivity(i);
                } else {
                    Toast.makeText(MainFragment.this.context, "Launch Intent not found.", 0).show();
                }
            }
        });
        loadInterfaces();
        String scheme = "http://";
        if (this.mPrefs.getBoolean(Config.SP_SWITCH_AUTH, false)) {
            scheme = "https://";
        }
        String port = String.valueOf(this.mPrefs.getInt(Config.SP_SERVER_PORT, 8008));
        String host = "";
        if (this.mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces").equals("All interfaces")) {
            String[] adds = this.mPrefs.getString(Config.SP_SERVER_INTERFACES, "--").split(",");
            for (int i = 0; i < adds.length; i++) {
                if (!adds[i].equals("All interfaces")) {
                    host = host + scheme + adds[i] + ":" + port + "\n";
                }
            }
        } else {
            String ip = this.mPrefs.getString(Config.SP_SERVER_HOST, "127.0.0.1");
            host = scheme + ip + ":" + port;
            Editor edit = this.mPrefs.edit();
            edit.putString(Config.SP_SERVER_IP, ip);
            edit.apply();
        }
        ((TextView) view.findViewById(R.id.txtHost)).setText(host);
        ((TextView) view.findViewById(R.id.txtAdb)).setText("adb forward tcp:" + port + " tcp:" + port);
        ((TextView) view.findViewById(R.id.txtAppSelected)).setText(">>> " + this.mPrefs.getString(Config.SP_PACKAGE, "..."));
        return view;
    }

    public void loadInterfaces() {
        StringBuilder sb = new StringBuilder();
        sb.append("All interfaces,");
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                Enumeration<InetAddress> enumIpAddr = ((NetworkInterface) en.nextElement()).getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    boolean isIPv4;
                    String address = ((InetAddress) enumIpAddr.nextElement()).getHostAddress();
                    if (address.indexOf(58) < 0) {
                        isIPv4 = true;
                    } else {
                        isIPv4 = false;
                    }
                    if (isIPv4) {
                        sb.append(address + ",");
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(Module.ERROR, ex.toString());
        }
        Editor edit = this.mPrefs.edit();
        edit.putString(Config.SP_SERVER_INTERFACES, sb.toString().substring(0, sb.length() - 1));
        edit.apply();
    }

    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    public void startService(String host, int port) {
        Intent i = new Intent(this.context, InspeckageService.class);
        i.putExtra(Config.SP_PROXY_PORT, port);
        i.putExtra(Config.SP_PROXY_HOST, host);
        this.context.startService(i);
    }

    public void stopService() {
        this.context.stopService(new Intent(this.context, InspeckageService.class));
    }

    private ArrayList<ExpandableListItem> getInstalledApps() {
        ArrayList<ExpandableListItem> appsList = new ArrayList();
        List<PackageInfo> packs = this.context.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = (PackageInfo) packs.get(i);
            if (!this.mPrefs.getBoolean(Config.SP_SWITCH_OUA, true) || (p.applicationInfo.flags & 129) == 0) {
                ExpandableListItem pInfo = new ExpandableListItem();
                pInfo.setAppName(p.applicationInfo.loadLabel(this.context.getPackageManager()).toString());
                pInfo.setPckName(p.packageName);
                pInfo.setIcon(p.applicationInfo.loadIcon(this.context.getPackageManager()));
                if (p.packageName.trim().equals(this.mPrefs.getString(Config.SP_PACKAGE, "").trim())) {
                    pInfo.setSelected(true);
                }
                appsList.add(pInfo);
            }
        }
        return appsList;
    }

    private void loadListView(View view) {
        List<String> mListDataHeader = new ArrayList();
        mListDataHeader.add(this.context.getString(R.string.fragment_config_choose));
        HashMap<String, List<ExpandableListItem>> mListDataChild = new HashMap();
        ArrayList<ExpandableListItem> mApps = getInstalledApps();
        Collections.sort(mApps, new Comparator<ExpandableListItem>() {
            public int compare(ExpandableListItem o1, ExpandableListItem o2) {
                return o1.getAppName().compareTo(o2.getAppName());
            }
        });
        ExpandableListView appList = (ExpandableListView) view.findViewById(R.id.appsListView);
        mListDataChild.put(mListDataHeader.get(0), mApps);
        appList.setAdapter(new ExpandableListAdapter(getActivity(), mListDataHeader, mListDataChild));
    }

    private void loadSelectedApp(String pkg) {
        Editor edit = this.mPrefs.edit();
        edit.putString(Config.SP_PACKAGE, pkg);
        this.pd = new PackageDetail(this.context, pkg);
        edit.putBoolean(Config.SP_HAS_W_PERMISSION, false);
        if (this.pd.getRequestedPermissions().contains("android.permission.WRITE_EXTERNAL_STORAGE") && VERSION.SDK_INT < 23) {
            edit.putBoolean(Config.SP_HAS_W_PERMISSION, true);
        }
        edit.putString(Config.SP_APP_NAME, this.pd.getAppName());
        edit.putString(Config.SP_APP_ICON_BASE64, this.pd.getIconBase64());
        edit.putString(Config.SP_PROCESS_NAME, this.pd.getProcessName());
        edit.putString(Config.SP_APP_VERSION, this.pd.getVersion());
        edit.putString(Config.SP_DEBUGGABLE, this.pd.isDebuggable());
        edit.putString(Config.SP_ALLOW_BACKUP, this.pd.allowBackup());
        edit.putString(Config.SP_APK_DIR, this.pd.getApkDir());
        edit.putString(Config.SP_UID, this.pd.getUID());
        edit.putString(Config.SP_GIDS, this.pd.getGIDs());
        edit.putString(Config.SP_DATA_DIR, this.pd.getDataDir());
        edit.putString(Config.SP_REQ_PERMISSIONS, this.pd.getRequestedPermissions());
        edit.putString(Config.SP_APP_PERMISSIONS, this.pd.getAppPermissions());
        edit.putString(Config.SP_EXP_ACTIVITIES, this.pd.getExportedActivities());
        edit.putString(Config.SP_N_EXP_ACTIVITIES, this.pd.getNonExportedActivities());
        edit.putString(Config.SP_EXP_SERVICES, this.pd.getExportedServices());
        edit.putString(Config.SP_N_EXP_SERVICES, this.pd.getNonExportedServices());
        edit.putString(Config.SP_EXP_BROADCAST, this.pd.getExportedBroadcastReceivers());
        edit.putString(Config.SP_N_EXP_BROADCAST, this.pd.getNonExportedBroadcastReceivers());
        edit.putString(Config.SP_EXP_PROVIDER, this.pd.getExportedContentProvider());
        edit.putString(Config.SP_N_EXP_PROVIDER, this.pd.getNonExportedContentProvider());
        edit.putString(Config.SP_SHARED_LIB, this.pd.getSharedLibraries());
        edit.putBoolean(Config.SP_APP_IS_RUNNING, false);
        edit.putString(Config.SP_DATA_DIR_TREE, "");
        edit.apply();
        if (this.pd.getRequestedPermissions().contains("android.permission.WRITE_EXTERNAL_STORAGE")) {
            this.pd.extractInfoToFile();
        }
    }
}
