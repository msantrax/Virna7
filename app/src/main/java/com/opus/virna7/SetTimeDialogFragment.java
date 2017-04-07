package com.opus.virna7;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.GregorianCalendar;


public class SetTimeDialogFragment extends DialogFragment {

    private static String TAG = "SetTime";

    private OnSetTimeFragmentInteractionListener mListener;

    private TextView presenttime;
    private TextView runtime;
    private TimePicker tp;
    private CheckBox cbox;

    private GregorianCalendar gc = new GregorianCalendar();
    private long startmillis;
    private long finalmillis;
    private static final long miliminute = 60000;
    private static final long milihour = miliminute * 60;

    private static boolean userelative;
    private static int period;

    public SetTimeDialogFragment() {
        // Required empty public constructor
    }


    public static  SetTimeDialogFragment newInstance(boolean ur, int p) {
        SetTimeDialogFragment fragment = new  SetTimeDialogFragment();
        userelative = ur;
        period = p;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_set_time_dialog, null);

        tp = (TimePicker) v.findViewById(R.id.timePicker);
        tp.setIs24HourView(true);
        presenttime = (TextView) v.findViewById(R.id.tv_presenttime);
        runtime = (TextView) v.findViewById(R.id.tv_dur);
        cbox = (CheckBox) v.findViewById(R.id.userelativetime);

        setMode(userelative);

        builder.setView(v)
                .setIcon(R.drawable.clock1)
                .setTitle("Ajuste Tempos")
                .setPositiveButton("O.k",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //((FragmentAlertDialog)getActivity()).doPositiveClick();
                                //Log.d(TAG, "Dialog ok");
                                mListener.onSetTimeFragmentInteraction(finalmillis, userelative);
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //((FragmentAlertDialog)getActivity()).doNegativeClick();
                                //Log.d(TAG, "Dialog cancel");
                            }
                        }
                );

        //Set a value change listener for NumberPicker
        tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker picker, int hour, int minute){
                //Log.d(TAG, "Time changed " + hour + " - " + minute );
                //gc.set(GregorianCalendar.HOUR, hour);
                //gc.set(GregorianCalendar.MINUTE, minute);
                setElapseTime(hour, minute);
            }
        });

        cbox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton bt, boolean checked){
                //Log.d(TAG, "Use relative = " + checked );
                setMode(checked);
            }
        });

        return builder.create();
    }

    public void setPeriod(int period) { this.period = period;}

    public void setMode(boolean mode){

        userelative = mode;

        if (userelative){
            presenttime.setText("Defina o tempo de duração da atividade");
            tp.setCurrentHour(0);
            tp.setCurrentMinute(period);
            cbox.setChecked(true);
        }
        else{
            //startmillis = System.currentTimeMillis();
            String pt = String.format("Horario atual: %1$tH:%1$tM:%1$tS",System.currentTimeMillis());
            presenttime.setText(pt);
            cbox.setChecked(false);
        }
        setElapseTime(0, 0);
    }


    public void setElapseTime(int hour, int min){

        long mdif;

        gc = new GregorianCalendar();
        gc.set(GregorianCalendar.SECOND,0);

        if (userelative){
            finalmillis = (hour * milihour) + (min * miliminute);
            mdif = finalmillis + 10800000 ;
        }
        else{
            String pt = String.format("Horario atual: %1$tH:%1$tM:%1$tS",System.currentTimeMillis());
            presenttime.setText(pt);
            gc.set(GregorianCalendar.HOUR_OF_DAY, hour);
            gc.set(GregorianCalendar.MINUTE, min);
            finalmillis = gc.getTimeInMillis();
            if (finalmillis < System.currentTimeMillis()){
                finalmillis = System.currentTimeMillis();
            }
            mdif = (finalmillis - System.currentTimeMillis()) + 10800000; //  - 43200000 ;
        }

        String sdif = String.format("%1$tH:%1$tM:%1$tS", mdif);
        //Log.d(TAG, String.format("GC=%1$tD -- %1$tT", mdif));
        runtime.setText(sdif);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void addListener(Context context){

        if (context instanceof OnSetTimeFragmentInteractionListener) {
            mListener = (OnSetTimeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnSetTimeFragmentInteractionListener {
        void onSetTimeFragmentInteraction(long time, boolean userelative);
    }

}

