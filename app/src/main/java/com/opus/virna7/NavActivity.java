package com.opus.virna7;

import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;

import butterknife.ButterKnife;

public class NavActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener {

    private CanvasCoordinator canvas_coordinator;
    private boolean exploded = false;

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        ButterKnife.bind(this);

        GridLayout grid = (GridLayout) findViewById(R.id.canvasgrid);
        canvas_coordinator = new CanvasCoordinator(grid);


        FragmentManager fragmentManager = getFragmentManager();
        canvas_coordinator.setFragmentManager(fragmentManager);

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



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //canvas_coordinator.setExclude_widget(1);
        canvas_coordinator.setCanvas(true);
        canvas_coordinator.onAnimationEnd(new ObjectAnimator());
        exploded = true;
    }

    public void CanvasClicked(View v) {

        int button_index = canvas_coordinator.getButtonIndex(v.getId());

        if (button_index == 2 && exploded){
            startActivity(new Intent("com.opus.virna7.SettingsActivity"));
            return;
        }
        else if (button_index == 3 && exploded){
            startActivity(new Intent("com.opus.virna7.Support3"));
            return;
        }
        else if (button_index == 1 && exploded){
            startActivity(new Intent("com.opus.virna7.RunActivity"));
            return;
        }



        else if ( button_index!= -1){
            if (exploded) {
                canvas_coordinator.setExclude_widget(button_index);
                canvas_coordinator.clearStates();

                canvas_coordinator.parkCanvas(true);
                canvas_coordinator.setCanvas(false);

                canvas_coordinator.onAnimationEnd(new ObjectAnimator());

                exploded = false;
            }
            else{
                canvas_coordinator.setExclude_widget(button_index);
                canvas_coordinator.clearStates();

                canvas_coordinator.setCanvas(true);
                canvas_coordinator.parkCanvas(false);

                canvas_coordinator.onAnimationEnd(new ObjectAnimator());
                exploded = true;
                //canvas_coordinator.setExclude_widget(-1);
            }
        }

        Log.i("L17", "Widget Clicked : " + v.getId());

    }


    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

        Log.i("L17", "list Clicked : ");

    }
}
