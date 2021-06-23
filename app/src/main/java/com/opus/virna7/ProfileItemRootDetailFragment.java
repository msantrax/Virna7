package com.opus.virna7;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A fragment representing a single ProfileItem detail screen.
 * This fragment is either contained in a {@link ProfileItemListActivity}
 * in two-pane mode (on tablets) or a {@link ProfileItemDetailActivity}
 * on handsets.
 */
public class ProfileItemRootDetailFragment extends Fragment {

    private ProfileEntry profile;
    private Virna7Application virna;
    private ProfileEntry entry;
    private View rootView;

    private TextView entryname;
    private TextView creation;

    private Spinner user;

    private ProfileItemListActivity leftlist;


    private static final String ARG_SECTION_NUMBER = "section_number";

    public static ProfileItemRootDetailFragment newInstance(int sectionNumber) {
        ProfileItemRootDetailFragment fragment = new ProfileItemRootDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileItemRootDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if (getArguments().containsKey(ARG_SECTION_NUMBER)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            //mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle("Perfil de Atividade");
            }
            leftlist = (ProfileItemListActivity)getActivity();
       // }
    }

    private void updateUI(){
        if (entry != null) {
            entryname.setText(entry.getName());
            creation.setText("21/05/2017 02:37:24");
        }
    }

    public void setEntry(ProfileEntry entry) {
        this.entry = entry;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.profileitem_detail_root, container, false);

        entryname = (TextView)rootView.findViewById(R.id.profileitemroot_tv_name);
        creation = (TextView)rootView.findViewById(R.id.profiledetail_root_tv_creation);
        user = (Spinner)rootView.findViewById(R.id.profiledetail_root_sp_user);

        entryname.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String sk_value = entryname.getText().toString();
                    entry.setName(sk_value);
                    leftlist.updateLeftList(sk_value);
                    return true;
                }
                return false;
            }
        });


        return rootView;
    }
}
