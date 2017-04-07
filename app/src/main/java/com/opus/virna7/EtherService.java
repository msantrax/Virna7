package com.opus.virna7;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EtherService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static String TAG = "   ETHERSERVICE====>";
    private final IBinder binder = new EtherBinder();

    //private NotificationManager mNotificationManager;
    public static LinkedHashMap<String,Integer> comand_map;
    public static LinkedHashMap<String,Integer> flags_map;
    public static LinkedHashMap<String,Integer> slots_map;

    private LinkedBlockingQueue<SMTraffic> servicequeue;
    private NetThread service_thread;
    private SharedPreferences preferences ;

    public static enum STATES { CONFIG, RESET, INIT, IDLE, ATTACH, ATTACHED, DETACH, NETLISTEN, NETCONNECTED,
                                SENDU, RECVU, ONERROR, MANAGETASK, RUNNINGTASK, PING,  TOOGLECB, MANAGECALLBACK

    };

    public static enum CMDS {LOADSTATE, RECOVERERROR, UPDATEVALUES};

    public static int port = 10887;
    public static String host = "192.168.0.110";

    public boolean callback_enabled = false;
    public int callbacklag = 500;
    private boolean attached = false;
    public boolean autoattach = true;

    // Status
    double []slots = new double[10];
    boolean []flags = new boolean [12];
    boolean newstatus = false;

    boolean broadcast_traffic = true;
    boolean broadcast_rawstatus = true;

    public EtherService() {
        servicequeue = new LinkedBlockingQueue<>();
        attached = false;
        loadMaps();
        //Log.d(TAG, "EtherService new class created...................");
    }

    public void loadNetPreferences(){
        autoattach = preferences.getBoolean(getString(R.string.pref_net_k_autologin), false);
        callback_enabled = preferences.getBoolean(getString(R.string.pref_net_k_dostatuscallback), false);
        callbacklag = Integer.valueOf(preferences.getString(getString(R.string.pref_net_k_statuscallbackperiod),"500"));
    }


    public void onDestroy() {
        super.onDestroy();
        service_thread.setDone(true);
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        Log.d(TAG, "EtherService Destroyed");
    }


    // Vars management =====================================================================================================
    private void loadStatus(byte[] mes){

        int cmd = 1;
        String payload;
        char token;
        char[] chars = new char[150];

        if (cmd==1){
            for (int i = 12; i < 162; i++) {
                token = (char)mes[i];
                if (token > 32 && token < 126){
                    chars[i-12] = token;
                }
                else{
                    chars[i-12] = ',';
                }
            }
            payload = String.valueOf(chars, 0, 150);
        }
        else{
            payload ="";
        }


        String[] s_slots = payload.split(",");
        for (int j = 0; j < 10; j++) {
            storeSlot(j, s_slots[j]);
        }
        for (int k = 0; k < 12; k++) {
            int idx = k+10;
            storeFlag(k, s_slots[idx]);
        }

        broadcastMessage("COMMANDACK", comand_map.get("NET_STATUSARRIVED"), "TRUE");
        //Log.d(TAG, "Receiving Status @ " + getTimestamp(mes) + " = " + payload);
    }

    private String getTimestamp(byte[] mes){
        String temp = String.format(Locale.US,"%02X%02X%02X",
                mes[2],
                mes[3],
                mes[4]);
        return temp;
    }

    private boolean storeSlot(int idx, String val){
        try {
            Double d = Double.valueOf(val);
            slots[idx] = d;
        } catch (NumberFormatException ex) {
            Log.d(TAG,  "Error converting status @ slot " + idx);
            return false;
        }
        return true;
    }

    private boolean storeFlag(int idx, String val){

        if (val.equals("1")){
            flags[idx] = true;
        }
        else if(val.equals("0")){
            flags[idx] = false;
        }
        else{
            Log.d(TAG,  "Error converting flag @ slot " + idx);
            return false;
        }
        return true;
    }


    public double getStatusSlot(int idx){return slots[idx];}

    public boolean getStatusFlag(int idx){return flags[idx];}


    public void readVar(int cmd){

        if (cmd == comand_map.get("NET_ATTACHED")){
            setAttached(attached);
        }
        else if (cmd == comand_map.get("NET_CALLBACKENABLED")){
            broadcastMessage("COMMANDACK", comand_map.get("NET_CALLBACKENABLED"), getSBoolean(callback_enabled));
        }
    }

    private String getSBoolean( boolean v){
        return v ? "TRUE" : "FALSE";
    }


    public boolean isAttached(){
        return attached;
    }

    public boolean isCallbackEnabled() {return callback_enabled; }
    public void setAttached(boolean att){
        attached = att;
        broadcastMessage("COMMANDACK", comand_map.get("NET_ATTACHED"), getSBoolean(attached));
        Log.d(TAG, "EtherService set attached = " + attached);
    }


    public void broadcastTraffic(boolean broadcast){ broadcast_traffic = broadcast;}


    private void loadMaps(){
        comand_map = new LinkedHashMap<>();
        flags_map = new LinkedHashMap<>();

        comand_map.put("CALLBACK_REQ", 0);
        comand_map.put("CALLBACK_OK", 1);
        comand_map.put("CALLBACK_STATUS", 3);
        comand_map.put("COMAND_ACK", 4);

        // Sweep
        comand_map.put("SWEEP_PARK", 30);
        comand_map.put("SWEEP_GOTO", 31);
        comand_map.put("SWEEP_SCAN", 32);
        comand_map.put("SWEEP_ENABLE", 33);
        comand_map.put("SWEEP_DIR", 34);
        comand_map.put("SWEEP_PULSE", 35);

        //SERVO
        comand_map.put("SERVO_ENABLE", 40);
        comand_map.put("SERVO_SETPWM",41);
        comand_map.put("SERVO_SETSPEED" , 42);
        comand_map.put("SERVO_SETTRANSFER" , 43);
        comand_map.put("SERVO_SETPID" , 44);

        comand_map.put("MON_SETVAC", 50);
        comand_map.put("MON_SETDRUM",51);
        comand_map.put("WG20_SETDIR" , 52);
        comand_map.put("WG20_SETVEL" , 53);
        comand_map.put("MON_RESET" , 54);


        // Command acks
        comand_map.put("ACK_ATTACH", 60);
        comand_map.put("ACK_DETACH", 61);
        comand_map.put("ACK_CALLBACK_ENABLE", 62);
        comand_map.put("ACK_CALLBACK_DISABLE", 63);
        comand_map.put("ACK_CALLBACK_PERIOD", 63);

        comand_map.put("CMD_CSTATUS", 20);

        // Internal talk
        comand_map.put("NET_ATTACHED", 1000);
        comand_map.put("NET_CALLBACKENABLED", 1001);
        comand_map.put("NET_STATUSARRIVED", 1002);

        flags_map.put("SWEEP_INNER", 0);
        flags_map.put("SWEEP_OUTER", 1);
        flags_map.put("SWEEP_PARK", 2);
        flags_map.put("SWEEP_ENABLED", 3);
        flags_map.put("SWEEP_OUTDIR", 4);
        flags_map.put("SERVO_ENABLED", 5);
        flags_map.put("SERVO_FAULT", 6);
        flags_map.put("WG20_ENABLED", 7);
        flags_map.put("WG20_DIR", 8);
        flags_map.put("WG20_PRESENT", 9);
        flags_map.put("DRUM_ENABLED", 10);
        flags_map.put("VAC_ENABLED", 11);
        flags_map.put("DROP_SENSOR", 12);

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {

        if (getString(R.string.pref_net_k_autologin).equals(key)) {
            autoattach = preferences.getBoolean(key, false);
            if (autoattach){
                Log.d(TAG, "Etherservice is trying to attach");
                servicequeue.add(new SMTraffic(CMDS.LOADSTATE, 0, STATES.ATTACH));
            }
            else{
                servicequeue.add(new SMTraffic(CMDS.LOADSTATE, 0, STATES.DETACH));
            }
        }
        else if (getString(R.string.pref_net_k_dostatuscallback).equals(key)) {
            callback_enabled = preferences.getBoolean(key, false);
        }
        else if (getString(R.string.pref_net_k_statuscallbackperiod).equals(key)) {
            callbacklag = Integer.valueOf(preferences.getString(key,"500"));
        }

    }

    // Bind procedures ================================================================================================================================
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        loadNetPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        if (service_thread == null) {
            Log.d(TAG, "StartCommand started Ether Service thread");
            service_thread =  new NetThread(servicequeue);
            startThread();
        }
        return START_STICKY;
    }


    public class EtherBinder extends Binder {
        EtherService getService() {
            return EtherService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public void broadcastMessage(String bcmd, String bmes){

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(bcmd);
        broadcastIntent.putExtra("STRING", bmes);
        getBaseContext().sendBroadcast(broadcastIntent);
    }

    public void broadcastMessage(String bcmd, int code, String value){

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(bcmd);
        broadcastIntent.putExtra("CODE", code);
        broadcastIntent.putExtra("VALUE", value);
        getBaseContext().sendBroadcast(broadcastIntent);
    }


    public void broadcastMessage(String bcmd, AcpMessage mes){

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(bcmd);
        broadcastIntent.putExtra("ACPM_BYTES", mes.getMessage());
        getBaseContext().sendBroadcast(broadcastIntent);

    }


    // Service Thread
    // ================================================================================================================

    public LinkedBlockingQueue<SMTraffic> getTrafficQueue() { return servicequeue;}

    private void stopThread(){
        //services.removeUsbServicesListener(this);
        service_thread.setDone(true);
    }

    private void startThread(){
        servicequeue.clear();
        //services.addUsbServicesListener(this);
        new Thread(service_thread).start();
    }


    public void sendMessage(AcpMessage mes){
        servicequeue.add(new SMTraffic(CMDS.LOADSTATE, 0, STATES.SENDU, mes));
    }

    public void loadMessage(AcpMessage mes){
        servicequeue.add(new SMTraffic(CMDS.LOADSTATE, 0, STATES.RECVU, mes));
    }

    public void doComand(STATES state, int code){
        servicequeue.add(new SMTraffic(CMDS.LOADSTATE, code, state));
    }


    private class NetThread extends Thread {

        private STATES state;
        private boolean done;
        private ArrayDeque <STATES>states_stack;

        char[] chars = new char[53];
        protected BlockingQueue<SMTraffic>tqueue;
        private SMTraffic smm;
        private CMDS cmd;

        private BufferedInputStream bis = null;
        private BufferedOutputStream bos = null;

        private byte[] message = new byte[162];
        private byte[] linktest = new byte [] {0,0x55,0, 0x55, 0, 0x55};
        private int mesptr = 0;
        private int msize=0;
        private int retry=0;

        Socket clientSock;

        public NetThread(BlockingQueue<SMTraffic> tqueue) {
            this.tqueue = tqueue;
            states_stack = new ArrayDeque<>();
            states_stack.push(STATES.RESET);
            setDone(false);
        }


        @Override
        public void run(){

            Log.d(TAG, "Starting network client Thread");
            states_stack.clear();
            states_stack.push(STATES.RESET);
            setDone(false);
            //callbacklag=500;
            //callback_enabled = false;
            attached = false;

            while (!done){

                if (!states_stack.isEmpty()) state = states_stack.pop();

                switch (state){

                    case INIT:
                        //Log.d(TAG, "Net Client State Machine in INIT");
                        break;

                    case ATTACH:
                        if (!attached) {
                            Log.d(TAG, "Net Client is trying to attach to Server");
                            //port = Integer.valueOf(tf_port.getText());
                            //host = tf_hostname.getText();
                            try {
                                clientSock = new Socket(host, port);
                                //int recsize = clientSock.getReceiveBufferSize();
                                //boolean kalive = clientSock.getKeepAlive();
                                clientSock.setKeepAlive(true); // <- important to packets > 64 bits
                                //SocketChannel schannel = clientSock.getChannel();
                                bis = new BufferedInputStream(clientSock.getInputStream());
                                bos = new BufferedOutputStream(clientSock.getOutputStream());
                                Log.d(TAG, "Link is UP - trying to talk...");
                                bos.write(linktest);
                                bos.flush();
                                retry = 4;
                                states_stack.push(STATES.ATTACHED);
                            } catch (UnknownHostException | NoRouteToHostException | ConnectException e) {
                                Log.d(TAG, "Client failed to connect ? ->" + e.getMessage());
                                states_stack.push(STATES.IDLE);
                            } catch (IOException e) {
                                Log.d(TAG, "Client had an IO problem ?-> " + e.getMessage());
                                states_stack.push(STATES.IDLE);
                            }
                        }
                        break;

                    case ATTACHED:
                        try {
                            int available = bis.available();
                            if (available !=0){
                                Log.d(TAG, "Talk test -> bytes on wire : " + available);
                                for (int i = 0; i <available; i++) {
                                    message[0] = (byte)bis.read();
                                }
                                setAttached(true);
                                Log.d(TAG,  "Good, We are talking");
                                states_stack.push(STATES.NETCONNECTED);
                            }
                            else{
                                sleep(500);
                                if (retry-- <=0){
                                    Log.d(TAG, "Could not talk, sad but we are dettaching");
                                    states_stack.push(STATES.DETACH);
                                }
                            }
                        } catch (IOException ex) {
                            Log.d(TAG, "IO Error in network transaction ?->" + ex.getMessage());
                            states_stack.push(STATES.IDLE);
                        } catch (InterruptedException ex) {
                            Log.d(TAG,  "Client Interrupted ?-> " + ex.getMessage());
                        }
                        break;

                    case DETACH:
                        try {
                            if (isAttached() && (!clientSock.isClosed())) clientSock.close();
                            Log.d(TAG, "Client detached from server");
                            setAttached(false);
                            states_stack.push(STATES.IDLE);
                        } catch (IOException ex) {
                            Log.d(TAG, "Unable to detach from server ? " + ex.getMessage());
                            states_stack.push(STATES.IDLE);
                        }
                        break;

                    case NETCONNECTED :
                        // First verify if we have any comand requested
                        smm = tqueue.poll();
                        if (smm != null){
                            cmd = smm.getCommand();
                            if (cmd == CMDS.LOADSTATE){
                                states_stack.push(smm.getState());
                            }
                        }
                        else if (callback_enabled){
                            // No comand -> request status then
                            // Weŕe on easy run now so do some sleep first
                            try {
                                sleep(callbacklag);
                            } catch (InterruptedException ex) {
                                Log.d(TAG, "Client Interrupted ?->" + ex.getMessage());
                            }
                            AcpMessage m = new AcpMessage();
                            m.setCmd(comand_map.get("CALLBACK_REQ"));
                            m.setMessage("Status req");
                            //m.setSize(0);
                            m.setOrig(0);
                            m.setDest(0);
                            try {
                                bos.write(m.getMessage());
                                bos.flush();
                                //Log.d(TAG,  "Sending   Status @ " + m.getTimestamp());
                                mesptr=0;
                                retry = 4;
                                //TODO : adjust msize
                                states_stack.push(STATES.NETLISTEN);
                            } catch (IOException ex) {
                                Log.d(TAG, "IO Fault on status callback request");
                                states_stack.push(STATES.DETACH);
                            }
                        }
                        else{
                            try {
                                sleep(100);
                            } catch (InterruptedException ex) {
                                Log.d(TAG, "Client Interrupted ?->" + ex.getMessage());
                            }
                        }
                        break;

                    case NETLISTEN:
                        smm = tqueue.poll();
                        if (smm != null){
                            cmd = smm.getCommand();
                            if (cmd == CMDS.LOADSTATE){
                                states_stack.push(smm.getState());
                            }
                        }
                        else {
                            try {
                                int available = bis.available();
                                if (available != 0) {
                                    //Log.d(TAG,"bytes on wire : " + available);
                                    for (int i = 0; i < available; i++) {
                                        if (mesptr < 162) {
                                            message[mesptr] = (byte) bis.read();
                                            //log.log(Level.FINE, "M byte ["+ mesptr+"] = " + message[mesptr]);
                                        }
                                        mesptr++;
                                    }
                                    //states_stack.push(STATES.RECVU);
                                    if (mesptr >= 162) {
                                        //log.log(Level.FINE, "Message received");
                                        states_stack.push(STATES.RECVU);
                                    } else {
                                        Log.d(TAG, "Truncated message, retry = " + retry);
                                        if (retry-- <= 0) {
                                            Log.d(TAG, "Unable to assemble message");
                                            states_stack.push(STATES.DETACH);
                                        }
                                    }
                                }
                            } catch (IOException ex) {
                                Log.d(TAG, "Error in network transaction ?-> " + ex.getMessage());
                                states_stack.push(STATES.IDLE);
                            }
                        }
                        break;

                    case PING:
                        AcpMessage pingm = new AcpMessage();
                        pingm.setCmd(comand_map.get("CALLBACK_REQ"));
                        pingm.setMessage("Status callback");
                        //m.setSize(0);
                        pingm.setOrig(0);
                        pingm.setDest(0);
                        try {
                            bos.write(pingm.getMessage());
                            bos.flush();
                            Log.d(TAG,  "Sending Ping : "+ pingm.toString());
                            mesptr=0;
                            retry = 4;
                            //TODO : adjust msize
                            states_stack.push(STATES.NETLISTEN);
                        } catch (IOException ex) {
                            Log.d(TAG, "IO Fault on PING request");
                            states_stack.push(STATES.DETACH);
                        }
                        break;

                    case IDLE:
                        smm = tqueue.poll();
                        if (smm != null){
                            cmd = smm.getCommand();
                            if (cmd == CMDS.LOADSTATE){
                                state = smm.getState();
                            }
                        }
                        break;

                    case TOOGLECB:
                        Log.d(TAG,"Toogling Callback.........................");

                        states_stack.push(STATES.IDLE);
                        break;

                    case RECVU:
                        AcpMessage rm = new AcpMessage(true, message);
                        if (rm.getCmd()== 20){
                            //Log.d(TAG, "Receiving Status @ recvu : " + rm.toString());
                            if (broadcast_rawstatus){
                                broadcastMessage("ACPMESSAGE", rm);
                            }
                            loadStatus(message);
                        }
                        else{
                            if (broadcast_traffic){
                                broadcastMessage("ACPMESSAGE", rm);
                            }
                            //Log.d(TAG, "Message Rcvd = " + rm.toString());
                        }
                        states_stack.push(STATES.NETCONNECTED);
                        break;

                    case SENDU:
                        //Log.d(TAG,  "Sending to network");
                        if(isAttached()){
                            AcpMessage mes = smm.getPayload();
                            try {
                                bos.write(mes.getMessage());
                                bos.flush();
                                Log.d(TAG, "Message Sent = "+ mes.toString());
                                //broadcastMessage("ACPMESSAGE", mes);
                            } catch (IOException ex) {
                                Log.d(TAG, "Client unable to decode ACPMessage");
                            }
                            mesptr=0;
                            retry=4;
                            states_stack.push(STATES.NETLISTEN);
                        }
                        else{
                            Log.d(TAG, "Unable to send, client is not connected");
                            states_stack.push(STATES.IDLE);
                        }
                        break;

                    case CONFIG:
                        //Log.d(TAG,  "Net Client State Machine in CONFIG");
                        break;

                    case RESET:
                        //Log.d(TAG,  "Net Client State Machine in RESET");
                        if(autoattach){
                            states_stack.push(STATES.ATTACH);
                            autoattach = false;
                        }
                        else{
                            states_stack.push(STATES.IDLE);
                        }
                        states_stack.push(STATES.CONFIG);
                        states_stack.push(STATES.INIT);
                        break;
                }
            }

        }

        public void setDone(boolean done) {
            if (done) Log.d(TAG,  "ClientNet Manager Stopping Service");
            this.done = done;
        }
    };


}
