package com.opus.virna7;

import android.app.Application;

/**
 * Created by opus on 16/05/17.
 */
public class Virna7Application  extends Application {

    private static Virna7Application instance = new Virna7Application();

    private int userid;
    private EtherService etherserviceBinder;
    private ProfileManager profilemanager;


    public static Virna7Application getInstance() {

        if (instance == null){
            instance = new com.opus.virna7.Virna7Application();
        }
        return instance;
    }

    public Virna7Application() {
    }


    @Override
    public final void onCreate() {
        super.onCreate();
        instance = this;
    }


    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public EtherService getEtherserviceBinder() {
        return etherserviceBinder;
    }

    public void setEtherserviceBinder(EtherService etherserviceBinder) {
        this.etherserviceBinder = etherserviceBinder;
    }

    public ProfileManager getProfilemanager() {
        return profilemanager;
    }

    public void setProfilemanager(ProfileManager profilemanager) {
        this.profilemanager = profilemanager;
    }
}
