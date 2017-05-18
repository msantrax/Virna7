package com.opus.virna7;

import java.util.ArrayList;

/**
 * Created by opus on 30/04/17.
 */

public class ProfilePhase {

    private String phasename;
    private String triggername;
    private double triggervalue;
    private ArrayList<ProfileValue> values;


    public ProfilePhase(String phasename, String triggername, double triggervalue, ArrayList<ProfileValue> values) {
        this.setPhasename(phasename);
        this.setTriggername(triggername);
        this.setTriggervalue(triggervalue);
        this.setValues(values);
    }


    public String getPhasename() {
        return phasename;
    }

    public void setPhasename(String phasename) {
        this.phasename = phasename;
    }

    public String getTriggername() {
        return triggername;
    }

    public void setTriggername(String triggername) {
        this.triggername = triggername;
    }

    public double getTriggervalue() {
        return triggervalue;
    }

    public void setTriggervalue(double triggervalue) {
        this.triggervalue = triggervalue;
    }

    public ArrayList<ProfileValue> getValues() {
        return values;
    }

    public void setValues(ArrayList<ProfileValue> values) {
        this.values = values;
    }
}
