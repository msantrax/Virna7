package com.opus.virna7;

import java.util.Locale;

/**
 * Created by opus on 12/01/17.
 */

public class AcpMessage{

    //public static enum MESSTYPE { RAW, STRING };

    private byte[] message = new byte[162];

    //private MESSTYPE type;

    private boolean out;

    private int csum;
    static final int pcsum = 0;

    private int size;
    static final int psize = pcsum+1;

    private long tstamp;
    static final int ptstamp = psize+1;

    private int cmd;
    static final int pcmd = ptstamp+3;

    private int arg;
    static final int parg = pcmd+1;

    private int seq;
    static final int pseq = parg+1;

    private int orig;
    static final int porig = pseq+2;

    private int dest;
    static final int pdest = porig+1;

    private int magic;
    static final int pmagic = pdest+1;

    static final int pdata = pmagic+1;



    public AcpMessage(boolean out, int orig, int dest, String scmd){

        //type = MESSTYPE.STRING;
        int mptr = pdata;
        csum = 0;
        this.out=out;

        storeTimestamp();

        // Store piping
        this.orig=orig;
        message[porig] = (byte)orig;
        this.dest = dest;
        message[pdest] = (byte)dest;

        // Store Type and config
        cmd = 0x05;
        message[pcmd] = 0x05;
        arg=0;
        message[parg] = 0x00;
        seq = 0;
        message[pseq] = 0x00;


        byte[] bcmd = scmd.getBytes();

        for (byte mpl : bcmd){
            message[mptr++] = mpl;
        }

        size = mptr-pdata;
        message[psize] = (byte)size;


        for (int i = 1; i<size+11; i++) {
            csum += message[i] & 0xff;
        }
        csum += csum & 0xff;
        message[pcsum] = (byte)csum;

        message[pmagic] = (byte) (cmd ^ 0xffffffff);

    }

    public void setMessage (String scmd){

        int mptr = pdata;
        csum = 0;

        byte[] bcmd = scmd.getBytes();

        for (byte mpl : bcmd){
            message[mptr++] = mpl;
        }

        size = mptr-pdata;
        message[psize] = (byte)size;


        for (int i = 1; i<size+11; i++) {
            csum += message[i] & 0xff;
        }
        csum += csum & 0xff;
        message[pcsum] = (byte)csum;

    }


    public AcpMessage(boolean out, byte[]mes){

        this.out = out;

        int meslenght = mes.length;
        //if (meslenght > 149) meslenght = 149;

        for (int i = 0; i <  meslenght; i++) {
            message[i] = mes[i];
        }

        tstamp= message[2] + (message[1]*256) + (message[0]*65536);
        cmd=message[pcmd];
        arg=message[parg];
        seq=(message[pseq]*256) + message[pseq+1];
        orig=message[porig];
        dest=message[pdest];
        size=message[psize];
        csum=message[pcsum];
        magic=message[pmagic];
    }


    public AcpMessage(){
        storeTimestamp();
    }


    private void storeTimestamp(){
        // Store ID/Timestamp
        tstamp = System.currentTimeMillis();
        long ts = tstamp;
        message[ptstamp+2] = (byte)ts;
        ts=tstamp>>8;
        message[ptstamp+1]= (byte)ts;
        ts=tstamp>>16;
        message[ptstamp] = (byte)ts;
    }

    public String getTimestamp(){

        String temp = String.format(Locale.US,"%02X%02X%02X",
                message[ptstamp],
                message[ptstamp+1],
                message[ptstamp+2]);
        return temp;
    }

    public String getStringPayload(){

        String payload;
        char[] chars = new char[150];
        char token;

        if (size == 127) size = 150;

        if (size >0){
            for (int i = 12; i < size + 12; i++) {
                token = (char)message[i];
                if (token > 32 && token < 126){
                    chars[i-12] = token;
                }
                else{
                    chars[i-12] = '|';
                }
            }
            payload = String.valueOf(chars, 0, size);
        }
        else{
            payload ="ACP message empty";
        }

        return payload;
    }

    @Override
    public String toString(){

        char[] chars = new char[150];
        char token;
        String payload;
        String sout ;
        String st = String.valueOf(System.currentTimeMillis());
        st = st.substring(st.length()-6);
        String dir = out ? "OUT" : " IN";

        if (size == 127) size = 150;

        if (size >0){
            for (int i = 12; i < size + 12; i++) {
                token = (char)message[i];
                if (token > 32 && token < 126){
                    chars[i-12] = token;
                }
                else{
                    chars[i-12] = '|';
                }
            }
            payload = String.valueOf(chars, 0, size);
        }
        else{
            payload ="";
        }


        //12345678 - OUT - 00 - 00 - 000000  --  00 - 00 - 00  --  00 - 00  --  00-00-00-00-00-00-00-00-00-00
        sout = String.format(Locale.US, "%s - %s - %02X - %02d - %02X%02X%02X -- %02X - %02x - %02X -- %02d - %02d [%s]",

                st,
                dir,
                message[pcsum],
                message[psize],

                message[ptstamp],
                message[ptstamp+1],
                message[ptstamp+2],
                cmd,
                arg,
                seq,
                orig,
                dest,
                payload
        );


        return sout;
    }

    public String getCmdString(){
        char[] chars = new char[53];
        for (int i = 12; i < 12 + size; i++) {
            chars[i-12] = (char)message[i];
        }
        return String.valueOf(chars, 0, size);
    }


    // Get Set stuff =====================================================================================
    public long getTstamp() {
        return tstamp;
    }

    public void setTstamp(long tstamp) {
        this.tstamp = tstamp;
    }

    public int getCmd() {
        return cmd;

    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
        message[pcmd] = (byte)cmd;
    }

    public int getArg() {
        return arg;
    }

    public void setArg(int arg) {
        this.arg = arg;
        message[parg] = (byte)arg;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
        message[pseq] = (byte)seq;
    }

    public int getOrig() {
        return orig;
    }

    public void setOrig(int orig) {
        this.orig = orig;
        message[porig] = (byte)orig;
    }

    public int getDest() {
        return dest;
    }

    public void setDest(int dest) {
        this.dest = dest;
        message[pdest] = (byte)dest;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        message[psize] = (byte)size;
    }

    public int getCsum() {
        return csum;
    }

    public void setCsum(int csum) {
        this.csum = csum;
        message[pcsum] = (byte)csum;
    }

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
        message[pmagic] = (byte)magic;
    }

    public byte[] getMessage() {
        return message;
    }
}

