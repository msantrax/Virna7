package com.opus.virna7;

import android.animation.ObjectAnimator;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;


public class UserDialogFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String dlgUserName;
    private boolean admuser;

    //private OnFragmentInteractionListener mListener;

    public UserDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserDialogFragment newInstance(String param1, boolean param2) {
        UserDialogFragment fragment = new UserDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putBoolean(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dlgUserName = getArguments().getString(ARG_PARAM1);
            admuser = getArguments().getBoolean(ARG_PARAM2);
        }
        //setStyle (DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_DeviceDefault);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String text;

        View v =  inflater.inflate(R.layout.fragment_user_dialog, container, false);
        ImageView fotov = (ImageView)v.findViewById(R.id.typeicon);

        Resources res = getResources();
        if (admuser) {
            text = String.format(res.getString(R.string.dlgmsg_admuser), dlgUserName);
            ImageView badgev = (ImageView)v.findViewById(R.id.badge);
            fotov.setImageDrawable(res.getDrawable(R.drawable.raso1));

            ObjectAnimator openbadge = ObjectAnimator.ofFloat(badgev, "ScaleY", 0f, 1f);
            openbadge.setDuration(1000);
            openbadge.setInterpolator(new BounceInterpolator());
            openbadge.setStartDelay(1000);
            openbadge.start();
        }
        else{
            text = String.format(res.getString(R.string.dlgmsg_guestuser), "");
            fotov.setImageDrawable(res.getDrawable(R.drawable.guestuser));
        }
        CharSequence styledText = Html.fromHtml(text);


        View tv = v.findViewById(R.id.message);
        ((TextView)tv).setText(styledText);

        return v;
    }






//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
