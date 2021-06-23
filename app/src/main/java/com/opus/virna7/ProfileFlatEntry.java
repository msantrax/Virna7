package com.opus.virna7;

/**
 * Created by opus on 20/05/17.
 */

public class ProfileFlatEntry {



    public static enum FLATYPE {ROOT, PHASE, VALUE};
    public static enum FLATSTATUS {VIRGIN, TOUCHED, ADDED, REMOVED};

    private String name;
    private FLATYPE type;
    private int index;
    private ProfilePhase phase;
    private ProfileValue value;
    private ProfileEntry entry;
    private FLATSTATUS status;


    public ProfileFlatEntry (String name, FLATYPE type, int index){
        this.name = name;
        this.type = type;
        this.index = index;
        status = FLATSTATUS.VIRGIN;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FLATYPE getType() {
        return type;
    }

    public void setType(FLATYPE type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public void setPhase( ProfilePhase phase){ this.phase = phase;}

    public ProfilePhase getPhase() {
        return phase;
    }

    public void setValue( ProfileValue value){ this.value = value;}

    public ProfileValue getValue() {
        return value;
    }

    public void setRoot( ProfileEntry entry){ this.entry = entry;}

    public ProfileEntry getRoot() {
        return entry;
    }


    public FLATSTATUS getStatus() {
        return status;
    }

    public void setStatus(FLATSTATUS status) {
        this.status = status;
    }



}
