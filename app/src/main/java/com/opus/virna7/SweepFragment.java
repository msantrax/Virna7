package com.opus.virna7;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;



public class SweepFragment extends Fragment implements SeekBar.OnSeekBarChangeListener , AdapterView.OnItemSelectedListener {

    public static final String TAG = "SWEEPFRAG";
    private static final String ARG_SECTION_NUMBER = "section_number";

    private EtherService etherservice;
    private SharedPreferences preferences ;
    private static Support3 parent;

    private RadioButton cb_inner;
    private SeekBar sk_inner;
    private SeekBar sk_outer;

    private EditText et_inner;
    private EditText et_outer;

    private Button bt_park;

    private Button bt_goto;
    private EditText et_goto;

    private Button bt_pulses;
    private EditText et_pulses;
    private EditText et_pulsesperiod;

    private Spinner sp_direction;
    private Spinner sp_scanvel;

    private Switch sw_enable;
    private Switch sw_scanenable;


    private int sk_value;
    private int sk_limit;

    public SweepFragment() {
    }


    public static SweepFragment newInstance(int sectionNumber) {
        SweepFragment fragment = new SweepFragment();
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
        View rootView = inflater.inflate(R.layout.sweep_fragment, container, false);

        cb_inner = (RadioButton) rootView.findViewById(R.id.cb_inner_sensor);

        sk_inner = (SeekBar) rootView.findViewById(R.id.sk_sweep_inner);
        et_inner = (EditText) rootView.findViewById(R.id.ed_sweep_inner);
        sk_outer = (SeekBar) rootView.findViewById(R.id.sk_sweep_outer);
        et_outer = (EditText) rootView.findViewById(R.id.ed_sweep_outer);

        bt_goto = (Button) rootView.findViewById(R.id.bt_sweep_goto);
        et_goto = (EditText) rootView.findViewById(R.id.ed_sweep_goto);

        bt_park = (Button) rootView.findViewById(R.id.bt_sweep_park);

        bt_pulses = (Button) rootView.findViewById(R.id.bt_sweep_pulses);
        et_pulses = (EditText) rootView.findViewById(R.id.ed_sweep_steps);
        et_pulsesperiod = (EditText) rootView.findViewById(R.id.ed_sweep_stepsperiod);

        sw_enable = (Switch) rootView.findViewById(R.id.sw_sweep_enable);
        sw_scanenable = (Switch) rootView.findViewById(R.id.sw_sweep_scanenable);

        sp_direction = (Spinner) rootView.findViewById(R.id.sp_sweep_direction);
        sp_scanvel = (Spinner) rootView.findViewById(R.id.sp_sweep_scanvel);

        sk_inner.setOnSeekBarChangeListener(this);
        sk_outer.setOnSeekBarChangeListener(this);

        sp_direction.setOnItemSelectedListener(this);


        preferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        initWidgets();

        et_inner.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    sk_value = Integer.valueOf(et_inner.getText().toString());
                    sk_limit = sk_outer.getProgress();
                    if (sk_value >= sk_limit){
                        sk_value = sk_limit-1;
                        et_inner.setText(String.valueOf(sk_value));
                    }
                    sk_inner.setProgress(sk_value);
                    return true;
                }
                return false;
            }
        });

        et_outer.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    sk_value = Integer.valueOf(et_outer.getText().toString());
                    sk_limit = sk_inner.getProgress();
                    if (sk_value <= sk_limit){
                        sk_value = sk_limit+1;
                        et_outer.setText(String.valueOf(sk_value));
                    }
                    sk_outer.setProgress(sk_value);
                    return true;
                }
                return false;
            }
        });


        sw_enable.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (sw_enable.isChecked()){
                    Log.d(Support3.TAG, "Sweep Enable");
                    AcpMessage m = new AcpMessage(true, 2, 2,"");
                    m.setCmd(EtherService.comand_map.get("SWEEP_ENABLE"));
                    m.setArg(1);
                    m.setMessage("");
                    etherservice.sendMessage(m);
                }
                else{
                    Log.d(Support3.TAG, "Sweep Disable");
                    AcpMessage m = new AcpMessage(true, 2, 2,"");
                    m.setCmd(EtherService.comand_map.get("SWEEP_ENABLE"));
                    m.setArg(0);
                    m.setMessage("");
                    etherservice.sendMessage(m);
                }
            }
        });

        sw_scanenable.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (sw_scanenable.isChecked()){
                    Log.d(Support3.TAG, "Sweep Scan Enable");

                    sw_enable.setChecked(true);
                    AcpMessage m0 = new AcpMessage(true, 2, 2,"");
                    m0.setCmd(EtherService.comand_map.get("SWEEP_ENABLE"));
                    m0.setArg(1);
                    m0.setMessage("");
                    etherservice.sendMessage(m0);

                    bt_goto.setEnabled(false);
                    bt_park.setEnabled(false);
                    bt_pulses.setEnabled(false);
                    String scanvel = String.valueOf(sp_scanvel.getSelectedItemPosition()+2);
                    AcpMessage m = new AcpMessage(true, 2, 2,"");
                    m.setCmd(EtherService.comand_map.get("SWEEP_SCAN"));
                    m.setArg(1);
                    m.setMessage(et_inner.getText().toString() + " " +
                                 et_outer.getText().toString() + " " +
                                 scanvel);
                    etherservice.sendMessage(m);
                }
                else{
                    Log.d(Support3.TAG, "Sweep Scan Disable");

                    sw_enable.setChecked(false);
                    AcpMessage m0 = new AcpMessage(true, 2, 2,"");
                    m0.setCmd(EtherService.comand_map.get("SWEEP_ENABLE"));
                    m0.setArg(0);
                    m0.setMessage("");
                    etherservice.sendMessage(m0);

                    bt_goto.setEnabled(true);
                    bt_park.setEnabled(true);
                    bt_pulses.setEnabled(true);
                    AcpMessage m = new AcpMessage(true, 2, 2,"");
                    m.setCmd(EtherService.comand_map.get("SWEEP_SCAN"));
                    m.setArg(0);
                    m.setMessage("");
                    etherservice.sendMessage(m);
                }
            }
        });

        bt_goto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(Support3.TAG, "Goto requested");
                AcpMessage m = new AcpMessage(true, 2, 2,"");
                m.setCmd(EtherService.comand_map.get("SWEEP_GOTO"));
                m.setArg(1);
                m.setMessage(et_goto.getText().toString() + " 2");
                etherservice.sendMessage(m);

            }
        });

        bt_park.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(Support3.TAG, "Park requested");
                AcpMessage m = new AcpMessage(true, 2, 2,"");
                m.setCmd(EtherService.comand_map.get("SWEEP_PARK"));
                m.setArg(0);
                m.setMessage("0");
                etherservice.sendMessage(m);
            }
        });

        bt_pulses.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(Support3.TAG, "Pulses requested");
                AcpMessage m = new AcpMessage(true, 2, 2,"");
                m.setCmd(EtherService.comand_map.get("SWEEP_PULSE"));
                m.setArg(0);
                m.setMessage(et_pulses.getText().toString() + " " + et_pulsesperiod.getText().toString());
                etherservice.sendMessage(m);
            }
        });

        return rootView;
    }

    public void updateUI() {
        boolean b = etherservice.getStatusFlag(0);
        cb_inner.setChecked(b);
        //Log.d(Support3.TAG, "Updating Sweep UI");
    }


    public void initWidgets(){
        sk_inner.setProgress(20);
        et_inner.setText("20");
        sk_outer.setProgress(80);
        et_outer.setText("80");
    }


    // ====
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (fromUser){
            if (seekBar.getId() == R.id.sk_sweep_inner){
                sk_value = seekBar.getProgress();
                sk_limit = sk_outer.getProgress();
                if (sk_value >= sk_limit){
                    sk_inner.setProgress(sk_limit -1);
                    sk_value = sk_limit-1;
                }
                et_inner.setText(String.valueOf(sk_value));
            }
            if (seekBar.getId() == R.id.sk_sweep_outer){
                sk_value = seekBar.getProgress();
                sk_limit = sk_inner.getProgress();
                if (sk_value <= sk_limit){
                    sk_outer.setProgress(sk_limit +1);
                    sk_value = sk_limit+1;
                }
                et_outer.setText(String.valueOf(sk_value));
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.sp_sweep_direction){
            Log.d(Support3.TAG, "Setting Sweep Direction" + position);
            AcpMessage m = new AcpMessage(true, 2, 2,"");
            m.setCmd(EtherService.comand_map.get("SWEEP_DIR"));
            m.setArg(position);
            m.setMessage("0");
            etherservice.sendMessage(m);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }


}
