package com.opus.virna7;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by opus on 29/04/17.
 */

public class ProfileEntry {

    private long id;
    private String name;
    private Date created;
    private String owner;
    private ArrayList<ProfilePhase> phases;


    public ProfileEntry(long id, String name, String owner, Date created, ArrayList<ProfilePhase> phases) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.phases = phases;
        this.owner = owner;
        for (ProfilePhase pp : phases){
            pp.setParent(this);
        }
    }

    public long getId() {return id;}

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public ArrayList<ProfilePhase> getPhases() {
        return phases;
    }

    public void setPhases(ArrayList<ProfilePhase> phases) {
        this.phases = phases;
    }

    public void addPhase (ProfilePhase pp) {
        phases.add(pp);
        pp.setParent(this);
    }

    public void removePhase (ProfilePhase pp){ phases.remove(pp);}

    public void pastePhase(ProfilePhase insertion, ProfilePhase inserted, boolean append){

        ArrayList<ProfilePhase> newphases = new ArrayList<>();
        for (ProfilePhase pp : phases){
            if (!append && pp.equals(insertion)){
                newphases.add(inserted);
            }
            newphases.add(pp);
            if (append && pp.equals(insertion)){
                newphases.add(inserted);
            }
        }
        phases = newphases;
    }


    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getPhasesNum() {return phases.size();}

    public ProfileEntry clone(boolean copyflag){

        long now = System.currentTimeMillis();
        String copytag = copyflag ? "Copia de " : "";

        ArrayList<ProfilePhase> tphases = new ArrayList<>();
        for (ProfilePhase pp : phases){
            tphases.add(pp.clone());
        }

        ProfileEntry clone = new ProfileEntry (now,
                                copytag + name,
                                owner,
                                new Date(now),
                                tphases);

        return clone;
    }

    public ArrayList<ProfileFlatEntry> getFlatEntries(){

        int i = 0;
        ArrayList<ProfileFlatEntry> flatentries = new ArrayList<>();

        ProfileFlatEntry entry = new ProfileFlatEntry(name, ProfileFlatEntry.FLATYPE.ROOT, i++);
        entry.setRoot(this);
        flatentries.add(entry);

        for (ProfilePhase ph : phases){
            i=ph.addFlatProfile(flatentries, i);
        }

        return flatentries;
    }

}
