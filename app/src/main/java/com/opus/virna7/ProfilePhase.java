package com.opus.virna7;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by opus on 30/04/17.
 */

public class ProfilePhase {

    private String phasename;
    private String triggername;
    private double triggervalue;
    private ArrayList<ProfileValue> values;
    private ProfileEntry pe;

    public static ProfilePhase create(){
        ProfilePhase pp = new ProfilePhase("Nova Fase", "Gatilho Desabilitado", 0.0, new ArrayList<ProfileValue>());
        return pp;
    }

    public ProfilePhase(String phasename, String triggername, double triggervalue, ArrayList<ProfileValue> values) {
        this.setPhasename(phasename);
        this.setTriggername(triggername);
        this.setTriggervalue(triggervalue);
        this.setValues(values);
        for (ProfileValue vl : values){
            vl.setParent(this);
        }
    }


    public void setParent(ProfileEntry pe) { this.pe = pe;};

    public ProfileEntry getParent() { return pe;}

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

    public int getValuesNum() {return values.size();}

    public void addValue (ProfileValue pv) {
        values.add(pv);
        pv.setParent(this);
    }

    public void removeValue (ProfileValue pv){ values.remove(pv);}


    public ProfilePhase clone(){

        ArrayList<ProfileValue> tvalues = new ArrayList<>();
        for (ProfileValue vl : values){
            tvalues.add(vl.clone());
        }

        ProfilePhase clone = new ProfilePhase (phasename,
                triggername,
                triggervalue,
                tvalues);
        return clone;
    }

    public int addFlatProfile (ArrayList<ProfileFlatEntry> flatentries, int index){

        ProfileFlatEntry entry = new ProfileFlatEntry(phasename, ProfileFlatEntry.FLATYPE.PHASE, index++);
        entry.setPhase(this);
        flatentries.add(entry);

        for (ProfileValue vl : values){
            vl.addFlatProfile(flatentries, index++);
        }
        return index;
    }


}
