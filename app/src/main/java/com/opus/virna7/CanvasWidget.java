package com.opus.virna7;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;

/**
 * Created by opus on 12/08/16.
 */

public class CanvasWidget {

    private int wdg_id;
    private int index;

    private View widget;
    private boolean button;

    // movement offsets
    private float colapse_x;
    private float colapse_y;

    private float park_x;
    private float unpark_x;
    private float park_y;

    private int colapse_duration;
    private int explode_duration;
    private TimeInterpolator colapse_interpolator;
    private TimeInterpolator explode_interpolator;


    private static float[] colapse_x_values = {420f,210f,0f,420f,210f};
    private static float[] colapse_y_values = {175f,175f,175f,0f,0f};

    private static float[] park_x_values = {-10f,-210f,-420f,    -10f, -210f};
    private static float[] upark_x_values = {-20f,-210f, 420f,    -20f, -210f};

    private static float[] park_y_values = {87f,87f,87f,  -87f, -87f};

    private static int[] colapse_durations = {1000,1000,1000,1000,1000};
    private static int[] explode_durations = {2000,2000,2000,2000,2000};


    public CanvasWidget(int index, View view, boolean button){
        widget=view;
        this.button = button;
        this.index=index;
        wdg_id=view.getId();
        colapse_x=colapse_x_values[index];
        colapse_y=colapse_y_values[index];
        park_x=park_x_values[index];
        unpark_x=upark_x_values[index];
        park_y=park_y_values[index];

        colapse_duration = colapse_durations[index];
        explode_duration = explode_durations[index];

        explode_interpolator= new BounceInterpolator();
        colapse_interpolator= new LinearInterpolator();

    }

    public ArrayList<ObjectAnimator> getColapseAnimator (boolean colapse){

        ArrayList<ObjectAnimator> animations = new ArrayList<>();
        ObjectAnimator anim;

        if(colapse) {
            if (colapse_x !=0) {
                anim = ObjectAnimator.ofFloat(widget, "translationX", 0f, colapse_x);
                anim.setInterpolator(colapse_interpolator);
                anim.setDuration(colapse_duration);
                animations.add(anim);
            }
            if (colapse_y !=0) {
                anim = ObjectAnimator.ofFloat(widget, "translationY", 0f, colapse_y);
                anim.setInterpolator(colapse_interpolator);
                anim.setDuration(colapse_duration);
                animations.add(anim);
            }
        }
        else{
            if (colapse_x !=0) {
                anim = ObjectAnimator.ofFloat(widget, "translationX", colapse_x, 0f);
                anim.setInterpolator(explode_interpolator);
                anim.setDuration(explode_duration);
                animations.add(anim);
            }
            if (colapse_y !=0) {
                anim = ObjectAnimator.ofFloat(widget, "translationY", colapse_y, 0f);
                anim.setInterpolator(explode_interpolator);
                anim.setDuration(explode_duration);
                animations.add(anim);
            }
        }
        return animations;
    }

    public ArrayList<ObjectAnimator> getParkAnimator(boolean park) {

        ArrayList<ObjectAnimator> animations = new ArrayList<>();
        ObjectAnimator anim;

        if(park) {
            anim = ObjectAnimator.ofFloat(widget, "translationY", 0f, park_y);
            //anim.setInterpolator(colapse_interpolator);
            anim.setDuration(500);
            animations.add(anim);

            anim = ObjectAnimator.ofFloat(widget, "translationX", 0f, park_x);
            //anim.setInterpolator(colapse_interpolator);
            anim.setStartDelay(500);
            anim.setDuration(500);
            animations.add(anim);
        }
        else{
            anim = ObjectAnimator.ofFloat(widget, "translationX", park_x, 0f);
            //anim.setInterpolator(explode_interpolator);
            anim.setDuration(500);
            animations.add(anim);


            anim = ObjectAnimator.ofFloat(widget, "translationY", park_y, 0f);
            //anim.setInterpolator(explode_interpolator);
            anim.setStartDelay(500);
            anim.setDuration(500);
            animations.add(anim);

        }
        return animations;
    }



    public ObjectAnimator getFadeLabelAnimator (boolean fade){

        ObjectAnimator anim;

        if(fade) {
            anim = ObjectAnimator.ofFloat(widget, "Alpha", 1f, 0f);
        }
        else{
            anim = ObjectAnimator.ofFloat(widget, "Alpha", 0f, 1f);
        }

        anim.setDuration(500);
        return anim;
    }

    public View getWidget() {
        return widget;
    }

    public int getWdg_id() {
        return wdg_id;
    }

    public int getIndex() {
        return index;
    }


}
