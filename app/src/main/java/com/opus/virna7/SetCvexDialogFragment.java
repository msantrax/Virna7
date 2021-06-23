package com.opus.virna7;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SetCvexDialogFragment extends DialogFragment {

    private static String TAG = "SetSweep";
    private OnSetCvexFragmentInteractionListener mListener;
    private NumberPicker innernp;
    private NumberPicker outernp;
    private NumberPicker velnp;

    private TextView lb1, lb2,lb3;
    private CheckBox convex;

    private int algo;

    private static RunSimpleActivity acvty;

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private  List<String> listDataHeader;
    private static List<String> algolist;
    private List<String> algotypes;
    private HashMap<String, List<String>> listDataChild;

    private static SweepDescriptor sweepdescriptor;


    public SetCvexDialogFragment() {
        // Required empty public constructor
    }


    public static SetCvexDialogFragment newInstance(RunSimpleActivity ctx, SweepDescriptor swdesc) {

        SetCvexDialogFragment fragment = new SetCvexDialogFragment();
        acvty = ctx;
        sweepdescriptor = swdesc;

        algolist = new ArrayList<String>();
        algolist.add("Varredura Fixa");
        algolist.add("Algoritmo Alpha");
        algolist.add("Algoritmo Beta");
        algolist.add("Algoritmo Gamma");
        algolist.add("Posição Definida");
        algolist.add("Oscilante Desabilitado");
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
        View v = inflater.inflate(R.layout.fragment_set_cvex_dialog, null);


        builder.setView(v)
                .setIcon(R.drawable.clock1)
                .setTitle("Defina estratégia de varredura")
                .setPositiveButton("O.k",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                sweepdescriptor.setAlgo(algo);
                                sweepdescriptor.setConvex(convex.isChecked());
                                mListener.onSetCvexFragmentInteraction();
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

        lb1 = (TextView)v.findViewById(R.id.lb_interno);
        lb2 = (TextView)v.findViewById(R.id.lb_sweepvel);
        lb3 = (TextView)v.findViewById(R.id.lb_externo);
        convex = (CheckBox)v.findViewById(R.id.cb_convex);


        innernp = (NumberPicker)v.findViewById(R.id.np_interno);
        outernp = (NumberPicker)v.findViewById(R.id.np_externo);
        velnp = (NumberPicker)v.findViewById(R.id.np_sweepvel);

        //innernp.setWrapSelectorWheel(true);
        //outernp.setWrapSelectorWheel(true);
        //velnp.setWrapSelectorWheel(true);

        updateInner();
        updateOuter();

        velnp.setMinValue(2);
        velnp.setMaxValue(20);
        velnp.setValue(sweepdescriptor.getVel());

        convex.setChecked(sweepdescriptor.isConvex());

        // get the listview
        expListView = (ExpandableListView) v.findViewById(R.id.el_sweepalgo);
        algo=sweepdescriptor.getAlgo();
        prepareListData(algo);


        innernp.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Log.e(TAG, "Inner clicked" + newVal);
                if (algo == 0) {
                    sweepdescriptor.setInner(newVal);
                    updateOuter();
                }
                else if (algo == 5){
                    sweepdescriptor.setPosition(newVal);
                }
                else{
                    sweepdescriptor.setTarget(newVal);
                }
            }
        });

        outernp.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Log.e(TAG, "Inner clicked" + newVal);
                sweepdescriptor.setOuter(newVal);
                updateInner();
            }
        });

        velnp.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Log.e(TAG, "Inner clicked" + newVal);
                sweepdescriptor.setVel(newVal);
            }
        });

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener () {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id){
                algo = getAbsListItem(childPosition);
                Log.e(TAG, "Algo = " + algo);
                expListView.collapseGroup(groupPosition);
                prepareListData(algo);
                return true;
            }
        });


        return builder.create();
    }

    private void updateInner(){
        innernp.setValue(sweepdescriptor.getInner());
        innernp.setMinValue(1);
        innernp.setMaxValue(sweepdescriptor.getOuter() -1);
    }

    private void updateOuter(){
        outernp.setValue(sweepdescriptor.getOuter());
        outernp.setMinValue(sweepdescriptor.getInner() + 1);
        outernp.setMaxValue(100);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public void addListener(Context context){
        if (context instanceof OnSetCvexFragmentInteractionListener) {
            mListener = (OnSetCvexFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnSetCvexFragmentInteractionListener {
        void onSetCvexFragmentInteraction();
    }

    private int getAbsListItem(int item){
        String sitem = algotypes.get(item);
        return algolist.indexOf(sitem);
    }


    private void prepareListData(int type) {

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        algotypes = new ArrayList<String>();

        for (int i = 0; i < 6; i++) {
            if (type != i) {
                algotypes.add(algolist.get(i));
            }
        }

        listDataHeader.add(algolist.get(type));

        listDataChild.put(listDataHeader.get(0), algotypes);

        listAdapter = new ExpandableListAdapter(acvty, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);

//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
//                RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

        if (type==0) {
            outernp.setVisibility(View.VISIBLE);
            velnp.setVisibility(View.VISIBLE);
            lb3.setVisibility(View.VISIBLE);
            //layoutParams.setMargins(100, 0, 0, 0);
            //lb2.setLayoutParams(layoutParams);
            lb2.setText("Velocidade");
            lb1.setText("Interno");
            updateInner();
            updateOuter();
            convex.setVisibility(View.INVISIBLE);
            convex.setChecked(sweepdescriptor.isConvex());
        }
        else if (type==5){
            outernp.setVisibility(View.INVISIBLE);
            velnp.setVisibility(View.INVISIBLE);
            lb3.setVisibility(View.INVISIBLE);
            //layoutParams.setMargins(100, 100, 0, 0);
            //lb2.setLayoutParams(layoutParams);
            lb2.setText("% do Centro");
            lb1.setText("Posição");
            innernp.setMinValue(0);
            innernp.setMaxValue(100);
            innernp.setValue(sweepdescriptor.getPosition());
            convex.setVisibility(View.INVISIBLE);
        }
        else  {
            outernp.setVisibility(View.INVISIBLE);
            velnp.setVisibility(View.INVISIBLE);
            lb3.setVisibility(View.INVISIBLE);
            //layoutParams.setMargins(100, 100, 0, 0);
            //lb2.setLayoutParams(layoutParams);
            lb2.setText("Nanometros");
            lb1.setText("META EM :");
            innernp.setMinValue(0);
            innernp.setMaxValue(10000);
            innernp.setValue(sweepdescriptor.getTarget());
            convex.setVisibility(View.VISIBLE);
        }
    }
}


