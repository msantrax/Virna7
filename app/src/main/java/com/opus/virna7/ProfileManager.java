package com.opus.virna7;

import android.content.res.AssetManager;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by opus on 28/04/17.
 */



public class ProfileManager {


    private static final String TAG = "ProfileMan";


    private static String filename = "profile.dat";
    private static com.opus.virna7.ProfileManager instance;
    private static AssetManager appassets;

    private FileOutputStream fOut;
    private OutputStreamWriter osw;
    private FileInputStream fin;
    private InputStreamReader isr;

    private ArrayList<ProfileEntry> profiles;


    public ProfileManager() {
        openProfile();
    }

    public static com.opus.virna7.ProfileManager getInstance(AssetManager as){

        if (instance == null){
            appassets = as;
            instance = new com.opus.virna7.ProfileManager();
        }
        return instance;
    }


    public void loadDefaults(){

        try {
            //String[] assets = as.list("");
            InputStream is = appassets.open("profiles.json");
            profiles = readJsonStream(is);
            Log.d(TAG, "Profile loaded from assets");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ProfileEntry> getProfile() {
        return profiles;
    }


    public ProfileEntry findProfileByName ( String name){

        for (ProfileEntry entry : profiles){
            if (entry.getName().equals(name)) return entry;
        }
        return profiles.get(0);
    }


    public void openProfile(){

        try {
            fin = new FileInputStream("profiles.txt");
            isr = new InputStreamReader(fin);
            profiles = readJsonStream(fin);
            Log.d(TAG, "Profile loaded from user data");
        }
        catch (IOException ioe) {
            loadDefaults();
            Log.e(TAG, "Failed to load profile : " + ioe.getMessage());
        }
    }


    public void initProfiles(){

        try {
            fOut = new FileOutputStream("profiles.txt");
            osw = new OutputStreamWriter(fOut);
            profiles = new ArrayList<>();
            //fin = openRawResource(R.values.profiles.json)
        }
        catch (IOException ioe) {
            Log.e(TAG, "Failed to load profile : " + ioe.getMessage());
        }


    }

    // ================================= Profile JSON Loading =====================================
    //=============================================================================================
    public ArrayList<ProfileEntry> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readProfilesArray(reader);
        } finally {
            reader.close();
        }
    }

    public ArrayList<ProfileEntry> readProfilesArray(JsonReader reader) throws IOException {

        ArrayList<ProfileEntry> profiles = new ArrayList<ProfileEntry>();

        reader.beginArray();
        while (reader.hasNext()) {
            profiles.add(readProfile(reader));
        }
        reader.endArray();
        return profiles;
    }

    public ProfileEntry readProfile(JsonReader reader) throws IOException {

        long id = -1;

        String profname = null;
        String owner = null;
        Date created = null;
        ArrayList<ProfilePhase> phases = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();

            if (name.equals("id")) {
                id = reader.nextLong();
            } else if (name.equals("name")) {
                profname = reader.nextString();
            } else if (name.equals("created")) {
                String sdate = reader.nextString();
                created = new Date();
            } else if (name.equals("phases")) {
                phases =  readPhasesArray(reader);
            } else if (name.equals("owner")) {
                owner = reader.nextString();

            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new ProfileEntry(id, profname, owner, created, phases);
    }


    public ArrayList<ProfilePhase> readPhasesArray(JsonReader reader) throws IOException {

        ArrayList<ProfilePhase> phases = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            phases.add(readPhase(reader));
        }
        reader.endArray();
        return phases;
    }


    public ProfilePhase readPhase(JsonReader reader) throws IOException {

        String phasename = null;
        String triggertype = null;
        double triggervalue = 0.0;
        ArrayList<ProfileValue>values = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals( "phasename")) {
                phasename = reader.nextString();
            } else if (name.equals("triggertype")) {
                triggertype = reader.nextString();
            } else if (name.equals("triggervalue")) {
                triggervalue = reader.nextDouble();
            } else if (name.equals("values")) {
                values =  readValuesArray(reader);

            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new ProfilePhase(phasename, triggertype, triggervalue, values);
    }


    public ArrayList<ProfileValue> readValuesArray(JsonReader reader) throws IOException {

        ArrayList<ProfileValue> values = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            values.add(readValue(reader));
        }
        reader.endArray();
        return values;
    }


    public ProfileValue readValue (JsonReader reader) throws IOException {

        String valuetype = null;
        double startvalue = 0.0;
        double stopvalue = 0.0;
        double extravalue = 0.0;

        ArrayList<ProfileValue>values = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals( "valuetype")) {
                valuetype = reader.nextString();
            } else if (name.equals("startvalue")) {
                startvalue = reader.nextDouble();
            } else if (name.equals("stopvalue")) {
                stopvalue = reader.nextDouble();
            } else if (name.equals("extravalue")) {
                extravalue = reader.nextDouble();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new ProfileValue(valuetype, startvalue, stopvalue, extravalue);
    }

    // =============================== Profile JSON Save ==========================================

    public void writeJsonStream(OutputStream out, ArrayList<ProfileEntry> profiles) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writeProfilesArray(writer, profiles);
        writer.close();
    }

    public void writeProfilesArray(JsonWriter writer, List<ProfileEntry> profentries) throws IOException {
        writer.beginArray();
        for (ProfileEntry entry : profentries) {
            writeProfile(writer, entry);
        }
        writer.endArray();
    }

    public void writeProfile (JsonWriter writer, ProfileEntry profentry) throws IOException {
        writer.beginObject();
        writer.name("id").value(profentry.getId());
        writer.name("name").value(profentry.getName());
        writer.name("owner").value(profentry.getOwner());
        if (!profentry.getPhases().isEmpty()) writePhasesArray (writer, profentry.getPhases());
        writer.endObject();
    }

    public void writePhasesArray(JsonWriter writer, List<ProfilePhase> profphases) throws IOException {
        writer.beginArray();
        for (ProfilePhase phase : profphases) {
            writeProfilePhase(writer, phase);
        }
        writer.endArray();
    }

    public void writeProfilePhase (JsonWriter writer, ProfilePhase profphase) throws IOException {
        writer.beginObject();
        writer.name("phasename").value(profphase.getPhasename());
        writer.name("triggertype").value(profphase.getTriggername());
        writer.name("triggervalue").value(profphase.getTriggervalue());
        if (!profphase.getValues().isEmpty()) writeValuesArray (writer, profphase.getValues());
        writer.endObject();
    }

    public void writeValuesArray(JsonWriter writer, List<ProfileValue> profvalues) throws IOException {
        writer.beginArray();
        for (ProfileValue value : profvalues) {
            writeProfileValue(writer, value);
        }
        writer.endArray();
    }

    public void writeProfileValue (JsonWriter writer, ProfileValue profvl) throws IOException {
        writer.beginObject();
        writer.name("valuetype").value(profvl.getStype());
        writer.name("startvalue").value(profvl.getStartvalue());
        if (profvl.getStartvalue() != 0.0) writer.name("stopvalue").value(profvl.getStopvalue());
        if (profvl.getExtravalue() != 0.0) writer.name("extravalue").value(profvl.getExtravalue());
        writer.endObject();
    }




}


