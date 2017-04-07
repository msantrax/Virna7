package com.opus.virna7;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class RunTaskRightGauges {

    private Context ctx;
    private RunActivity acvty;
    private TableLayout gauges;
    private RunTaskDescriptor td;
    private LayoutInflater inflater;
    private GaugeType gtype;

    public static final int GAUGEFULL = R.id.fl_gauge_full;
    public static final int GAUGEEMPTY = R.id.fl_gauge_empty;

    private FrameLayout[] frame_gauges = new FrameLayout[4];
    private int[] installed = new int[4];
    private TextView[] tv_values = new TextView[4];
    private String[] values_format = new String[4];


    public RunTaskRightGauges(TableLayout gauges, RunTaskDescriptor td, RunActivity acvty) {
        this.gauges = gauges;
        this.td = td;
        this.acvty = acvty;
        this.ctx = acvty.getBaseContext();

        inflater = LayoutInflater.from(ctx);
        gtype = new GaugeType();

        for (int i = 0; i < 4; i++) {
            installGauge (i);
        }
    }

    public GaugeType getGaugeDescriptor() { return gtype;}

    public int getInstalled(int slot) {return installed[slot];}

    private void installGauge (int index){

        // First select what type of gauge
        if (td.getGaugeType(index) == GaugeType.Gauget.EMPTY.getOrdinal()){
            addEmptyGauge(index);
        }
        else{
            addGauge(index, td.getGaugeType(index));
        }
    }


    public void addEmptyGauge(int index){

        FrameLayout fl;

        fl = (FrameLayout)inflater.inflate(R.layout.gauge_empty, gauges, false);
        if(frame_gauges[index] != null) gauges.removeViewAt(index);
        gauges.addView(fl, index);
        tv_values[index] = null;
        values_format[index] = null;
        acvty.registerGaugeContextMenu(fl);
        installed[index] = 0;

        frame_gauges[index] = fl;

    }


    public void addGauge(int index, int gaugetype){

        FrameLayout fl;
        TextView tv_temp;
        //int gaugetype = (int)gaugetype;

        fl = (FrameLayout)inflater.inflate(R.layout.gauge_full, gauges, false);
        if(frame_gauges[index] != null) gauges.removeViewAt(index);
        gauges.addView(fl, index);

        // Update value
        tv_values[index] = (TextView)fl.getChildAt(1);
        values_format[index] = gtype.getFormat(gaugetype);
        updateRTGauge(index, 0.0);

        // Update Label
        tv_temp = (TextView)fl.getChildAt(2);
        tv_temp.setText(gtype.getLabel(gaugetype));

        installed[index] = (int)gaugetype; //td.getGaugeType(index);
        fl.setOnTouchListener(new RightGaugesSwipeTouchListener(ctx, acvty, index));

        frame_gauges[index] = fl;

    }


    public boolean isInstalled(int ordinal){

        for (int i = 0; i < 4; i++) {
            if (installed[i] == ordinal){
                return true;
            }
        }
        return false;
    }

    public int getGaugeIndex(View v){

        for (int i = 0; i < 4; i++) {
            if (frame_gauges[i].equals(v)){
                return i;
            }
        }
        return 0;
    }


    public void updateRTGauge(int lindex, double value){

        String tmp;

        if (tv_values[lindex] != null){
            tmp = String.format(values_format[lindex], value);
            tv_values[lindex].setText(tmp);
        }
    }




    private class RightGaugesSwipeTouchListener extends RunTaskSwipeTouchListener {

        RunActivity acvty;
        int index;


        public RightGaugesSwipeTouchListener (Context ctx, RunActivity acvty, int index){
            super(ctx);
            this.acvty = acvty;
            this.index = index;
        }

        @Override
        public void onSwipeTop() {
            //Toast.makeText(acvty, "top @ " + index, Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onSwipeRight() {
            //Toast.makeText(acvty, "right @ "+ index, Toast.LENGTH_SHORT).show();
            addEmptyGauge(index);
        }
        @Override
        public void onSwipeLeft() {
            //Toast.makeText(acvty, "left", Toast.LENGTH_SHORT).show();
            acvty.setGaugevalue(installed[index], gtype);
        }
        @Override
        public void onSwipeBottom() {
            //Toast.makeText(acvty, "bottom", Toast.LENGTH_SHORT).show();
        }


    };

}
