package com.opus.virna7;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static android.view.Menu.NONE;


public class RunSimpleActivity extends AppCompatActivity implements
        SetValDialogFragment.OnSetValFragmentInteractionListener,
        SetTimeDialogFragment.OnSetTimeFragmentInteractionListener,
        SetCvexDialogFragment.OnSetCvexFragmentInteractionListener,
        ObjectAnimator.AnimatorListener {

    public static final String TAG = "RUNTASK";

    public static final int CTXMENUGAUGES = 256;

    private LinkedBlockingQueue<SMTraffic> run_smqueue;
    private RunThread runthread;

    private RunTaskRightGauges rtg;

    public EtherService EtherServiceBinder;
    public Intent i;
    public IntentFilter intentFilter;
    NotificationManager mNotificationManager;
    private SharedPreferences preferences;

    private TextView timergauge;
    private double[][] slots = new double[10][2];
    private long[] times = new long[2];
    private static final long toff = 10800000;
    private boolean userelative;
    private int defperiod;


    private double[] coefs = new double[3];
    private double[] limits = new double[2];
    private double[] recal = new double[2];
    boolean userecalflag = true;

    private String spref;
    private int platespeed;
    private int lastspeed;

    private int scantype;
    private int scaninner;
    private int scanouter;
    private int scanvel;
    private int scanposition;

    private int wg20dir;
    private int wg20vel;
    private boolean usedrum;

    private TextView gcvex;
    private TextView rpmtv;

    private double cvexvalue;
    private ImageView gpt;
    private ObjectAnimator cvexpt;
    private boolean ptmoving = false;
    double lastpt = 0f;


    private boolean taskrunning;

    private ImageButton ibsync;
    private Toolbar runtoolbar;
    private boolean rt_callbackenabled;

    private String runtimelabel;
    private boolean tlock;

    private ProfileEntry profile;
    private Virna7Application virna;

    private int rpmvalue;
    private RunSwipeTouchListener runtouchlistener;



    private ToggleButton drumtoggle;
    private ToggleButton wg2toggle;
    private ToggleButton acvttoggle;

    private SweepDescriptor sweepdescriptor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simplerun);


        virna = (Virna7Application) getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String s = getIntent().getStringExtra("profile");

        profile = virna.getProfilemanager().findProfileByName(s);

        RunTaskDescriptor td = RunTaskDescriptor.getPolishDescriptor();
        sweepdescriptor = new SweepDescriptor();

        ibsync = (ImageButton) findViewById(R.id.ib_sync);

        gcvex = (TextView) findViewById(R.id.gcvex);
        gpt = (ImageView) findViewById(R.id.gpt);


        cvexpt = ObjectAnimator.ofFloat(gpt, "translationX", 0, 0);
        cvexpt.setInterpolator(new AccelerateDecelerateInterpolator());
        cvexpt.setDuration(1000);
        cvexpt.addListener(this);

        timergauge = (TextView) findViewById(R.id.timer_message);


        run_smqueue = new LinkedBlockingQueue<>();
        loadDefaults();


        rpmtv = (TextView) findViewById(R.id.tv_rpm);
        rpmvalue=0;

        wg2toggle = (ToggleButton) findViewById(R.id.tb_wg2);
        wg2toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "WG2 is checked");
                } else {
                    Log.d(TAG, "WG2 is NOT checked");
                }
                setWG2(isChecked);
            }
        });


        drumtoggle = (ToggleButton) findViewById(R.id.tb_drum);
        drumtoggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    Log.d(TAG, "Drum is checked");
//                } else {
//                    Log.d(TAG, "Drum is NOT checked");
//                }
                setDrum(isChecked);
            }
        });

        acvttoggle = (ToggleButton) findViewById(R.id.tb_acvt);
        acvttoggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    Log.d(TAG, "ACVT is checked");
//                } else {
//                    Log.d(TAG, "ACVT is NOT checked");
//                }
                doOperation(0);
            }
        });


        Button rpmupbutton = (Button)findViewById(R.id.bt_rpmup);
        rpmupbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Log.d(TAG, "Increasing Vel by 1");
                setRpm(1);
            }
        });

        Button rpmdownbutton = (Button)findViewById(R.id.bt_rpmdown);
        rpmdownbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Log.d(TAG, "DEcreasing Vel by 1");
                setRpm(-1);
            }
        });


        Button rpmplusbutton = (Button)findViewById(R.id.bt_rpmplus);
        rpmplusbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Log.d(TAG, "DEcreasing Vel by 1");
                setRpm(10);
            }
        });

        Button rpmminusbutton = (Button)findViewById(R.id.bt_rpmminus);
        rpmminusbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Log.d(TAG, "DEcreasing Vel by 1");
                setRpm(-10);
            }
        });


        timergauge = (TextView) findViewById(R.id.timer_message);
        ImageView timerbkg = (ImageView) findViewById(R.id.timer_backgroung);
        runtouchlistener = new RunSimpleActivity.RunSwipeTouchListener(getBaseContext(), this, 0);
        timerbkg.setOnTouchListener(runtouchlistener);

        ImageView timericon  = (ImageView)findViewById(R.id.icn_timer);
        timericon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Log.d(TAG, "Timer ativado");
                runtouchlistener.setTimer();
            }
        });

        ImageView cvexicon  = (ImageView)findViewById(R.id.icn_cvex);
        cvexicon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Log.d(TAG, "CVEX ativado");
                callSweepDialog();
            }
        });




        runtoolbar = (Toolbar) findViewById(R.id.run_toolbar);
        setSupportActionBar(runtoolbar);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        loadPreferences();
        updateTimerLabel();
        setRpmLabel();

    }


    private void setRpm(int value){

        if (value == 0){
            platespeed = 0;
        }
        else if (value == 1){
            if (platespeed < 80) platespeed++;
        }
        else if (value == -1){
            if (platespeed > 0) platespeed--;
        }
        else if (value == 10){
            if (platespeed < 70) platespeed+=10;
        }
        else if (value == -10){
            if (platespeed > 10 ) platespeed-=10;
        }
        setRpmLabel();
    }

    private void setRpmLabel(){
        rpmtv.setText(String.format("%d r.p.m", platespeed));
    }


    private void updateSpeed(){
        if (platespeed != lastspeed){
            AcpMessage m1 = new AcpMessage(true, 3, 3, "");
            m1.setCmd(EtherService.comand_map.get("SERVO_SETSPEED"));
            m1.setArg(1);
            m1.setMessage(String.valueOf(platespeed));
            run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m1));
        }
        lastspeed = platespeed;
    }



    private void setDrum(boolean dset){

        AcpMessage m3 = new AcpMessage(true, 4, 4, "");
        m3.setCmd(EtherService.comand_map.get("MON_SETDRUM"));
        if (dset) {
            m3.setArg(1);
        }
        else{
            m3.setArg(0);
        }
        m3.setMessage("");
        run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m3));

        //drumtoggle.toggle();
    }

    private void setWG2(boolean dset){

        AcpMessage m2 = new AcpMessage(true, 4, 4,"");
        m2.setCmd(EtherService.comand_map.get("WG20_SETVEL"));
        if (dset) {
            m2.setArg(1);
        }
        else{
            m2.setArg(0);
        }
        m2.setMessage("");
        run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m2));
    }



    private void loadPreferences() {

        spref = preferences.getString(getString(R.string.pref_main_k_platespeed), "20");
        platespeed = Integer.valueOf(spref);
        lastspeed=0;

        spref = preferences.getString(getString(R.string.pref_main_k_wg20vel), "0");
        wg20vel = Integer.valueOf(spref);
        spref = preferences.getString(getString(R.string.pref_main_k_wg20dir), "0");
        wg20dir = Integer.valueOf(spref);

        userelative = preferences.getBoolean(getString(R.string.pref_main_k_useperiod), true);
        spref = preferences.getString(getString(R.string.pref_main_k_defperiod), "45");
        defperiod = Integer.valueOf(spref);
        if (userelative) {
            times[1] = (defperiod * 60) * 1000;
        }

        usedrum = preferences.getBoolean(getString(R.string.pref_main_k_usedrum), true);

        spref = preferences.getString(getString(R.string.pref_mon_k_lvdta0), "-11.1102432");
        coefs[0] = Double.valueOf(spref);

        spref = preferences.getString(getString(R.string.pref_mon_k_lvdta1), "0.00032752");
        coefs[1] = Double.valueOf(spref);

        spref = preferences.getString(getString(R.string.pref_mon_k_lvdta2), "0");
        coefs[2] = Double.valueOf(spref);

        spref = preferences.getString(getString(R.string.pref_mon_k_lvdtmin), "-10");
        limits[0] = Double.valueOf(spref);

        spref = preferences.getString(getString(R.string.pref_mon_k_lvdtmax), "10");
        limits[1] = Double.valueOf(spref);

        spref = preferences.getString(getString(R.string.pref_mon_k_lvdtoffset), "0");
        recal[0] = Double.valueOf(spref);

        spref = preferences.getString(getString(R.string.pref_mon_k_lvdtslope), "1");
        recal[1] = Double.valueOf(spref);

        userecalflag = preferences.getBoolean(getString(R.string.pref_mon_k_lvdtuserecal), true);
    }

    public void savePreferences() {

    }


    public void changeAction(View view) {
        Log.i(TAG, "Context Requested");
        openContextMenu(view);
    }


    public void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming Run Task");

        // Start connection with EtherServices
        intentFilter = new IntentFilter();
        intentFilter.addAction("ACPMESSAGE");
        intentFilter.addAction("COMMANDCALLBACK");
        intentFilter.addAction("COMMANDACK");

        registerReceiver(intentReceiver, intentFilter);

        mNotificationManager.cancelAll();

        if (EtherServiceBinder == null) {
            startEtherService();
        }

        // Start Run services
        if (runthread == null) {
            runthread = new RunThread(run_smqueue);
        }
        run_smqueue.clear();
        new Thread(runthread).start();
    }


    public void onPause() {

        super.onPause();

        Intent intent = getIntent();
        Log.d(TAG, "Pausing Run Task DUE : " + intent);
        runthread.setDone(true);

        unregisterReceiver(intentReceiver);
        if (EtherServiceBinder != null) {
            //unbindService(connection);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //Log.e(TAG, "Intercepting buttons" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK) {
            if (taskrunning) {
                Toast.makeText(this, "Ooops !! - A atividade está em funcionamento - O logout não foi autorizado", Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    private void callSweepDialog(){
        SetCvexDialogFragment setFragment = SetCvexDialogFragment.newInstance(this, sweepdescriptor);
        setFragment.addListener(this);
        setFragment.show(getFragmentManager(), "sweepdialog");
    }


    public void onSetCvexFragmentInteraction() {


        AcpMessage m;

        scantype = sweepdescriptor.getAlgo();
        scaninner = sweepdescriptor.getInner();
        scanouter = sweepdescriptor.getOuter();
        scanvel = sweepdescriptor.getVel();
        scanposition = sweepdescriptor.getPosition();


        Log.d(TAG, "Setsweep returned : " + scantype + " - " + scaninner + " - " + scanvel + " - " + scanouter + " - " + scanposition );


        //if (taskrunning) {
            if (scantype == 0) {
                AcpMessage m0 = new AcpMessage(true, 2, 2, "");
                m0.setCmd(EtherService.comand_map.get("SWEEP_ENABLE"));
                m0.setArg(1);
                m0.setMessage("");
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m0));

                AcpMessage m1 = new AcpMessage(true, 2, 2, "");
                m1.setCmd(EtherService.comand_map.get("SWEEP_SCAN"));
                m1.setArg(1);
                m1.setMessage(String.valueOf(scaninner) + " " +
                        String.valueOf(scanouter) + " " +
                        String.valueOf(scanvel));
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m1));

            } else if (scantype == 5) {
                AcpMessage m2 = new AcpMessage(true, 2, 2,"");
                m2.setCmd(EtherService.comand_map.get("SWEEP_ENABLE"));
                m2.setArg(0);
                m2.setMessage("");
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m2));

                AcpMessage m3 = new AcpMessage(true, 2, 2,"");
                m3.setCmd(EtherService.comand_map.get("SWEEP_SCAN"));
                m3.setArg(0);
                m3.setMessage("");
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m3));
            }
        //}
    }

    // Ether Service Binding ===============================================================================================

    private ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "Run Task is binding to EtherService");
            EtherServiceBinder = ((EtherService.EtherBinder) service).getService();
            startService(i);
            //EtherServiceBinder.notifyStatus(EtherServiceBinder.isAttached());
        }

        public void onServiceDisconnected(ComponentName className) {
            EtherServiceBinder = null;
        }
    };

    private void startEtherService() {
        Log.d(TAG, " Run Task is starting Coms Service -->");
        i = new Intent(getBaseContext(), EtherService.class);
        bindService(i, connection, Context.BIND_AUTO_CREATE);
    }



    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Log.e(TAG, "Run task is Receiving messa from Service");
            String action = intent.getAction();
            if ("ACPMESSAGE".equals(action)) {
                //Log.e(TAG, "Received ACPMESSAGE");
                byte[] mesbytes = intent.getByteArrayExtra("ACPM_BYTES");
                if (mesbytes != null) {
                    AcpMessage m = new AcpMessage(false, mesbytes);
                    run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.RECVU, m));
                } else {
                    Log.e(TAG, "Failed to parse ACPM_BYTES");
                }
            } else if ("COMMANDCALLBACK".equals(action)) {

                String extra = intent.getStringExtra("String");
                Log.e(TAG, "Run task is Receiving  Commad callback" + extra);

                if ("CALLBACKENABLED".equals(extra)) {
                    rt_callbackenabled = true;
                    Toast.makeText(context, "O Monitoramento foi Habilitado", Toast.LENGTH_LONG).show();
                }
                if ("CALLBACKDISABLED".equals(extra)) {
                    rt_callbackenabled = false;
                    Toast.makeText(context, "Monitoramento Desabilitado", Toast.LENGTH_LONG).show();
                }
                //Log.d(TAG, "CONNAMD CALLBACK RETURNED : " + intent.getStringExtra("String"));
            } else if ("COMMANDACK".equals(action)) {
                int cmd_code = intent.getIntExtra("CODE", 0);
                if (cmd_code == EtherService.comand_map.get("NET_ATTACHED")) {
                    Log.d(TAG, "NET Attached informs : " + intent.getStringExtra("VALUE"));
                } else if (cmd_code == EtherService.comand_map.get("NET_CALLBACKENABLED")) {
                    Log.d(TAG, "NET callback enabled informs : " + intent.getStringExtra("VALUE"));
                } else if (cmd_code == EtherService.comand_map.get("NET_STATUSARRIVED")) {
                    //Log.d(TAG, "NET callback enabled informs : Status Arrived ");
                    //ibsync.setBackgroundColor();
                    ibsync.setEnabled(!ibsync.isEnabled());
                }
            } else {
                Log.e(TAG, "Undetermined Service message received : " + action);
            }

        }
    };

    // Context Services ===================================================================================================
    public boolean runService(boolean onlyui) {

        long now;
        boolean state = true;

        if (!onlyui) {
            now = System.currentTimeMillis();
            //Log.i(TAG, "Run service : " + (now - 1484659000000L) + " = " + (times[1] - 1484659000000L));

            if (taskrunning) {
                if (now > times[1]) {
                    state = false;
                    runtimelabel = (String.format("Atividade foi encerrada as %1$tH:%1$tM:%1$tS", now));
                } else {
                    runtimelabel = String.format("Tempo para o final : %1$tH:%1$tM:%1$tS [as %2$tH:%2$tM:%2$tS] ", (times[1] - now) + toff, times[1]);
                }
            } else {
                runtimelabel = ("Aguardando comando para iniciar tarefa");
            }
        }

        updateGauges();
        return state;
    }

    public void updateGauges() {



        if (!tlock) {
            //Log.i(TAG, "Updating gauges");
            tlock = true;

            // Do UI refresh
            runOnUiThread(new Runnable() {

                double cvexval;
                double cvexraw;

                @Override
                public void run() {

                    double ptvalue;

                    int gaugeindex;
                    timergauge.setText(runtimelabel);



                    // Update cvex
                    if (EtherServiceBinder != null) {
                        cvexraw = EtherServiceBinder.getStatusSlot(4);
                        cvexval = calculateCVex(cvexraw);
                        //Log.e(TAG, "CVex values : " + cvexraw + " / " + cvexval);
//                        if (cvexval < limits[0]) {
//                            cvexval = limits[0];
//                        } else if (cvexval > limits[1]) {
//                            cvexval = limits[1];
//                        }
                        slots[8][1] = cvexval;
                    } else {
                        slots[8][1] = 0.02;
                    }
                    gcvex.setText(String.format("%2.2f µ", slots[8][1]));
                    if (!ptmoving) {
                        ptvalue = slots[8][1] * 15;
                        cvexpt.setFloatValues((float) lastpt, (float) ptvalue);
                        lastpt = ptvalue;
                        ptmoving = true;
                        cvexpt.start();
                    }
                    tlock = false;
                }
            });

        }
    }

    private double calculateCVex(double raw) {
        double res = ((raw * raw) * coefs[2]) + (raw * coefs[1]) + coefs[0];
        if (userecalflag) {
            res = (res * recal[1]) + recal[0];
        }
        return res;
    }


    // EVENTS CALLBACK HOOK =======================================================================
    @Override
    public void onSetValFragmentInteraction(SetValDescriptor descriptor) {
        AcpMessage m;
        AcpMessage n;

        Log.d(TAG, "Setval type " + descriptor.getType() + " returned with " + descriptor.getValue());

        int dtype = descriptor.getType();

        if (dtype == 5) { // WG20
            wg20dir = descriptor.getDirection();
            wg20vel = descriptor.getValue();
            if (taskrunning && (wg20vel != 0)) {
                AcpMessage m2 = new AcpMessage(true, 4, 4, "");
                m2.setCmd(EtherService.comand_map.get("WG20_SETVEL"));
                m2.setArg(wg20dir);
                m2.setMessage(String.valueOf(wg20vel));
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m2));
            }
        } else if (dtype == 1) { // Vel
            platespeed = descriptor.getValue();
            if (taskrunning) {
                AcpMessage m1 = new AcpMessage(true, 3, 3, "");
                m1.setCmd(EtherService.comand_map.get("SERVO_SETSPEED"));
                m1.setArg(1);
                m1.setMessage(String.valueOf(platespeed));
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m1));
            }
        }
        //slots [descriptor.getType()] [0] = descriptor.getValue();

    }

    @Override
    public void onSetTimeFragmentInteraction(long stime, boolean userel) {

        userelative = userel;
        times[1] = stime;
        if (userelative) {
            Log.d(TAG, "SetTime returned relative  to " + stime + " miliseconds");
        } else {
            Log.d(TAG, "SetTime returned time set to " + String.format("%1$tD - %1$tT", stime));
        }

        updateTimerLabel();

    }

    public void updateTimerLabel(){

        final long toffset = (60000 * 60) * 21;

        if (userelative){
            timergauge.setText(String.format("Período ajustado para : %1$tH:%1$tM:%1$tS (Hr:Min:Seg)", (times[1]-toffset)));
        }
        else{
            timergauge.setText(String.format("Atividade encerra as %1$tH:%1$tM:%1$tS ", times[1]));
        }

    }


    // Operations Context menu services =====================================================================================
    public void doOperation(int optype) {
        //Log.i(TAG, "Operation requested : " + optype);
        AcpMessage m;

        switch (optype) {

            case 0: // Start / Stop Run
                //if (!rt_callbackenabled) EtherServiceBinder.doComand(EtherService.STATES.TOOGLECB, 0);
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.MANAGETASK));
                break;

            case 1: // Drum control
                usedrum = !usedrum;
                if (taskrunning) {
                    AcpMessage m3 = new AcpMessage(true, 4, 4, "");
                    m3.setCmd(EtherService.comand_map.get("MON_SETDRUM"));
                    if (usedrum) {
                        m3.setArg(1);
                    } else {
                        m3.setArg(0);
                    }
                    m3.setMessage("");
                    run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m3));
                }
                Toast.makeText(this, "Ajustando Tambor de abrasivo", Toast.LENGTH_LONG).show();
                break;

            case 2: // Vacuo
                m = new AcpMessage(true, 2, 4, "");
                m.setCmd(44);
                m.setArg(32);
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m));
                Toast.makeText(this, "Ajustando Bomba de Vácuo", Toast.LENGTH_LONG).show();
                break;

            case 3:
                EtherServiceBinder.doComand(EtherService.STATES.TOOGLECB, 0);
                Toast.makeText(this, "Habilitando Monitoramento", Toast.LENGTH_LONG).show();
                break;

            case 4: //Config load
                Toast.makeText(this, "(Re)Carregando Valores Pré-definidos de Trabalho", Toast.LENGTH_LONG).show();
                loadPreferences();
                break;

            case 5: //Config Save
                Toast.makeText(this, "A gravação da configuração está desabilitada nessa versão", Toast.LENGTH_LONG).show();
                break;

            default:
                Log.e(TAG, "Undetermined Operation requested : " + optype);
                break;
        }

    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        //Log.e(TAG, "Animation Ended");
        ptmoving = false;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    public void setSupportActionBar(Toolbar supportActionBar) {
        //this.supportActionBar = supportActionBar;
    }


    public static class OperationsDialogFragment extends DialogFragment {

        private static RunSimpleActivity racvty;

        private static String[] op = {
                "",
                "",
                "Ativar / Desativar Vãcuo",
                "Habilitar Monitoramento",
                "Reiniciar Perfil",
                "Carregar Perfil",
        };

        public static OperationsDialogFragment newInstance(RunSimpleActivity run) {
            OperationsDialogFragment fragment = new OperationsDialogFragment();
            racvty = run;
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {


            if (racvty.taskrunning) {
                op[0] = "Parar Atividade";
            } else {
                op[0] = "Iniciar Atividade";
            }

            if (racvty.usedrum) {
                op[1] = "Desligar Tambor de Abrasivo";
            } else {
                op[1] = "Ativar Tambro de Abrasivo";
            }


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Selecione a Operação Desejada")
                    .setIcon(R.drawable.clock1)
                    .setItems(op, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Log.i(TAG, "Doing task num : "+ which);
                            racvty.doOperation(which);
                        }
                    });
            return builder.create();
        }
    }


    // Error Dialog services ===============================================================================
    public static class OPErrorDialogFragment extends DialogFragment {

        private static String message;
        private static RunSimpleActivity racvty;

        public static OPErrorDialogFragment newInstance(String mes, RunSimpleActivity run) {
            OPErrorDialogFragment fragment = new OPErrorDialogFragment();
            message = mes;
            racvty = run;
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Oops !! - Problemas na operação")
                    .setIcon(R.drawable.clock1)
                    .setMessage(message)
                    .setPositiveButton("Positivo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
            ;
            return builder.create();
        }

        public void onDismiss(DialogInterface dialog) {
            racvty.recoverRunErrorMessage();
        }
    }


    public void showRunErrorMessage(String message) {

        OPErrorDialogFragment dialog = OPErrorDialogFragment.newInstance(message, this);
        dialog.show(getFragmentManager(), "OperationsErrorFragment");
    }

    public void recoverRunErrorMessage() {
    }


    public void startLGTask() {

        AcpMessage m0 = new AcpMessage(true, 3, 3, "");
        m0.setCmd(EtherService.comand_map.get("SERVO_ENABLE"));
        m0.setArg(1);
        m0.setMessage("0");
        run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m0));

        AcpMessage m1 = new AcpMessage(true, 3, 3, "");
        m1.setCmd(EtherService.comand_map.get("SERVO_SETSPEED"));
        m1.setArg(1);
        m1.setMessage(String.valueOf(platespeed));
        run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m1));


    }

    public void stopLGTask() {

        AcpMessage m0 = new AcpMessage(true, 3, 3, "");
        m0.setCmd(EtherService.comand_map.get("SERVO_ENABLE"));
        m0.setArg(0);
        m0.setMessage("0");
        run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m0));

        AcpMessage m1 = new AcpMessage(true, 3, 3, "");
        m1.setCmd(EtherService.comand_map.get("SERVO_SETSPEED"));
        m1.setArg(0);
        m1.setMessage("0");
        run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m1));
    }


    //                                  MAIN STATE MACHINE
    // =============================================================================================
    private class RunThread extends Thread {

        private EtherService.STATES state;
        private boolean done;
        protected BlockingQueue<SMTraffic> tqueue;
        private EtherService.CMDS cmd;
        private ArrayDeque<EtherService.STATES> states_stack;
        private SMTraffic smm;
        StringBuilder sb = new StringBuilder();
        char[] chars = new char[53];

        int tempcounter;

        public RunThread(BlockingQueue<SMTraffic> tqueue) {
            this.tqueue = tqueue;
            states_stack = new ArrayDeque<>();
            states_stack.push(EtherService.STATES.RESET);
            setDone(false);
            tempcounter = 0;
        }

        private void requestCallback(){
            AcpMessage m = new AcpMessage();
            m.setCmd(0);
            m.setMessage("Status req");
            //m.setSize(0);
            m.setOrig(0);
            m.setDest(0);
            EtherServiceBinder.sendMessage(m);
        }

        @Override
        public void run() {

            Log.d(TAG, "Starting Run Task Service Thread");
            states_stack.clear();
            states_stack.push(EtherService.STATES.RESET);
            setDone(false);

            try {
                while (!done) {

                    if (!states_stack.isEmpty()) state = states_stack.pop();

                    switch (state) {

                        case INIT:
                            Log.d(TAG, "Runtask in INIT");
                            break;

                        case IDLE:
                            smm = tqueue.poll();
                            if (smm != null) {
                                cmd = smm.getCommand();
                                if (cmd == EtherService.CMDS.LOADSTATE) {
                                    states_stack.push(EtherService.STATES.IDLE);
                                    states_stack.push(smm.getState());
                                }
                            } else {
                                sleep(300);
                            }
                            break;

                        case ONERROR:
                            smm = tqueue.poll();
                            if (smm != null) {
                                cmd = smm.getCommand();
                                if (cmd == EtherService.CMDS.RECOVERERROR) {
                                    Log.d(TAG, "Recovering from Error");
                                    if (states_stack.isEmpty()) {
                                        Log.e(TAG, "Error recovering from error ! - going IDLE");
                                        state = EtherService.STATES.IDLE;
                                    }
                                } else if (cmd == EtherService.CMDS.LOADSTATE) {
                                    states_stack.push(EtherService.STATES.ONERROR);
                                    states_stack.push(smm.getState());
                                }
                            } else {
                                states_stack.push(EtherService.STATES.ONERROR);
                            }
                            break;

                        case MANAGETASK:
                            if (taskrunning) {
                                taskrunning = false;
                                runtimelabel = (String.format("Encerrada pelo usuário as %1$tH:%1$tM:%1$tS", System.currentTimeMillis()));
                                runService(true);
                                stopLGTask();
                                states_stack.clear();
                                states_stack.push(EtherService.STATES.IDLE);
                            } else {
                                sb = new StringBuilder();
                                if (userelative) {
                                    times[1] = System.currentTimeMillis() + times[1];
                                }
                                if (platespeed == 0) { // speed <
                                    sb.append("A Velocidade não foi especificada\n\r");
                                }
                                if (times[1] < System.currentTimeMillis() + 1000) { // time
                                    sb.append("O periodo da atividade não foi especificado\n\r");
                                }

                                if (sb.length() == 0) {
                                    Log.d(TAG, "Iniciando Tarefa");
                                    startLGTask();
                                    tlock = false;
                                    taskrunning = true;
                                    states_stack.push(EtherService.STATES.RUNNINGTASK);
                                } else {
                                    showRunErrorMessage(sb.toString());
                                    states_stack.push(EtherService.STATES.IDLE);
                                    states_stack.push(EtherService.STATES.ONERROR);
                                }
                            }
                            break;

                        case RUNNINGTASK:

                            smm = tqueue.poll();
                            if (smm != null) {
                                cmd = smm.getCommand();
                                if (cmd == EtherService.CMDS.LOADSTATE) {
                                    states_stack.push(EtherService.STATES.RUNNINGTASK);
                                    states_stack.push(smm.getState());
                                }
                            } else {
                                boolean run = runService(false);
                                if (run) {
                                    states_stack.push(EtherService.STATES.RUNNINGTASK);
                                    sleep(2000);
                                    updateSpeed();
                                    requestCallback();
                                } else {
                                    states_stack.push(EtherService.STATES.MANAGETASK);
                                }
                            }
                            break;

                        case RECVU:
                            //Log.d(TAG, "Receiving from USB Sservice");
//                            AcpMessage rmes = smm.getPayload();
//                            if (rmes.getCmd() == comand_map.get("CALLBACK_NONE")){
//                                parseStatusCallback(rmes.getCmdString());
//                            }

                            break;

                        case SENDU:
                            Log.d(TAG, "Transmiting to EtherService");
                            AcpMessage acpm = smm.getPayload();
                            EtherServiceBinder.sendMessage(acpm);
                            state = EtherService.STATES.IDLE;
                            break;

                        case CONFIG:
                            //Log.d(TAG, "RUN TASK in CONFIG");
                            states_stack.push(EtherService.STATES.IDLE);
                            break;

                        case RESET:
                            //Log.d(TAG, "RUNTASK in RESET");
                            states_stack.push(EtherService.STATES.CONFIG);
                            states_stack.push(EtherService.STATES.INIT);
                            break;
                    }
                }
            } catch (InterruptedException ex) {
                Log.e(TAG, ex.toString());
            }

        }

        public void setDone(boolean done) {
            if (done) Log.d(TAG, "Run Task Stopping Service");
            this.done = done;
        }


    }

    private class RunSwipeTouchListener extends RunTaskSwipeTouchListener {

        RunSimpleActivity acvty;
        int index;


        public RunSwipeTouchListener(Context ctx, RunSimpleActivity acvty, int index) {
            super(ctx);
            this.acvty = acvty;
            this.index = index;
        }

        @Override
        public void onSwipeRight() {
            times[1] += 300000L;
            Toast.makeText(acvty, "Periodo da atividade foi incrementado em 5 minutos", Toast.LENGTH_SHORT).show();
            updateTimerLabel();
        }

        @Override
        public void onSwipeLeft() {
            if ( times[1] >= 300000L) {
                times[1] -= 300000L;
                Toast.makeText(acvty, "Periodo da atividade foi reduzido em 5 minutos", Toast.LENGTH_SHORT).show();
                updateTimerLabel();
            }
        }

        public void setTimer(){
            if (taskrunning) {
                Toast.makeText(acvty, "Atividade em funcionamento - O Ajuste foi desativado", Toast.LENGTH_LONG).show();
            } else {
                SetTimeDialogFragment setFragment = SetTimeDialogFragment.newInstance(userelative, times[1]);
                setFragment.addListener(acvty);
                setFragment.show(getFragmentManager(), "timedialog");
            }
        }
    };




    private void loadDefaults() {

        taskrunning = false;

        // Velocidade
        slots[1][0] = 0;
        slots[1][1] = 0;

        // Torque
        slots[2][0] = 0;
        slots[2][1] = 0;

        // Jig Spin
        slots[3][0] = 0;
        slots[3][1] = 0;

        // Sweep Pos
        slots[4][0] = 50;
        slots[4][1] = 45.6;

        // WG2 Spin
        slots[5][0] = 20;
        slots[5][1] = 0;

        // Microns
        slots[6][0] = 1.5;
        slots[6][1] = 1.5743;

        // Drop
        slots[7][0] = 2.7;
        slots[7][1] = 2.059;

        // CVex
        slots[8][0] = 1.5;
        slots[8][1] = 0;


        times[0] = System.currentTimeMillis();
        times[1] = times[0] + 60000;
        userelative = true;

        runService(false);
    }

}