package com.opus.virna7;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

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

    public enum paneltypes { OPERATIONSPANEL, EDITPANEL, HELPPANEL}

    private Stack<animation_states> animation_stack;

    private Virna7Application virna;
    private NavActivity nav;
    private ConstraintLayout choicepanel;
    private RecyclerView choicelist;
    private TextView choiceheader;
    private Button btedit;
    private Button btcopy;
    private Button btdelete;

    boolean choice_visible;
    private int exclude_widget;
    private boolean canvasready = false;

    private CanvasChoiceRecyclerViewAdapter adapter;
    private CanvasChoiceRecyclerViewAdapter.ViewHolder selectedprofile;



    public CanvasCoordinator(NavActivity nav) {

        buttons=new ArrayList<>();
        labels=new ArrayList<>();
        int bt_ptr = 0;
        int lb_ptr = 0;
        animation_stack= new Stack<>();
        exclude_widget=-1;
        choice_visible = false;

        GridLayout grid = (GridLayout) nav.findViewById(R.id.canvasgrid);
        choicepanel = (ConstraintLayout) nav.findViewById(R.id.canvas_choice_root);
        choicelist = (RecyclerView) nav.findViewById(R.id.canvas_choice_rv_items);
        choiceheader = (TextView) nav.findViewById(R.id.canvas_choice_tv_header);
        btedit = (Button) nav.findViewById(R.id.canvas_choice_bt_edit);
        btcopy = (Button) nav.findViewById(R.id.canvas_choice_bt_copy);
        btdelete = (Button) nav.findViewById(R.id.canvas_choice_bt_delete);

        this.nav=nav;
        virna  = (Virna7Application)nav.getApplicationContext();

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

        choicelist.setLayoutManager(new LinearLayoutManager(nav));
        adapter = new CanvasChoiceRecyclerViewAdapter(virna.getProfilemanager().getProfiles(), this);
        choicelist.setAdapter(adapter);

        btedit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            //Log.i("L17", "Edit clicked");
            startProfileEdition();

            }
        });

        btcopy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("L17", "Copy clicked");
                if (selectedprofile != null){
                    virna.getProfilemanager().getProfiles().add(selectedprofile.mItem.clone(true));
                    adapter.notifyItemInserted(virna.getProfilemanager().getProfiles().size());
                }
            }
        });

        btdelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("L17", "Delete clicked");
                if (selectedprofile != null){
                    int position = selectedprofile.getAdapterPosition();
                    virna.getProfilemanager().getProfiles().remove(position);
                    adapter.notifyItemRemoved(position);
                }
            }
        });

    }

    public void startProfileEdition(){
        Intent i = new Intent("com.opus.virna7.ProfileItemListActivity");
        i.putExtra("profile", selectedprofile.mItem.getName());
        nav.startActivity(i);
    }


    public int getButtonIndex(int id){

        for(CanvasWidget cw : buttons){
            if(cw.getWdg_id() == id) return cw.getIndex();
        }
        return -1;
    }


    public void clearStates() {animation_stack.clear();}



    public void choiceClicked (CanvasChoiceRecyclerViewAdapter.ViewHolder holder){

        Log.i("L17", "Choice clicked @" + holder.mItem.getName());

        if (exclude_widget == 1){

            if (selectedprofile != null && (selectedprofile.mItem.getId() != holder.mItem.getId())) selectedprofile.setSelect(false);
            if (selectedprofile == null || (selectedprofile.mItem.getId() != holder.mItem.getId())) {
                holder.setSelect(true);
                selectedprofile = holder;
            }
        }
        else{
            nav.choiceClicked(holder.mItem, exclude_widget);
        }



    }

    public void configChoicePanel(paneltypes paneltype){

        if (paneltype == paneltypes.OPERATIONSPANEL){
            choiceheader.setText("Selecione o Perfil a Executar :");
            //choicelist.setLayoutParams(new android.support.constraint.ConstraintLayout.LayoutParams(500, 200));
            btedit.setVisibility(View.GONE);
            btcopy.setVisibility(View.GONE);
            btdelete.setVisibility(View.GONE);
        }
        if (paneltype == paneltypes.EDITPANEL){
            choiceheader.setText("Selecione o Perfil a Modificar :");
            //choicelist.setLayoutParams(new android.support.constraint.ConstraintLayout.LayoutParams(500, 150));
            btedit.setVisibility(View.VISIBLE);
            btcopy.setVisibility(View.VISIBLE);
            btdelete.setVisibility(View.VISIBLE);
        }
        if (paneltype == paneltypes.HELPPANEL){
            btedit.setVisibility(View.GONE);
            btcopy.setVisibility(View.GONE);
            btdelete.setVisibility(View.GONE);

        }

    }


    public void showChoiceFragment(boolean show){

        if (show){
            if (!choice_visible) {
                choicepanel.setVisibility(View.VISIBLE);
                choice_visible = true;
            }
        }
        else{
            if (choice_visible) {
                choicepanel.setVisibility(View.GONE);
                choice_visible = false;
            }
        }

        onAnimationEnd(new ObjectAnimator());
    }

    public void startCanvas(){

        choicepanel.setVisibility(View.GONE);
        choice_visible = false;
        animation_stack.push(animation_states.SHOWLABELS);
        animation_stack.push(animation_states.EXPLODE);

    }

    public void setCanvas(boolean operational){

        if(operational){
            animation_stack.push(animation_states.SHOWLABELS);
            animation_stack.push(animation_states.EXPLODE);
            animation_stack.push(animation_states.SHOWBUTTONS);
            animation_stack.push(animation_states.UNPARKBUTTON);
            animation_stack.push(animation_states.HIDECHOICE);

        }
        else{
            animation_stack.push(animation_states.SHOWCHOICE);
            animation_stack.push(animation_states.PARKBUTTON);
            animation_stack.push(animation_states.HIDEBUTTONS);
            animation_stack.push(animation_states.COLAPSE);
            animation_stack.push(animation_states.FADELABELS);
        }
    }


    public void parkButton(boolean park){

        //Log.i("L17", "Parking Button = " + park + " on " + exclude_widget);
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

        canvasready=!fade;
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

    public boolean isCanvasready() {
        return canvasready;
    }

}
