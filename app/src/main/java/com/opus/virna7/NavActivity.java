package com.opus.virna7;

import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class NavActivity extends AppCompatActivity {

    public static final String TAG = "NAV----";

    private CanvasCoordinator canvas_coordinator;
    private boolean exploded = false;
    private Virna7Application virna;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        ButterKnife.bind(this);

        canvas_coordinator = new CanvasCoordinator(this);

//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

//    @Override
//    public void onBackPressed(){
//        Log.i("LTAG", "Moving to back");
////        if (exploded){
////            canvas_coordinator.setCanvas(false);
////            exploded=false;
////        }
////        else{
////            canvas_coordinator.setCanvas(true);
////            exploded=true;
////        }
////        canvas_coordinator.onAnimationEnd(new ObjectAnimator());
//
//        moveTaskToBack(false);
//    }


    public void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming Nav Canvas...");

    }

    public void onPause() {
        super.onPause();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //canvas_coordinator.setExclude_widget(1);
        canvas_coordinator.clearStates();
        canvas_coordinator.startCanvas();
        canvas_coordinator.onAnimationEnd(new ObjectAnimator());
        exploded = true;
    }

    private void setCanvas (boolean restore, int btindex){

        if (restore){
            canvas_coordinator.setExclude_widget(btindex);
            canvas_coordinator.clearStates();
            canvas_coordinator.setCanvas(true);
            canvas_coordinator.onAnimationEnd(new ObjectAnimator());
            exploded = true;
        }
        else{
            canvas_coordinator.setExclude_widget(btindex);
            canvas_coordinator.clearStates();
            canvas_coordinator.setCanvas(false);
            canvas_coordinator.onAnimationEnd(new ObjectAnimator());
            exploded = false;
        }
    }

    public void choiceClicked(ProfileEntry profile, int button){

        Log.i(TAG, "Acionando run com :" + profile.getName());
        setCanvas (true, button);
        Intent i = new Intent("com.opus.virna7.RunActivity");
        i.putExtra("profile", profile.getName());
        startActivity(i);


    }


    public void CanvasClicked(View v) {

        //startActivity(new Intent("com.opus.virna7.RunActivity"));

        int button_index = canvas_coordinator.getButtonIndex(v.getId());

        if (button_index == 2 && exploded){
            startActivity(new Intent("com.opus.virna7.SettingsActivity"));
        }
        else if (button_index == 3 && exploded){
            startActivity(new Intent("com.opus.virna7.Support3"));
        }
        else if (button_index == 0 ){

//            Intent i = new Intent("com.opus.virna7.ProfileItemListActivity");
//            i.putExtra("profile", "ACP Default 2");
//            startActivity(i);

            if (exploded) {
                canvas_coordinator.configChoicePanel(CanvasCoordinator.paneltypes.OPERATIONSPANEL);
                setCanvas(false, 0);
            }
            else{
                setCanvas(true, 0);
            }
        }
        else if (button_index == 1 ){
            if (exploded) {
                canvas_coordinator.configChoicePanel(CanvasCoordinator.paneltypes.EDITPANEL);
                setCanvas(false, 1);
            }
            else{
                setCanvas(true, 1);
            }
        }

    }

}
