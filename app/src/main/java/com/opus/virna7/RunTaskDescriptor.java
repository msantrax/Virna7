package com.opus.virna7;



public class RunTaskDescriptor {


    //public enum TASKMODE {POLISH, LAP, FLATNESS};
    //public enum GAUGETYPE {EMPTY, SPEED, TORQUE, SPIN, SWEEPPOS, WG2SPIN, MICRONS, TRAVEL};

    private static  RunTaskDescriptor instance;

    private int mode;
    private String pgmname;
    private int[] gauges = new int[4];


    public static  RunTaskDescriptor getPolishDescriptor(){

        if (instance == null){
            instance = new  RunTaskDescriptor();
            instance.mode = 0;
            instance.pgmname = "Polish1";
            instance.gauges[0] = GaugeType.Gauget.SPEED.getOrdinal();
            instance.gauges[1] = GaugeType.Gauget.TORQUE.getOrdinal();
            instance.gauges[2] = GaugeType.Gauget.EMPTY.getOrdinal();
            instance.gauges[3] = GaugeType.Gauget.EMPTY.getOrdinal();

        }

        return instance;
    }


    public RunTaskDescriptor() {
        mode = 0;
        pgmname = "Desbaste 7";
    }

    public int getGaugeType (int index) { return gauges[index];}




}
