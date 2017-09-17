package mobi.acpm.inspeckage.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.R;
import mobi.acpm.inspeckage.log.LogService;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.webserver.InspeckageService;

public class ConfigFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Context context;
    private OnFragmentInteractionListener mListener;
    private String mParam1;
    private String mParam2;
    private SharedPreferences mPrefs;
    private Activity mainActivity;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @SuppressLint({"ValidFragment"})
    public ConfigFragment(Activity act) {
        this.mainActivity = act;
        this.context = this.mainActivity.getApplicationContext();
    }

    public static ConfigFragment newInstance(String param1, String param2) {
        ConfigFragment fragment = new ConfigFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = this.context;
        String str = Module.PREFS;
        Context context2 = this.context;
        this.mPrefs = context.getSharedPreferences(str, 1);
        if (getArguments() != null) {
            this.mParam1 = getArguments().getString(ARG_PARAM1);
            this.mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_config, container, false);
        String h = this.mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces");
        RadioGroup radioGroup = new RadioGroup(view.getContext());
        radioGroup.setOrientation(1);
        final String[] address = this.mPrefs.getString(Config.SP_SERVER_INTERFACES, "--").split(",");
        for (int i = 0; i < address.length; i++) {
            RadioButton rdbtn = new RadioButton(view.getContext());
            rdbtn.setId(i);
            rdbtn.setText(address[i]);
            if (h.equals(address[i])) {
                rdbtn.setChecked(true);
            }
            radioGroup.addView(rdbtn);
        }
        ((ViewGroup) view.findViewById(R.id.radiogroup)).addView(radioGroup);
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                String host = address[radioGroup.indexOfChild(radioGroup.findViewById(i))];
                Editor edit = ConfigFragment.this.mPrefs.edit();
                edit.putString(Config.SP_SERVER_HOST, host);
                edit.apply();
            }
        });
        ((TextView) view.findViewById(R.id.txtPort)).setText(String.valueOf(this.mPrefs.getInt(Config.SP_SERVER_PORT, 8008)));
        ((TextView) view.findViewById(R.id.txtWSPort)).setText(String.valueOf(this.mPrefs.getInt(Config.SP_WSOCKET_PORT, 8887)));
        ((Button) view.findViewById(R.id.btnNewPort)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TextView txtPort = (TextView) view.findViewById(R.id.txtPort);
                ConfigFragment.this.stopService();
                String host = null;
                if (!ConfigFragment.this.mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces").equals("All interfaces")) {
                    host = ConfigFragment.this.mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces");
                }
                ConfigFragment.this.startService(host, Integer.parseInt(txtPort.getText().toString()));
                TextView txtWSPort = (TextView) view.findViewById(R.id.txtWSPort);
                Editor edit = ConfigFragment.this.mPrefs.edit();
                edit.putInt(Config.SP_SERVER_PORT, Integer.valueOf(txtPort.getText().toString()).intValue());
                edit.putInt(Config.SP_WSOCKET_PORT, Integer.valueOf(txtWSPort.getText().toString()).intValue());
                edit.apply();
            }
        });
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (this.mListener != null) {
            this.mListener.onFragmentInteraction(uri);
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            this.mListener = (OnFragmentInteractionListener) context;
        }
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
        this.context.stopService(new Intent(this.context, LogService.class));
    }
}
