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
    private String stringvalue;

    private ProfilePhase pp;

    public static enum TYPES {SPEED, SWEEP}


    public static ProfileValue create(){
        ProfileValue pv = new ProfileValue("Novo Comando", 0.0, 0.0, 0.0);
        return pv;
    }

    public ProfileValue(String stype, double startvalue, double stopvalue, double extravalue) {
        this.setStype(stype);
        this.setStartvalue(startvalue);
        this.setStopvalue(stopvalue);
        this.setExtravalue(extravalue);
    }


    public void setParent(ProfilePhase pp) { this.pp = pp;};

    public ProfilePhase getParent() { return pp;}

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

    public String getStringvalue() {return stringvalue;}

    public void setStringvalue(String stringvalue) {this.stringvalue = stringvalue;}


    public ProfileValue clone(){
        ProfileValue clone = new ProfileValue (stype,
                startvalue,
                stopvalue,
                extravalue);
        clone.setStringvalue(stringvalue);
        return clone;
    }


    public void addFlatProfile (ArrayList<ProfileFlatEntry> flatentries, int index){

        ProfileFlatEntry entry = new ProfileFlatEntry(stype, ProfileFlatEntry.FLATYPE.VALUE, index);
        entry.setValue(this);
        flatentries.add(entry);
    }

}
