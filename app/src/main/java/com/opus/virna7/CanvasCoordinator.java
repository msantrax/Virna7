package com.opus.virna7;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by opus on 13/08/16.
 */

public class CanvasCoordinator implements Animator.AnimatorListener{

    private ArrayList<CanvasWidget> buttons;
    private ArrayList<CanvasWidget> labels;



    private enum animation_states {NONE, FADELABELS, SHOWLABELS, EXPLODE, COLAPSE,
                        SHOWBUTTONS, HIDEBUTTONS, PARKBUTTON, UNPARKBUTTON,
                        SHOWCHOICE, HIDECHOICE}
    private Stack<animation_states> animation_stack;

    FragmentManager fragmentManager;
    boolean choice_visible;

    private int exclude_widget;
    private ItemFragment itemfragment;


    public CanvasCoordinator(GridLayout grid) {

        buttons=new ArrayList<>();
        labels=new ArrayList<>();
        int bt_ptr = 0;
        int lb_ptr = 0;
        animation_stack= new Stack<>();
        exclude_widget=-1;
        choice_visible = false;

        for (int i = 0; i < grid.getChildCount(); i++) {
            View widget = (View) grid.getChildAt(i);

            if (widget instanceof android.widget.ImageButton) {
                CanvasWidget cw = new CanvasWidget(bt_ptr++,widget, true);
                buttons.add(cw);
                //Log.i("L17", "Loading Button @" + bt_ptr + " = " + widget.getId());
            }
            else if (widget instanceof android.widget.TextView){
                CanvasWidget cw = new CanvasWidget(lb_ptr++,widget, true);
                labels.add(cw);
                //Log.i("L17", "Loading Label  @" + lb_ptr + " = " + widget.getId());
            }
        }

        itemfragment = ItemFragment.newInstance(1);

    }

    public void setFragmentManager(FragmentManager fm) { fragmentManager = fm;}


    public int getButtonIndex(int id){

        for(CanvasWidget cw : buttons){
            if(cw.getWdg_id() == id) return cw.getIndex();
        }
        return -1;
    }


    public void clearStates() {animation_stack.clear();}


    public void showChoiceFragment(boolean show){

        if (show){
            if (!choice_visible) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                itemfragment.setIndex(exclude_widget);
                
                fragmentTransaction.add(R.id.ui_container, itemfragment);
//                fragmentTransaction.setCustomAnimations(R.animator.slide_in_left,
//                        R.animator.slide_out_right);
//                fragmentTransaction.
                fragmentTransaction.commit();
                choice_visible = true;
            }
        }
        else{
            if (choice_visible) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment fragment = fragmentManager.findFragmentById(R.id.ui_container);
                fragmentTransaction.remove(fragment);
                fragmentTransaction.commit();
                choice_visible = false;
            }
        }
        onAnimationEnd(new ObjectAnimator());

    }


    public void setCanvas(boolean operational){

        if(operational){
            animation_stack.push(animation_states.SHOWLABELS);
            animation_stack.push(animation_states.EXPLODE);
            animation_stack.push(animation_states.SHOWBUTTONS);
            animation_stack.push(animation_states.HIDECHOICE);

        }
        else{
            animation_stack.push(animation_states.SHOWCHOICE);
            animation_stack.push(animation_states.HIDEBUTTONS);
            animation_stack.push(animation_states.COLAPSE);
            animation_stack.push(animation_states.FADELABELS);
        }
    }

    public void parkCanvas(boolean park){

        if(park){
            animation_stack.push(animation_states.PARKBUTTON);
        }
        else{
            animation_stack.push(animation_states.UNPARKBUTTON);
        }
    }

    public void parkButton(boolean park){

        Log.i("L17", "Parking Button = " + park + " on " + exclude_widget);
        AnimatorSet btanimset = new AnimatorSet();
        ArrayList<ObjectAnimator> animations = new ArrayList<>();

        if (exclude_widget ==-1) return;
        animations.addAll(buttons.get(exclude_widget).getParkAnimator(park));

        for (ObjectAnimator oa : animations){
            btanimset.play(oa);
        }

        btanimset.addListener(this);
        btanimset.start();
    }


    public void colapseButtons(boolean colapse){

        AnimatorSet btanimset = new AnimatorSet();
        ArrayList<ObjectAnimator> animations = new ArrayList<>();

        for(CanvasWidget cw : buttons){
            if(cw.getIndex() != exclude_widget) animations.addAll(cw.getColapseAnimator(colapse));
        }

        for (ObjectAnimator oa : animations){
            btanimset.play(oa);
        }

        btanimset.addListener(this);
        btanimset.start();
    }

    public void fadeLabels(boolean fade){

        AnimatorSet btanimset = new AnimatorSet();

        for(CanvasWidget cw : labels){
            btanimset.play(cw.getFadeLabelAnimator(fade));
        }

        btanimset.addListener(this);
        btanimset.start();
    }

    public void hideButtons(boolean hide){


        for (CanvasWidget cw : buttons){
            if (hide){
                if(cw.getIndex() != exclude_widget) cw.getWidget().setAlpha(0f);
            }
            else{
                cw.getWidget().setAlpha(1f);
            }
        }

        onAnimationEnd(new ObjectAnimator());
    }



    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        //Log.i("L17", "Ending animation");
        if (!animation_stack.empty()){
            animation_states animation_state = animation_stack.pop();
            switch(animation_state){
                case FADELABELS:
                    fadeLabels(true);
                    break;
                case SHOWLABELS:
                    fadeLabels(false);
                    break;
                case COLAPSE:
                    colapseButtons(true);
                    break;
                case EXPLODE:
                    colapseButtons(false);
                    break;
                case HIDEBUTTONS:
                    hideButtons(true);
                    break;
                case SHOWBUTTONS:
                    hideButtons(false);
                    break;
                case PARKBUTTON:
                    parkButton(true);
                    break;
                case UNPARKBUTTON:
                    parkButton(false);
                    break;
                case HIDECHOICE:
                    showChoiceFragment(false);
                    break;
                case SHOWCHOICE:
                    showChoiceFragment(true);
                    break;

                case NONE:
                    break;
                default:
            }
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {}

    @Override
    public void onAnimationRepeat(Animator animation) {}

    public void setExclude_widget(int exclude_widget) {
        this.exclude_widget = exclude_widget;
    }

    public int getExclude_widget() {
        return exclude_widget;
    }


}
