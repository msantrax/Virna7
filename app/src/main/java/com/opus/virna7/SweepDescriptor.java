package com.opus.virna7;

/**
 * Created by opus on 23/01/17.
 */

public class SweepDescriptor {

    private int inner;
    private int outer;
    private int vel;
    private int target;
    private int position;
    private boolean convex;
    private int algo;
    private boolean updated = true;

    public SweepDescriptor(int inner, int outer, int vel, int target, boolean convex, int algo, int pos) {
        this.setInner(inner);
        this.setOuter(outer);
        this.setVel(vel);
        this.setTarget(target);
        this.setPosition(pos);
        this.setConvex(convex);
        this.setAlgo(algo);
    }

    public SweepDescriptor(){
        this.setInner(30);
        this.setOuter(80);
        this.setVel(2);
        this.setTarget(1500);
        this.setPosition(50);
        this.setConvex(true);
        this.setAlgo(0);
    }



    public int getInner() {
        return inner;
    }

    public void setInner(int inner) {
        this.inner = inner;
        updated = true;
    }

    public int getOuter() {
        return outer;
    }

    public void setOuter(int outer) {
        this.outer = outer;
        updated = true;
    }

    public int getVel() {
        return vel;
    }

    public void setVel(int vel) {
        this.vel = vel;
        updated = true;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
        updated = true;
    }

    public boolean isConvex() {
        return convex;
    }

    public void setConvex(boolean convex) {
        this.convex = convex;
        updated = true;
    }

    public int getAlgo() {
        return algo;
    }

    public void setAlgo(int algo) {
        this.algo = algo;
        updated = true;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
        updated = true;
    }
}
