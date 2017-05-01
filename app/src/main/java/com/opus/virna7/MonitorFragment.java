package com.opus.virna7;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by opus on 03/03/17.
 */
public class MonitorFragment extends Fragment implements SeekBar.OnSeekBarChangeListener,
                                                            AdapterView.OnItemSelectedListener,
                                                            ObjectAnimator.AnimatorListener{

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String TAG = "MONITORFRAG";

    private EtherService etherservice;
    private SharedPreferences preferences ;

    private SeekBar sk_wg20vel;
    private EditText et_wg20vel;
    private Switch sw_wg20vel;
    private Spinner sp_wg20vel;

    private Button bt_lvdtzero;
    private Button bt_userecal;

    private Switch sw_vac_enable;
    private Switch sw_drum_enable;

    private TextView tv_mon_raw;
    private TextView tv_mon_cal;


    private double[] coefs = new double[3];
    private double[] limits = new double[2];
    private double[] recal = new double[2];

    private EditText et_lvdta0;
    private EditText et_lvdta1;
    private EditText et_lvdta2;
    private EditText et_lvdtmin;
    private EditText et_lvdtmax;

    private ImageView gpt;
    private ObjectAnimator cvexpt;
    private boolean ptmoving = false;
    double lastpt = 0f;
    boolean userecalflag = true;
    double cvex = 0;

    EditText et_temp;
    String stemp;
    Double dtemp;


    public MonitorFragment() {
    }


    public static MonitorFragment newInstance(int sectionNumber) {
        MonitorFragment fragment = new MonitorFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public void bindService(EtherService etherservice){
        this.etherservice = etherservice;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.monitor_fragment, container, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        sk_wg20vel = (SeekBar) rootView.findViewById(R.id.sk_mon_wg20vel);
        et_wg20vel = (EditText) rootView.findViewById(R.id.et_mon_wg20vel);
        sw_wg20vel = (Switch) rootView.findViewById(R.id.sw_mon_wg20enable);
        sp_wg20vel = (Spinner) rootView.findViewById(R.id.sp_mon_wg20vel);

        bt_lvdtzero = (Button) rootView.findViewById(R.id.bt_mon_lvdtzero);
        bt_userecal = (Button) rootView.findViewById(R.id.bt_mon_userecal);

        sk_wg20vel.setOnSeekBarChangeListener(this);
        sp_wg20vel.setOnItemSelectedListener(this);

        sw_vac_enable = (Switch) rootView.findViewById(R.id.sw_mon_vac);
        sw_drum_enable = (Switch) rootView.findViewById(R.id.sw_mon_drum);

        tv_mon_raw = (TextView) rootView.findViewById(R.id.tv_mon_lvdtraw);
        tv_mon_cal = (TextView) rootView.findViewById(R.id.tv_mon_lvdtcalib);

        et_lvdta0 = (EditText) rootView.findViewById(R.id.et_mon_lvdta0);
        et_lvdta1 = (EditText) rootView.findViewById(R.id.et_mon_lvdta1);
        et_lvdta2 = (EditText) rootView.findViewById(R.id.et_mon_lvdta2);
        et_lvdtmin = (EditText) rootView.findViewById(R.id.et_mon_lvdtmin);
        et_lvdtmax = (EditText) rootView.findViewById(R.id.et_mon_lvdtmax);

        stemp = preferences.getString(getString(R.string.pref_mon_k_lvdta0), "-11.1102432");
        coefs[0] = Double.valueOf(stemp);
        et_lvdta0.setText(stemp);

        stemp = preferences.getString(getString(R.string.pref_mon_k_lvdta1), "0.00032752");
        coefs[1] = Double.valueOf(stemp);
        et_lvdta1.setText(stemp);

        stemp = preferences.getString(getString(R.string.pref_mon_k_lvdta2), "0");
        coefs[2] = Double.valueOf(stemp);
        et_lvdta2.setText(stemp);

        stemp = preferences.getString(getString(R.string.pref_mon_k_lvdtmin), "-10");
        limits[0] = Double.valueOf(stemp);
        et_lvdtmin.setText(stemp);

        stemp = preferences.getString(getString(R.string.pref_mon_k_lvdtmax), "10");
        limits[1] = Double.valueOf(stemp);
        et_lvdtmax.setText(stemp);

        stemp = preferences.getString(getString(R.string.pref_mon_k_lvdtoffset), "0");
        recal[0] = Double.valueOf(stemp);

        stemp = preferences.getString(getString(R.string.pref_mon_k_lvdtslope), "1");
        recal[1] = Double.valueOf(stemp);

        userecalflag = preferences.getBoolean(getString(R.string.pref_mon_k_lvdtuserecal), true);
        if (userecalflag){
            bt_userecal.setText("Recal OFF");
        }
        else{
            bt_userecal.setText("Recal ON");
        }


        gpt = (ImageView)  rootView.findViewById(R.id.gpt);
        cvexpt = ObjectAnimator.ofFloat(gpt, "translationX", 0, 0);
        stemp = preferences.getString(getString(R.string.pref_mon_k_lvdtinterpolator), "0");
        if (!stemp.equals("0")){
            cvexpt.setInterpolator(new AccelerateDecelerateInterpolator());
            cvexpt.setDuration(Integer.valueOf(stemp));
        }
        cvexpt.addListener(this);
        lastpt=0;


        et_lvdta0.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    stemp = et_lvdta0.getText().toString();
                    coefs[0] = Double.valueOf(stemp);
                    preferences.edit().putString(getString(R.string.pref_mon_k_lvdta0), stemp).apply();
                    return true;
                }
                return false;
            }
        });

        et_lvdta1.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    stemp = et_lvdta1.getText().toString();
                    coefs[1] = Double.valueOf(stemp);
                    preferences.edit().putString(getString(R.string.pref_mon_k_lvdta1), stemp).apply();
                    return true;
                }
                return false;
            }
        });

        et_lvdta2.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    stemp = et_lvdta2.getText().toString();
                    coefs[2] = Double.valueOf(stemp);
                    preferences.edit().putString(getString(R.string.pref_mon_k_lvdta2), stemp).apply();
                    return true;
                }
                return false;
            }
        });

        et_lvdtmin.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    stemp = et_lvdtmin.getText().toString();
                    limits[0] = Double.valueOf(stemp);
                    preferences.edit().putString(getString(R.string.pref_mon_k_lvdtmin), stemp).apply();
                    return true;
                }
                return false;
            }
        });


        et_lvdtmax.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    stemp = et_lvdtmax.getText().toString();
                    limits[1] = Double.valueOf(stemp);
                    preferences.edit().putString(getString(R.string.pref_mon_k_lvdtmax), stemp).apply();
                    return true;
                }
                return false;
            }
        });


        //  Text Edits
        et_wg20vel.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int t_value;
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    t_value = Integer.valueOf(et_wg20vel.getText().toString());
                    if (t_value > 100){
                        t_value = 100;
                        et_wg20vel.setText(String.valueOf(t_value));
                    }
                    sk_wg20vel.setProgress(t_value);

                    return true;
                }
                return false;
            }
        });


        sw_wg20vel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (sw_wg20vel.isChecked()){
                    Log.d(Support3.TAG, "WG20 Enable");
                    AcpMessage m = new AcpMessage(true, 4, 4,"");
                    m.setCmd(EtherService.comand_map.get("WG20_SETVEL"));
                    m.setArg(sp_wg20vel.getSelectedItemPosition());
                    m.setMessage(et_wg20vel.getText().toString());
                    etherservice.sendMessage(m);
                }
                else{
                    AcpMessage m = new AcpMessage(true, 4, 4,"");
                    m.setCmd(EtherService.comand_map.get("WG20_SETVEL"));
                    m.setArg(sp_wg20vel.getSelectedItemPosition());
                    m.setMessage("0");
                    etherservice.sendMessage(m);
                }
            }
        });

        bt_lvdtzero.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recal[0] = cvex * -1;
                stemp = String.valueOf(recal[0]);
                preferences.edit().putString(getString(R.string.pref_mon_k_lvdtoffset), stemp).apply();
            }
        });

        bt_userecal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (userecalflag){
                    userecalflag = false;
                    bt_userecal.setText("Recal OFF");
                }
                else{
                    userecalflag = true;
                    bt_userecal.setText("Recal ON");
                }
                preferences.edit().putBoolean(getString(R.string.pref_mon_k_lvdtuserecal), userecalflag).apply();
            }
        });

        sw_vac_enable.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (sw_vac_enable.isChecked()){
                    //Log.d(Support3.TAG, "Vac Enable");
                    AcpMessage m = new AcpMessage(true, 4, 4,"");
                    m.setCmd(EtherService.comand_map.get("MON_SETVAC"));
                    m.setArg(1);
                    m.setMessage("");
                    etherservice.sendMessage(m);
                }
                else{
                   // Log.d(Support3.TAG, "Vac Disable");
                    AcpMessage m = new AcpMessage(true, 4, 4,"");
                    m.setCmd(EtherService.comand_map.get("MON_SETVAC"));
                    m.setArg(0);
                    m.setMessage("");
                    etherservice.sendMessage(m);
                }
            }
        });

        sw_drum_enable.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (sw_drum_enable.isChecked()){
                    //Log.d(Support3.TAG, "Vac Enable");
                    AcpMessage m = new AcpMessage(true, 4, 4,"");
                    m.setCmd(EtherService.comand_map.get("MON_SETDRUM"));
                    m.setArg(1);
                    m.setMessage("");
                    etherservice.sendMessage(m);
                }
                else{
                    AcpMessage m = new AcpMessage(true, 4, 4,"");
                    m.setCmd(EtherService.comand_map.get("MON_SETDRUM"));
                    m.setArg(0);
                    m.setMessage("");
                    etherservice.sendMessage(m);
                }
            }
        });


        return rootView;
    }


    public void initWidgets(){

    }

    public void updateUI() {

        double val, acvex;
        double ptvalue;;
        //Log.d(Support3.TAG, "Updating Monitor UI");
        val = etherservice.getStatusSlot(4);
        tv_mon_raw.setText (String.format(Locale.ENGLISH, "%6.0f", val));

        cvex = calculateCVex(val);
        //Log.d(Support3.TAG, "CVEX="+cvex);
        acvex = Math.abs(cvex);

        if (acvex < 1){
            tv_mon_cal.setText (String.format(Locale.ENGLISH, "%5.3f", cvex));
        }
        else if (acvex < 10){
            tv_mon_cal.setText (String.format(Locale.ENGLISH, "%5.2f", cvex));
        }
        else {
            tv_mon_cal.setText (String.format(Locale.ENGLISH, "%6.0f", cvex));
        }

        if (!ptmoving){
            ptvalue = cvex*15;
            //Log.d(Support3.TAG, "PTValue="+ptvalue);
            cvexpt.setFloatValues( (float)lastpt , (float)ptvalue );
            lastpt = ptvalue;
            ptmoving = true;
            cvexpt.start();
        }

    }

    private double calculateCVex(double raw){
        double res = ((raw*raw)*coefs[2]) + (raw * coefs[1]) + coefs[0];
        if (userecalflag) {
            res = (res * recal[1]) + recal[0];
        }
        return res;
    }




    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (fromUser){
            if (seekBar.getId() == R.id.sk_mon_wg20vel){
                et_wg20vel.setText(String.valueOf(seekBar.getProgress()));
                //Log.d(Support3.TAG, "Adjusting wg20");
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (parent.getId() == R.id.sp_mon_wg20vel){
            Log.d(Support3.TAG, "Adjusting wg20 direction " + position);
//            AcpMessage m = new AcpMessage(true, 4, 4,"");
//            m.setCmd(EtherService.comand_map.get("WG20_SETDIR" ));
//            m.setArg(position);
//            m.setMessage("0");
//            etherservice.sendMessage(m);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(Support3.TAG, "NOTHING SELECTED");
    }



    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        ptmoving = false;
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}



//et_temp = (EditText) rootView.findViewById(R.id.et_mon_lvdta0);
//        stemp = et_temp.getText().toString();
//        dtemp = Double.valueOf( stemp);
//        coefs[0] = dtemp;
//
//        et_temp = (EditText) rootView.findViewById(R.id.et_mon_lvdta1);
//        stemp = et_temp.getText().toString();
//        dtemp = Double.valueOf( stemp);
//        coefs[1] = dtemp;
//
//        et_temp = (EditText) rootView.findViewById(R.id.et_mon_lvdta2);
//        stemp = et_temp.getText().toString();
//        dtemp = Double.valueOf( stemp);
//        coefs[2] = dtemp;
//
//        et_temp = (EditText) rootView.findViewById(R.id.et_mon_lvdtmin);
//        stemp = et_temp.getText().toString();
//        dtemp = Double.valueOf( stemp);
//        limits[0] = dtemp;
//
//        et_temp = (EditText) rootView.findViewById(R.id.et_mon_lvdtmin);
//        stemp = et_temp.getText().toString();
//        dtemp = Double.valueOf( stemp);
//        limits[0] = dtemp;