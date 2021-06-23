package com.opus.virna7;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.opus.virna7.dummy.DummyContent;

import java.util.ArrayList;

/**
 * A fragment representing a single ProfileItem detail screen.
 * This fragment is either contained in a {@link ProfileItemListActivity}
 * in two-pane mode (on tablets) or a {@link ProfileItemDetailActivity}
 * on handsets.
 */
public class ProfileItemDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    private ProfileEntry profile;
    private Virna7Application virna;
    private ArrayList<ProfileFlatEntry> flatentries;


    public ProfileItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            //mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle("Profile Entyry : Root");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profileitem_detail, container, false);

        // Show the dummy content as text in a TextView.
        //if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.profileitem_detail)).setText("gfgfgfgf");
       // }

        return rootView;
    }
}
