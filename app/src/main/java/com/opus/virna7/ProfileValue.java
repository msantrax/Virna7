package com.opus.virna7;

import java.util.ArrayList;

/**
 * Created by opus on 30/04/17.
 */

public class ProfileValue {

    private String stype;
    private TYPES type;
    private double startvalue;
    private double stopvalue;
    private double extravalue;


    public static enum TYPES {SPEED, SWEEP}


    public ProfileValue(String stype, double startvalue, double stopvalue, double extravalue) {
        this.setStype(stype);
        this.setStartvalue(startvalue);
        this.setStopvalue(stopvalue);
        this.setExtravalue(extravalue);
    }

    public String getStype() {
        return stype;
    }

    public void setStype(String stype) {
        this.stype = stype;
    }

    public double getStartvalue() {
        return startvalue;
    }

    public void setStartvalue(double startvalue) {
        this.startvalue = startvalue;
    }

    public double getStopvalue() {
        return stopvalue;
    }

    public void setStopvalue(double stopvalue) {
        this.stopvalue = stopvalue;
    }

    public double getExtravalue() {return extravalue;}

    public void setExtravalue(double extravalue) {this.extravalue = extravalue;}


    public ProfileValue clone(){

        ProfileValue clone = new ProfileValue (stype,
                startvalue,
                stopvalue,
                extravalue);
        return clone;
    }



}
