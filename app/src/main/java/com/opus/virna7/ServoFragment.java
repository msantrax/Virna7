package com.opus.virna7;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by opus on 03/03/17.
 */
public class ServoFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String TAG = "SERVOFRAG";

    private EtherService etherservice;
    private SharedPreferences preferences ;

    private SeekBar sk_pwm;
    private SeekBar sk_vel;
    private EditText et_pwm;
    private EditText et_vel;
    private Switch sw_pwm;
    private Switch sw_vel;

    private Switch sw_servoenable;

    private Button bt_loadparam;
    private Button bt_reset;

    private TextView tv_servovel;
    private TextView tv_servogamma;


    public ServoFragment() {
    }


    public static ServoFragment newInstance(int sectionNumber) {
        ServoFragment fragment = new ServoFragment();
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

        View rootView = inflater.inflate(R.layout.servo_fragment, container, false);

        sk_pwm = (SeekBar) rootView.findViewById(R.id.sk_pwm);
        et_pwm = (EditText) rootView.findViewById(R.id.et_mon_wg20vel);
        sw_pwm = (Switch) rootView.findViewById(R.id.sw_mon_wg20enable);

        sk_vel = (SeekBar) rootView.findViewById(R.id.sk_servovel);
        et_vel = (EditText) rootView.findViewById(R.id.et_servovel);
        sw_vel = (Switch) rootView.findViewById(R.id.sw_servovel);

        bt_loadparam = (Button) rootView.findViewById(R.id.bt_loadParam);
        bt_reset = (Button) rootView.findViewById(R.id.bt_reset);

        sw_servoenable = (Switch) rootView.findViewById(R.id.sw_servo_enable);


        sk_pwm.setOnSeekBarChangeListener(this);
        sk_vel.setOnSeekBarChangeListener(this);

        tv_servovel = (TextView) rootView.findViewById(R.id.tv_servo_vel);
        tv_servogamma = (TextView) rootView.findViewById(R.id.tv_servo_gamma);


        // Seek bars
        et_pwm.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int t_value;
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    t_value = Integer.valueOf(et_pwm.getText().toString());
                    if (t_value > 100){
                        t_value = 100;
                        et_pwm.setText(String.valueOf(t_value));
                    }
                    sk_pwm.setProgress(t_value);
                    return true;
                }
                return false;
            }
        });

        et_vel.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int t_value;
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    t_value = Integer.valueOf(et_vel.getText().toString());
                    if (t_value > 100){
                        t_value = 100;
                        et_vel.setText(String.valueOf(t_value));
                    }
                    sk_vel.setProgress(t_value);
                    return true;
                }
                return false;
            }
        });

        sw_vel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (sw_vel.isChecked()){

                    sk_pwm.setEnabled(false);
                    et_pwm.setEnabled(false);
                    sw_pwm.setEnabled(false);

                    AcpMessage m0 = new AcpMessage(true, 3, 3,"");
                    m0.setCmd(EtherService.comand_map.get("SERVO_ENABLE"));
                    m0.setArg(1);
                    m0.setMessage("0");
                    etherservice.sendMessage(m0);

                    bt_loadparam.setEnabled(false);
                    AcpMessage m = new AcpMessage(true, 3, 3,"");
                    m.setCmd(EtherService.comand_map.get("SERVO_SETSPEED"));
                    m.setArg(1);
                    m.setMessage(et_vel.getText().toString());
                    etherservice.sendMessage(m);
                }
                else{
                    sk_pwm.setEnabled(true);
                    et_pwm.setEnabled(true);
                    sw_pwm.setEnabled(true);

                    AcpMessage m0 = new AcpMessage(true, 3, 3,"");
                    m0.setCmd(EtherService.comand_map.get("SERVO_ENABLE"));
                    m0.setArg(0);
                    m0.setMessage("0");
                    etherservice.sendMessage(m0);

                    bt_loadparam.setEnabled(true);
                    AcpMessage m = new AcpMessage(true, 3, 3,"");
                    m.setCmd(EtherService.comand_map.get("SERVO_SETSPEED"));
                    m.setArg(0);
                    m.setMessage("");
                    etherservice.sendMessage(m);
                }
            }
        });


        sw_pwm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (sw_pwm.isChecked()){
                    sk_vel.setEnabled(false);
                    et_vel.setEnabled(false);
                    sw_vel.setEnabled(false);
                    bt_loadparam.setEnabled(false);

                    sw_servoenable.setChecked(true);
                    AcpMessage m0 = new AcpMessage(true, 3, 3,"");
                    m0.setCmd(EtherService.comand_map.get("SERVO_ENABLE"));
                    m0.setArg(1);
                    m0.setMessage("0");
                    etherservice.sendMessage(m0);

                    AcpMessage m = new AcpMessage(true, 3, 3,"");
                    m.setCmd(EtherService.comand_map.get("SERVO_SETPWM"));
                    m.setArg(1);
                    m.setMessage(et_pwm.getText().toString());
                    etherservice.sendMessage(m);
                }
                else{
                    sk_vel.setEnabled(true);
                    et_vel.setEnabled(true);
                    sw_vel.setEnabled(true);
                    bt_loadparam.setEnabled(true);

                    sw_servoenable.setChecked(false);
                    AcpMessage m0 = new AcpMessage(true, 3, 3,"");
                    m0.setCmd(EtherService.comand_map.get("SERVO_ENABLE"));
                    m0.setArg(0);
                    m0.setMessage("0");
                    etherservice.sendMessage(m0);

                    AcpMessage m = new AcpMessage(true, 3, 3,"");
                    m.setCmd(EtherService.comand_map.get("SERVO_SETPWM"));
                    m.setArg(0);
                    m.setMessage("0");
                    etherservice.sendMessage(m);
                }
            }
        });

        sw_servoenable.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (sw_servoenable.isChecked()){
                    //Log.d(Support3.TAG, "Servo Enable");
                    AcpMessage m = new AcpMessage(true, 3, 3,"");
                    m.setCmd(EtherService.comand_map.get("SERVO_ENABLE"));
                    m.setArg(1);
                    m.setMessage("0");
                    etherservice.sendMessage(m);
                }
                else{
                    //Log.d(Support3.TAG, "Servo Disable");
                    AcpMessage m = new AcpMessage(true, 3, 3,"");
                    m.setCmd(EtherService.comand_map.get("SERVO_ENABLE"));
                    m.setArg(0);
                    m.setMessage("0");
                    etherservice.sendMessage(m);
                }
            }
        });


        bt_loadparam.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(Support3.TAG, "Load Param");
            }
        });

        bt_reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(Support3.TAG, "Reset");
                AcpMessage m = new AcpMessage(true, 3, 3,"");
                m.setCmd(EtherService.comand_map.get("SERVO_ENABLE"));
                m.setArg(10);
                m.setMessage("0");
                etherservice.sendMessage(m);


            }
        });

        return rootView;
    }


    public void initWidgets(){

    }

    public void updateUI() {

        double val;

        val = etherservice.getStatusSlot(0);
        tv_servovel.setText(String.format(Locale.ENGLISH, "%5.2f", val));

        val = etherservice.getStatusSlot(1);
        tv_servogamma.setText(String.format(Locale.ENGLISH, "%5.2f", val));

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        String ptemp;
        if (fromUser){
            if (seekBar.getId() ==R.id.sk_pwm){
                ptemp = String.valueOf(seekBar.getProgress());
                et_pwm.setText(ptemp);
                if (sw_pwm.isChecked()){
                    AcpMessage m = new AcpMessage(true, 3, 3,"");
                    m.setCmd(EtherService.comand_map.get("SERVO_SETPWM"));
                    m.setArg(1);
                    m.setMessage(ptemp);
                    etherservice.sendMessage(m);
                }
            }
            if (seekBar.getId() == R.id.sk_servovel) {
                ptemp = String.valueOf(seekBar.getProgress());
                et_vel.setText(ptemp);
                if (sw_vel.isChecked()){
                    AcpMessage m = new AcpMessage(true, 3, 3,"");
                    m.setCmd(EtherService.comand_map.get("SERVO_SETSPEED"));
                    m.setArg(1);
                    m.setMessage(ptemp);
                    etherservice.sendMessage(m);
                }
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }

}
