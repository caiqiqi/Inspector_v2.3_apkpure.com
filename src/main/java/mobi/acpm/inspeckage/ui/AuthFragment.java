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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.R;
import mobi.acpm.inspeckage.log.LogService;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.webserver.InspeckageService;

public class AuthFragment extends Fragment {
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
    public AuthFragment(Activity act) {
        this.mainActivity = act;
        this.context = this.mainActivity.getApplicationContext();
    }

    public static AuthFragment newInstance(String param1, String param2) {
        AuthFragment fragment = new AuthFragment();
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
        final View view = inflater.inflate(R.layout.fragment_auth, container, false);
        TextView txtLogin = (TextView) view.findViewById(R.id.txtLogin);
        TextView txtPass = (TextView) view.findViewById(R.id.txtPass);
        final Switch mSwitch = (Switch) view.findViewById(R.id.auth_switch);
        String login = this.mPrefs.getString(Config.SP_USER_PASS, "");
        if (!login.trim().equals("")) {
            txtLogin.setText(login.split(":")[0]);
            txtPass.setText(login.split(":")[1]);
        }
        mSwitch.setChecked(Boolean.valueOf(this.mPrefs.getBoolean(Config.SP_SWITCH_AUTH, false)).booleanValue());
        txtLogin.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSwitch.setChecked(false);
            }

            public void afterTextChanged(Editable s) {
            }
        });
        txtPass.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSwitch.setChecked(false);
            }

            public void afterTextChanged(Editable s) {
            }
        });
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Editor edit = AuthFragment.this.mPrefs.edit();
                if (isChecked) {
                    edit.putBoolean(Config.SP_SWITCH_AUTH, true);
                } else {
                    edit.putBoolean(Config.SP_SWITCH_AUTH, false);
                }
                edit.putString(Config.SP_USER_PASS, ((TextView) view.findViewById(R.id.txtLogin)).getText() + ":" + ((TextView) view.findViewById(R.id.txtPass)).getText());
                edit.apply();
                AuthFragment.this.stopService();
                String host = null;
                int port = AuthFragment.this.mPrefs.getInt(Config.SP_SERVER_PORT, 8008);
                if (!AuthFragment.this.mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces").equals("All interfaces")) {
                    host = AuthFragment.this.mPrefs.getString(Config.SP_SERVER_HOST, "All interfaces");
                }
                AuthFragment.this.startService(host, port);
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
