package com.opus.virna7;

/**
 * Created by opus on 12/01/17.
 */

public class SMTraffic {

    private EtherService.CMDS command;
    private int code;
    private EtherService.STATES state;
    private AcpMessage payload;

    public SMTraffic(EtherService.CMDS command, int code,EtherService.STATES state, AcpMessage payload) {
        this.command = command;
        this.code = code;
        this.state=state;

        this.payload = payload;

    }

    public SMTraffic(EtherService.CMDS command, int code,EtherService.STATES state, byte[] mesbytes) {

        this.command = command;
        this.code = code;
        this.state=state;

        this.payload = new AcpMessage(true, mesbytes);

    }

    public SMTraffic(EtherService.CMDS command, int code,EtherService.STATES state) {
        this.command = command;
        this.code = code;
        this.state=state;

        //this.payload = new AcpMessage(true,0,0,"");

    }


    public EtherService.CMDS getCommand() {
        return command;
    }

    public int getCode() {
        return code;
    }

    public EtherService.STATES getState() {
        return state;
    }

    public AcpMessage getPayload() {
        return payload;
    }



}
