package com.opus.virna7;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A fragment representing a single ProfileItem detail screen.
 * This fragment is either contained in a {@link ProfileItemListActivity}
 * in two-pane mode (on tablets) or a {@link ProfileItemDetailActivity}
 * on handsets.
 */
public class ProfileItemPhaseDetailFragment extends Fragment {

    private ProfileEntry profile;
    private Virna7Application virna;
    private ProfilePhase phase;
    private View rootView;

    private TextView phasename;
    private Spinner triggertype;
    private EditText ettriggervalue;
    private TextView lbtriggervalue;
    private ArrayAdapter<CharSequence> spinneradapter;

    private ProfileItemListActivity leftlist;


    private static final String ARG_SECTION_NUMBER = "section_number";

    public static ProfileItemPhaseDetailFragment newInstance(int sectionNumber) {
        ProfileItemPhaseDetailFragment fragment = new ProfileItemPhaseDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    public ProfileItemPhaseDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_SECTION_NUMBER)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            //mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle("Profile Entyry : Phase");
            }
        }

        leftlist = (ProfileItemListActivity)getActivity();

    }

    private void updateUI(){
        phasename.setText(phase.getPhasename());
        int idx = spinneradapter.getPosition(phase.getTriggername());
        triggertype.setSelection(idx);
    }

    public void setPhase(ProfilePhase phase) {
        this.phase = phase;
        if (rootView != null){
            updateUI();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        if (rootView != null){
            updateUI();
        }
    }

    private void hideValues(boolean hide){



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.profileitem_detail_phase, container, false);
        phasename = (TextView)rootView.findViewById(R.id.profiledetail_phase_tv_name);
        triggertype = (Spinner) rootView.findViewById(R.id.profiledetail_phase_sp_triggertype);
        ettriggervalue = (EditText) rootView.findViewById(R.id.profiledetail_phase_et_triggervalue);
        lbtriggervalue = (TextView) rootView.findViewById(R.id.profiledetail_phase_lb_triggervalue);


        spinneradapter= ArrayAdapter.createFromResource(this.getActivity(),
                R.array.phasetriggers, android.R.layout.simple_spinner_item);
        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        triggertype.setAdapter(spinneradapter);


        updateUI();

        phasename.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String sk_value = phasename.getText().toString();
                    phase.setPhasename(sk_value);
                    leftlist.updateLeftList("      " + sk_value);
                    return true;
                }
                return false;
            }
        });

        triggertype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()  {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinneradapter.getItem(position).toString();
                if (selected.equals(spinneradapter.getItem(0))){
                    ettriggervalue.setVisibility(View.GONE);
                    lbtriggervalue.setVisibility(View.GONE);
                    phase.setTriggervalue(0.0);
                }
                else{
                    ettriggervalue.setVisibility(View.VISIBLE);
                    lbtriggervalue.setVisibility(View.VISIBLE);
                }

                if (selected.equals(spinneradapter.getItem(1))){
                    ettriggervalue.setText("5");
                    phase.setTriggervalue(5.0);
                }

                phase.setTriggername(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ettriggervalue.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String sk_value = ettriggervalue.getText().toString();
                    phase.setTriggervalue(Double.parseDouble(sk_value));
                    return true;
                }
                return false;
            }
        });


        return rootView;
    }
}
