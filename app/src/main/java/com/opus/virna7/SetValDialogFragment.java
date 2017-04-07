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
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.TextView;


public class SetValDialogFragment extends DialogFragment {

    private static String TAG = "SetVal";
    private static SetValDescriptor descriptor;
    private OnSetValFragmentInteractionListener mListener;
    private NumberPicker np;
    private CheckBox direction;

    public SetValDialogFragment() {
        // Required empty public constructor
    }


    public static SetValDialogFragment newInstance(SetValDescriptor ldescriptor) {
        SetValDialogFragment fragment = new SetValDialogFragment();
        descriptor=ldescriptor;
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
        View v = inflater.inflate(R.layout.fragment_set_val_dialog, null);


        builder.setView(v)
                .setIcon(descriptor.getIcon())
                .setTitle(descriptor.getTitle())
                .setPositiveButton("O.k",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //((FragmentAlertDialog)getActivity()).doPositiveClick();
                                Log.d(TAG, "Dialog ok");
                                mListener.onSetValFragmentInteraction(descriptor);
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //((FragmentAlertDialog)getActivity()).doNegativeClick();
                                Log.d(TAG, "Dialog cancel");
                            }
                        }
                );

        np = (NumberPicker)v.findViewById(R.id.np);
        final TextView tvprompt = (TextView) v.findViewById(R.id.tv_prompt);
        final TextView tvunit = (TextView) v.findViewById(R.id.tv_unit);

        direction = (CheckBox)v.findViewById(R.id.cb_direction);
        if (descriptor.getDirection() != 0) {
            direction.setVisibility(View.VISIBLE);
        }

        tvprompt.setText(descriptor.getPrompt());
        tvunit.setText(descriptor.getUnit());

        np.setMinValue(descriptor.getMin());
        np.setMaxValue(descriptor.getMax());
        np.setValue(descriptor.getValue());

        //Gets whether the selector wheel wraps when reaching the min/max value.
        np.setWrapSelectorWheel(true);

        //Set a value change listener for NumberPicker
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Display the newly selected number from picker
                //tvunit.setText(newVal + descriptor.getTitle());
                descriptor.setValue(newVal);
            }
        });

        direction.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton bt, boolean checked){
                if (checked){
                    descriptor.setDirection(1);
                }
                else{
                    descriptor.setDirection(0);
                }
            }
        });

        return builder.create();
    }

    @Override
    public void onResume() {

        super.onResume();

    }

    public void addListener(Context context){
        if (context instanceof OnSetValFragmentInteractionListener) {
            mListener = (OnSetValFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnSetValFragmentInteractionListener {
        void onSetValFragmentInteraction(SetValDescriptor descriptor);
    }

}



//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnSetValFragmentInteractionListener) {
//            mListener = (OnSetValFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_set_val_dialog, container, false);
//        return view;
//    }

