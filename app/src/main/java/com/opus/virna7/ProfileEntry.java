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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
