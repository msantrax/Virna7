package com.opus.virna7;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
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
public class ProfileItemValueDetailFragment extends Fragment {

    private ProfileEntry profile;
    private Virna7Application virna;
    private ProfileValue value;
    private View rootView;

    //private TextView phasename;
    private Spinner valuetype;
    private EditText ettriggervalue;
    private TextView lbtriggervalue;
    private ArrayAdapter<CharSequence> spinneradapter;

    private ProfileItemListActivity leftlist;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static ProfileItemValueDetailFragment newInstance(int sectionNumber) {
        ProfileItemValueDetailFragment fragment = new ProfileItemValueDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileItemValueDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey( ARG_SECTION_NUMBER)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            //mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle("Profile Entry : Value");
            }
        }

        leftlist = (ProfileItemListActivity)getActivity();
    }

    private void updateUI(){

        int idx = spinneradapter.getPosition(value.getStringvalue());
        valuetype.setSelection(idx);
    }

    public void setValue(ProfileValue value) {
        this.value = value;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profileitem_detail_value, container, false);


        valuetype = (Spinner) rootView.findViewById(R.id.profiledetail_value_sp_valuetype);
        spinneradapter= ArrayAdapter.createFromResource(this.getActivity(),
                R.array.commands, android.R.layout.simple_spinner_item);
        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        valuetype.setAdapter(spinneradapter);


        valuetype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()  {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinneradapter.getItem(position).toString();
                value.setStringvalue(selected);
                leftlist.updateLeftList("   " + selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        return rootView;
    }
}
