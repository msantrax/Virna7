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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static android.R.drawable.ic_popup_sync;
import static android.view.Menu.NONE;


public class RunActivity extends AppCompatActivity implements
        SetValDialogFragment.OnSetValFragmentInteractionListener,
        SetTimeDialogFragment.OnSetTimeFragmentInteractionListener,
        SetSweepDialogFragment.OnSetSweepFragmentInteractionListener,
        ObjectAnimator.AnimatorListener
    {

    public static final String TAG = "RUNTASK";

    public static final int CTXMENUGAUGES = 256;

    private LinkedBlockingQueue<SMTraffic> run_smqueue;
    private RunThread runthread;

    private RunTaskRightGauges rtg;

    public EtherService EtherServiceBinder;
    public Intent i;
    public IntentFilter intentFilter;
    NotificationManager mNotificationManager;
    private SharedPreferences preferences ;



    private GaugeType gtype;
    private double[][] slots = new double[10][2];
    private long[] times = new long[2];
    private static final long toff =  10800000;
    private boolean userelative;
    private int defperiod;
    private TextView timergauge;

    private double[] coefs = new double[3];
    private double[] limits = new double[2];
    private double[] recal = new double[2];
    boolean userecalflag = true;  

    private String spref;
    private int platespeed;
    private int scantype;
    private int scaninner;
    private int scanouter;
    private int scanvel;
    private int scanposition;
    private int wg20dir;
    private int wg20vel;
    private boolean usedrum;

    private TextView gcvex;
    private double cvexvalue;
    private ImageView gpt;
    private ObjectAnimator cvexpt;
    private boolean ptmoving = false;
    double lastpt = 0f;


    private boolean taskrunning;
    private boolean rt_callbackenabled;
    private ImageButton ibsync;
    private android.support.v7.widget.Toolbar runtoolbar;


    private String runtimelabel;
    private boolean tlock;

    private XYPlot plot;
    private Redrawer redrawer;
    private GaugePlotModel[] series = new GaugePlotModel[4];

    private SweepDescriptor sweepdescriptor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        RunTaskDescriptor td = RunTaskDescriptor.getPolishDescriptor();
        sweepdescriptor = new SweepDescriptor();


        //TextView tvpgm = (TextView) findViewById(R.id.tv_program);
        //tvpgm.setText("Desbaste Default");
        //registerForContextMenu(tvpgm);

        ibsync = (ImageButton) findViewById(R.id.ib_sync);


        gcvex = (TextView) findViewById(R.id.gcvex);
        gpt = (ImageView) findViewById(R.id.gpt);
        cvexpt = ObjectAnimator.ofFloat(gpt, "translationX", 0, 0);
        cvexpt.setInterpolator(new AccelerateDecelerateInterpolator());
        cvexpt.setDuration(1000);
        cvexpt.addListener(this);

        timergauge = (TextView) findViewById(R.id.timer_message);
        FrameLayout fmtime = (FrameLayout) findViewById(R.id.fm_time);
        fmtime.setOnTouchListener(new RunSwipeTouchListener(getBaseContext(), this, 0));

        rtg = new RunTaskRightGauges((TableLayout) findViewById(R.id.gl), td, this);
        gtype=rtg.getGaugeDescriptor();
        run_smqueue = new LinkedBlockingQueue<>();
        loadDefaults();

        plot = (XYPlot) findViewById(R.id.wplot);
        SetPlotter(1200);

        final Button button = (Button) findViewById(R.id.bt_operations);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Operations called");
                doTask();
            }
        });

        final FrameLayout fm_cvex = (FrameLayout) findViewById(R.id.fm_cvex);
        fm_cvex.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Cvex called");
               callSweepDialog();
            }
        });

        runtoolbar = (Toolbar) findViewById(R.id.run_toolbar);
        setSupportActionBar(runtoolbar);


        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        loadPreferences();
    }

    private void loadPreferences(){

        spref = preferences.getString(getString(R.string.pref_main_k_platespeed), "20");
        platespeed = Integer.valueOf(spref);
        spref = preferences.getString(getString(R.string.pref_main_k_scantype), "0");
        scantype = Integer.valueOf(spref);
        spref = preferences.getString(getString(R.string.pref_main_k_scaninner), "20");
        scaninner = Integer.valueOf(spref);
        spref = preferences.getString(getString(R.string.pref_main_k_scanouter), "80");
        scanouter = Integer.valueOf(spref);
        spref = preferences.getString(getString(R.string.pref_main_k_scanposition), "45");
        scanposition = Integer.valueOf(spref);
        spref = preferences.getString(getString(R.string.pref_main_k_wg20vel), "0");
        wg20vel = Integer.valueOf(spref);
        spref = preferences.getString(getString(R.string.pref_main_k_wg20dir), "0");
        wg20dir = Integer.valueOf(spref);

        userelative = preferences.getBoolean(getString(R.string.pref_main_k_useperiod), true);
        spref = preferences.getString(getString(R.string.pref_main_k_defperiod), "45");
        defperiod = Integer.valueOf(spref);
        if(userelative){
            times[1] = (defperiod*60) * 1000;
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

    public void savePreferences(){





    }


    private void callSweepDialog(){
        SetSweepDialogFragment setFragment = SetSweepDialogFragment.newInstance(this, sweepdescriptor);
        setFragment.addListener(this);
        setFragment.show(getFragmentManager(), "sweepdialog");
    }

    public void doTask() {
        DialogFragment dialog = OperationsDialogFragment.newInstance(this);
        dialog.show(getFragmentManager(), "OperationsDialogFragment");
    }


    public void changeAction(View view) {
        Log.i(TAG, "Context Requested");
        openContextMenu(view);
    }


    public void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming Run Task");

//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setLogo(R.mipmap.acp_launcher);
//        getSupportActionBar().setDisplayUseLogoEnabled(true);


        // Initiate status
        rt_callbackenabled = false;

        // Start connection with EtherServices
        intentFilter = new IntentFilter();
        intentFilter.addAction("ACPMESSAGE");
        intentFilter.addAction("COMMANDCALLBACK");
        intentFilter.addAction("COMMANDACK");

        registerReceiver(intentReceiver, intentFilter);

        mNotificationManager.cancelAll();

        if (EtherServiceBinder == null){
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
    public boolean onKeyDown (int keyCode, KeyEvent event) {

        //Log.e(TAG, "Intercepting buttons" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK) {
            if (taskrunning){
                Toast.makeText(this, "Ooops !! - A atividade está em funcionamento - O logout não foi autorizado", Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return super.onKeyDown( keyCode, event );
    }


    // USB Service Binding ===============================================================================================

    private ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "Run Task is binding to EtherService" );
            EtherServiceBinder = ((EtherService.EtherBinder)service).getService();
            startService(i);
            //EtherServiceBinder.notifyStatus(EtherServiceBinder.isAttached());
        }
        public void onServiceDisconnected(ComponentName className) {
            EtherServiceBinder = null;
        }
    };

    private void startEtherService(){
        Log.d(TAG, " Run Task is starting USB Service -->");
        i = new Intent(getBaseContext(), EtherService.class);
        bindService(i, connection, Context.BIND_AUTO_CREATE);
    }

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Log.e(TAG, "Run task is Receiving messa from Service");
            String action = intent.getAction();
            if ("ACPMESSAGE".equals(action)){
                byte[] mesbytes = intent.getByteArrayExtra("ACPM_BYTES");
                if (mesbytes !=null) {
                    AcpMessage m = new AcpMessage(false, mesbytes);
                    run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.RECVU, m));
                }
                else{
                    Log.e(TAG, "Failed to parse ACPM_BYTES");
                }
            }
            else if ("COMMANDCALLBACK".equals(action)){

                String extra = intent.getStringExtra("String");
                //Log.e(TAG, "Run task is Receiving  Commad callback" + extra);

                if ("CALLBACKENABLED".equals(extra)){
                    rt_callbackenabled = true;
                    Toast.makeText(context, "O Monitoramento foi Habilitado", Toast.LENGTH_LONG).show();
                }
                if ("CALLBACKDISABLED".equals(extra)){
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
            }
            else{
                Log.e(TAG, "Undetermined Service message received : " + action);
            }

        }
    };

    // Context Services ===================================================================================================
    public boolean runService(boolean onlyui){

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
            tlock=true;

            // Do UI refresh
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    double ptvalue;

                    int gaugeindex;
                    timergauge.setText(runtimelabel);

                    if (EtherServiceBinder != null) {
                        slots[1][1] = EtherServiceBinder.getStatusSlot(0);
                        slots[2][1] = EtherServiceBinder.getStatusSlot(1);
                        slots[7][1] = EtherServiceBinder.getStatusSlot(5);
                        slots[4][1] = EtherServiceBinder.getStatusSlot(2);
                    } else {
                        slots[1][1] = 0.0; //vel
                        slots[2][1] = 0.0; //torque
                        slots[7][1] = 0.0; //drop
                        slots[7][1] = 50.0; //delta
                    }

                    for (int j = 0; j < 4; j++) {
                        gaugeindex = rtg.getInstalled(j);
                        if (gaugeindex != 0) {
                            rtg.updateRTGauge(j, slots[gaugeindex][1]);
                        }
                    }

                    // Update cvex
                    if (EtherServiceBinder != null) {
                        double cvexval = calculateCVex(EtherServiceBinder.getStatusSlot(4));
                        if (cvexval < limits[0]) {
                            cvexval = limits[0];
                        } else if (cvexval > limits[1]) {
                            cvexval = limits[1];
                        }
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

                    // Update Graf
                    for (int k = 0; k < 4; k++) {
                        if (series[k] != null) {
                            gaugeindex = series[k].getSlot();
                            series[0].setY(slots[8][1]);
                        }
                    }
                    tlock=false;
                }
            });

        }
    }


    private double calculateCVex(double raw){
        double res = ((raw*raw)*coefs[2]) + (raw * coefs[1]) + coefs[0];
        if (userecalflag) {
            res = (res * recal[1]) + recal[0];
        }
        return res;
    }

    // ==================== Context menus =================================================================================
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        MenuItem mi;
        int gaugeindex;

        super.onCreateContextMenu(menu, v, menuInfo);

        gaugeindex = rtg.getGaugeIndex(v);

        if (v.getId() == RunTaskRightGauges.GAUGEEMPTY) {
            menu.setHeaderTitle("Medidores Disponíveis");
            // todo : install icon
            GaugeType gt = rtg.getGaugeDescriptor();
            int gaugenum = gt.getlenght();
            for (int i = 0; i < gaugenum; i++) {
                if (gt.getGroup(i) == 1) {
                    String s = gt.getMenuEntry(i).substring(2);
                    mi = menu.add(CTXMENUGAUGES + gaugeindex, i, NONE, s);
                    if (rtg.isInstalled(i)) mi.setEnabled(false);
                }
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int gindex = (item.getGroupId() & 0x000000ff);

        if ((item.getGroupId() & 0xffffff00) == CTXMENUGAUGES) {
            Log.i(TAG, "Gauge Empty menu : [ " + item.getTitle() + " ] was selected");
            rtg.addGauge(gindex, item.getItemId());
            return true;
        }
        return super.onContextItemSelected(item);
    }

    public void registerGaugeContextMenu(View v) {
        registerForContextMenu(v);
    }


    public void setGaugevalue(int index, GaugeType gtype) {

        String temp = gtype.getMenuEntry(index);
        int ordinal = gtype.getOrdinal(index);

        if (ordinal == 3){
            Toast.makeText(this, "O Jig de acionamento não está instalado", Toast.LENGTH_LONG).show();
        }
        else if (ordinal == 4){
            Toast.makeText(this, "Use bargraph de concavidade (<- o da  esquerda) para o ajuste", Toast.LENGTH_LONG).show();
        }
        else if (ordinal == 2 || ordinal >5 ){ //ordinal == 7 || ordinal == 8 ){
            Toast.makeText(this, "Esse medidor é passivo e os alarmes estão desabilitados nessa versão", Toast.LENGTH_LONG).show();
        }
        else{
            SetValDescriptor svd = new SetValDescriptor();
            svd.setIcon(R.drawable.clock1)
                    .setType(ordinal)
                    .setTitle("Ajuste " + temp)
                    .setMax(gtype.getMax(index))
                    .setMin(gtype.getMin(index))
                    .setValue((int)slots[gtype.getSlot(index)] [0])
                    .setUnit("Rev / min");

            if (ordinal == 1) svd.setValue(platespeed);

            if (ordinal == 5){
                svd.setDirection(wg20dir);
                svd.setValue(wg20vel);
            }

            SetValDialogFragment setFragment = SetValDialogFragment.newInstance(svd);
            setFragment.addListener(this);
            setFragment.show(getFragmentManager(), "dialog");
        }
    }



    // EVENTS CALLBACK HOOK =======================================================================
    @Override
    public void onSetValFragmentInteraction(SetValDescriptor descriptor) {
        AcpMessage m;
        AcpMessage n;

        Log.d(TAG, "Setval type " + descriptor.getType() + " returned with " + descriptor.getValue());

        int dtype = descriptor.getType();

        if (dtype == 5){ // WG20
            wg20dir=descriptor.getDirection();
            wg20vel= descriptor.getValue();
            if (taskrunning && (wg20vel !=0)){
                AcpMessage m2 = new AcpMessage(true, 4, 4,"");
                m2.setCmd(EtherService.comand_map.get("WG20_SETVEL"));
                m2.setArg(wg20dir);
                m2.setMessage(String.valueOf(wg20vel));
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m2));
            }
        }
        else if (dtype == 1) { // Vel
            platespeed = descriptor.getValue();
            if (taskrunning){
                AcpMessage m1 = new AcpMessage(true, 3, 3,"");
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
        if (userelative){
            Log.d(TAG, "SetTime returned relative  to " + stime + " miliseconds");
        }
        else{
            Log.d(TAG, "SetTime returned time set to " + String.format("%1$tD - %1$tT", stime));
        }
    }

    public void onSetSweepFragmentInteraction() {

        //Log.d(TAG, "Setsweep returned : " + sweepdescriptor.getInner() + " - " + sweepdescriptor.getOuter() + " - " + sweepdescriptor.getVel());
        AcpMessage m;

        scantype = sweepdescriptor.getAlgo();
        scaninner = sweepdescriptor.getInner();
        scanouter =  sweepdescriptor.getOuter();
        scanvel = sweepdescriptor.getVel();
        scanposition = sweepdescriptor.getPosition();

        if (taskrunning){
            if (scantype == 0){
                AcpMessage m0 = new AcpMessage(true, 2, 2,"");
                m0.setCmd(EtherService.comand_map.get("SWEEP_ENABLE"));
                m0.setArg(1);
                m0.setMessage("");
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m0));

                AcpMessage m1 = new AcpMessage(true, 2, 2,"");
                m1.setCmd(EtherService.comand_map.get("SWEEP_SCAN"));
                m1.setArg(1);
                m1.setMessage(String.valueOf(scaninner) + " " +
                                String.valueOf(scanouter) + " " +
                                String.valueOf(scanvel));
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m1));
            }
            else if (scantype == 5){
                AcpMessage m0 = new AcpMessage(true, 2, 2,"");
                m0.setCmd(EtherService.comand_map.get("SWEEP_ENABLE"));
                m0.setArg(0);
                m0.setMessage("");
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m0));

                AcpMessage m1 = new AcpMessage(true, 2, 2,"");
                m1.setCmd(EtherService.comand_map.get("SWEEP_SCAN"));
                m1.setArg(1);
                m1.setMessage("");
                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m1));
            }



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
                if (taskrunning){
                    AcpMessage m3 = new AcpMessage(true, 4, 4,"");
                    m3.setCmd(EtherService.comand_map.get("MON_SETDRUM"));
                    if (usedrum) {
                        m3.setArg(1);
                    }
                    else{
                        m3.setArg(0);
                    }
                    m3.setMessage("");
                    run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m3));
                }
                Toast.makeText(this, "Ajustando Tambor de abrasivo", Toast.LENGTH_LONG).show();
                break;

            case 2: // Vacuo
                m = new AcpMessage(true, 2, 4,"");
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
        public void onAnimationStart(Animator animation) { }

        @Override
        public void onAnimationEnd(Animator animation) {
            //Log.e(TAG, "Animation Ended");
            ptmoving = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) { }

        @Override
        public void onAnimationRepeat(Animator animation) {}

        public void setSupportActionBar(Toolbar supportActionBar) {
            //this.supportActionBar = supportActionBar;
        }


        public static class OperationsDialogFragment extends DialogFragment {

            private static RunActivity racvty;

            private static String[] op = {
                    "",
                    "",
                    "Ativar / Desativar Vãcuo",
                    "Habilitar Monitoramento",
                    "Reiniciar Configuração",
                    "Salvar Configuração",
        };

        public static OperationsDialogFragment newInstance( RunActivity run) {
            OperationsDialogFragment fragment = new OperationsDialogFragment();
            racvty = run;
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {


            if (racvty.taskrunning){
                op[0] = "Parar Atividade";
            }
            else{
                op[0] = "Iniciar Atividade";
            }

            if (racvty.usedrum){
                op[1] = "Desligar Tambor de Abrasivo";
            }
            else{
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
        private static  RunActivity racvty;

        public static OPErrorDialogFragment newInstance(String mes,  RunActivity run ) {
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

        public void onDismiss(DialogInterface dialog){
            racvty.recoverRunErrorMessage();
        }
    }


    public void showRunErrorMessage(String message) {

        OPErrorDialogFragment dialog = OPErrorDialogFragment.newInstance(message, this);
        dialog.show(getFragmentManager(), "OperationsErrorFragment");
    }

    public void recoverRunErrorMessage() { }



    public void startLGTask(){

        AcpMessage m0 = new AcpMessage(true, 3, 3,"");
        m0.setCmd(EtherService.comand_map.get("SERVO_ENABLE"));
        m0.setArg(1);
        m0.setMessage("0");
        run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m0));

        AcpMessage m1 = new AcpMessage(true, 3, 3,"");
        m1.setCmd(EtherService.comand_map.get("SERVO_SETSPEED"));
        m1.setArg(1);
        m1.setMessage(String.valueOf(platespeed));
        run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m1));
//
//        if (usedrum){
//            AcpMessage m3 = new AcpMessage(true, 4, 4,"");
//            m3.setCmd(EtherService.comand_map.get("MON_SETDRUM"));
//            m3.setArg(1);
//            m3.setMessage("");
//            run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m3));
//        }


        //runtoolbar.setBackgroundColor(0xFF3F51B5);

//        if (scantype == 0){
//            AcpMessage m10 = new AcpMessage(true, 2, 2,"");
//            m10.setCmd(EtherService.comand_map.get("SWEEP_ENABLE"));
//            m10.setArg(1);
//            m10.setMessage("");
//            run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m10));
//
//            AcpMessage m11 = new AcpMessage(true, 2, 2,"");
//            m11.setCmd(EtherService.comand_map.get("SWEEP_SCAN"));
//            m11.setArg(1);
//            m11.setMessage(String.valueOf(scaninner) + " " +
//                    String.valueOf(scanouter) + " " +
//                    String.valueOf(scanvel));
//            run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m11));
//        }
//        else if (scantype == 5){
//            AcpMessage m10 = new AcpMessage(true, 2, 2,"");
//            m10.setCmd(EtherService.comand_map.get("SWEEP_ENABLE"));
//            m10.setArg(0);
//            m10.setMessage("");
//            run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m10));
//
//            AcpMessage m11 = new AcpMessage(true, 2, 2,"");
//            m11.setCmd(EtherService.comand_map.get("SWEEP_SCAN"));
//            m11.setArg(1);
//            m11.setMessage("");
//            run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m11));
//        }

//        if (wg20vel !=0){
//            AcpMessage m2 = new AcpMessage(true, 4, 4,"");
//            m2.setCmd(EtherService.comand_map.get("WG20_SETVEL"));
//            m2.setArg(wg20dir);
//            m2.setMessage(String.valueOf(wg20vel));
//            run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m2));
//        }



    }

    public void stopLGTask(){

            AcpMessage m0 = new AcpMessage(true, 3, 3,"");
            m0.setCmd(EtherService.comand_map.get("SERVO_ENABLE"));
            m0.setArg(0);
            m0.setMessage("0");
            run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m0));

            AcpMessage m1 = new AcpMessage(true, 3, 3,"");
            m1.setCmd(EtherService.comand_map.get("SERVO_SETSPEED"));
            m1.setArg(0);
            m1.setMessage("0");
            run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m1));
//
//            if (wg20vel !=0){
//                AcpMessage m2 = new AcpMessage(true, 4, 4,"");
//                m2.setCmd(EtherService.comand_map.get("WG20_SETVEL"));
//                m2.setArg(wg20dir);
//                m2.setMessage("0");
//                run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m2));
//            }

            AcpMessage m3 = new AcpMessage(true, 4, 4,"");
            m3.setCmd(EtherService.comand_map.get("MON_SETDRUM"));
            m3.setArg(0);
            m3.setMessage("");
            run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m3));


            AcpMessage m4 = new AcpMessage(true, 2, 2,"");
            m4.setCmd(EtherService.comand_map.get("SWEEP_ENABLE"));
            m4.setArg(0);
            m4.setMessage("");
            run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m4));

            AcpMessage m5 = new AcpMessage(true, 2, 2,"");
            m5.setCmd(EtherService.comand_map.get("SWEEP_SCAN"));
            m5.setArg(0);
            m5.setMessage("");
            run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m4));

            //runtoolbar.setBackgroundColor(0xFFFF4081);

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
                            }
                            else{
                                sleep(300);
                            }
                            break;

                        case ONERROR :
                            smm = tqueue.poll();
                            if (smm != null) {
                                cmd = smm.getCommand();
                                if (cmd == EtherService.CMDS.RECOVERERROR) {
                                    Log.d(TAG, "Recovering from Error");
                                    if (states_stack.isEmpty()) {
                                        Log.e(TAG, "Error recovering from error ! - going IDLE");
                                        state = EtherService.STATES.IDLE;
                                    }
                                }
                                else if (cmd == EtherService.CMDS.LOADSTATE) {
                                    states_stack.push(EtherService.STATES.ONERROR);
                                    states_stack.push(smm.getState());
                                }
                            }
                            else{
                                states_stack.push(EtherService.STATES.ONERROR);
                            }
                            break;

                        case MANAGETASK:
                            if (taskrunning){
                                taskrunning=false;
                                redrawer.pause();
                                runtimelabel = (String.format("Encerrada pelo usuário as %1$tH:%1$tM:%1$tS", System.currentTimeMillis()));
                                runService(true);
                                stopLGTask();

                                states_stack.clear();
                                states_stack.push(EtherService.STATES.IDLE);
                            }
                            else{
                                sb = new StringBuilder();
                                if (userelative){
                                    times[1] = System.currentTimeMillis()+times[1];
                                }
                                if (platespeed==0) { // speed <
                                    sb.append("A Velocidade não foi especificada\n\r");
                                }
                                if (times[1] < System.currentTimeMillis() + 1000) { // time
                                    sb.append("O periodo da atividade não foi especificado\n\r");
                                }

                                if (sb.length()==0){
                                    Log.d(TAG, "Iniciando Tarefa");
                                    startLGTask();
                                    tlock=false;
                                    taskrunning = true;

                                    redrawer.start();
                                    states_stack.push(EtherService.STATES.RUNNINGTASK);
                                }
                                else{
                                    showRunErrorMessage(sb.toString());
                                    states_stack.push(EtherService.STATES.IDLE);
                                    states_stack.push(EtherService.STATES.ONERROR);
                                }
                            }
                            break;

                        case RUNNINGTASK :

                            smm = tqueue.poll();
                            if (smm != null) {
                                cmd = smm.getCommand();
                                if (cmd == EtherService.CMDS.LOADSTATE) {
                                    states_stack.push(EtherService.STATES.RUNNINGTASK);
                                    states_stack.push(smm.getState());
                                }
                            }
                            else{
                                boolean run = runService(false);
                                if (run) {
                                    states_stack.push(EtherService.STATES.RUNNINGTASK);
                                    sleep(1000);
                                }
                                else{
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

         RunActivity acvty;
        int index;


        public RunSwipeTouchListener(Context ctx,  RunActivity acvty, int index) {
            super(ctx);
            this.acvty = acvty;
            this.index = index;
        }

        @Override
        public void onSwipeTop() {
            if (taskrunning){
                times[1] += 300000L;
                Toast.makeText(acvty, "Periodo da atividade foi incrementado em 5 minutos", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onSwipeBottom() {
            if (taskrunning){
                times[1] -= 300000L;
                Toast.makeText(acvty, "Periodo da atividade foi reduzido em 5 minutos", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onSwipeRight() {
            if (!taskrunning){
                times[1] = 300000L;
                userelative = true;
                Toast.makeText(acvty, "Periodo da atividade foi ajustado para 5 minutos", Toast.LENGTH_SHORT).show();
                runService(false);
            }

        }

        @Override
        public void onSwipeLeft() {
            if (taskrunning){
                Toast.makeText(acvty, "Atividade em funcionamento - O Ajuste foi desativado", Toast.LENGTH_LONG).show();
            }
            else{
                SetTimeDialogFragment setFragment = SetTimeDialogFragment.newInstance(userelative, defperiod );
                setFragment.addListener(acvty);
                setFragment.show(getFragmentManager(), "timedialog");
            }
        }
    };



    private void loadDefaults(){

            taskrunning = false;

            // Velocidade
            slots [1] [0] = 0;
            slots [1] [1] = 0;

            // Torque
            slots [2] [0] = 0;
            slots [2] [1] = 0;

            // Jig Spin
            slots [3] [0] = 0;
            slots [3] [1] = 0;

            // Sweep Pos
            slots [4] [0] = 50;
            slots [4] [1] = 45.6;

            // WG2 Spin
            slots [5] [0] = 20;
            slots [5] [1] = 0;

            // Microns
            slots [6] [0] = 1.5;
            slots [6] [1] = 1.5743;

            // Drop
            slots [7] [0] = 2.7;
            slots [7] [1] = 2.059;

            // CVex
            slots [8] [0] = 1.5;
            slots [8] [1] = 0;


            times[0] = System.currentTimeMillis();
            times[1] = times[0] + 60000;
            userelative = true;

            runService(false);
        }


        // Plotter Services ==============================================================================

        private void SetPlotter(int size) {

            MyFadeFormatter formatter1 = new MyFadeFormatter(size);
            //MyFadeFormatter formatter2 = new MyFadeFormatter(size);

            formatter1.setLegendIconEnabled(false);

            plot.setLinesPerRangeLabel(3);
            plot.setTitle("Concavidade da Placa de Desbaste");

            series[0] = new GaugePlotModel(1200);
            //series[1] = new GaugePlotModel(1200);


            plot.addSeries(series[0], formatter1);
            //plot.addSeries(series[1], formatter2);


            plot.setRangeBoundaries(-10, 10, BoundaryMode.FIXED);
            plot.setDomainBoundaries(0, size, BoundaryMode.FIXED);

            series[0].init(new WeakReference<>(plot.getRenderer(AdvancedLineAndPointRenderer.class)), 4);
            //series[1].init(new WeakReference<>(plot.getRenderer(AdvancedLineAndPointRenderer.class)), 4);

            redrawer = new Redrawer(plot, 2, false);

        }

        public static class GaugePlotModel implements XYSeries {

            private Number[] data;
            private int latestIndex;
            private int slot;

            private WeakReference<AdvancedLineAndPointRenderer> rendererRef;

            public GaugePlotModel(int size) {
                data = new Number[size];
                for(int i = 0; i < data.length; i++) {
                    //data[i] = (Math.random() * 3) + 20;
                    data[i] = 0;
                }
            }

            public void init(final WeakReference<AdvancedLineAndPointRenderer> rendererRef, int slot) {
                this.rendererRef = rendererRef;
                this.slot = slot;
                latestIndex = 0;
            }

            public void setY(Number val) {
                if (latestIndex >= data.length) {
                    latestIndex = 0;
                }
                data[latestIndex] = val;
                if(rendererRef.get() != null) {
                    rendererRef.get().setLatestIndex(latestIndex);
                }
                latestIndex++;
            }

            public int getSlot(){ return slot;}

            @Override
            public int size() {
                return data.length;
            }

            @Override
            public Number getX(int index) {
                return index;
            }

            @Override
            public Number getY(int index) {
                return data[index];
            }

            @Override
            public String getTitle() {
                return "Signal";
            }
        }

        /**
         * Special {@link AdvancedLineAndPointRenderer.Formatter} that draws a line
         * that fades over time.  Designed to be used in conjunction with a circular buffer model.
         */
        public static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

            private int trailSize;

            public MyFadeFormatter(int trailSize) {
                this.trailSize = trailSize;
            }

            @Override
            public Paint getLinePaint(int thisIndex, int latestIndex, int seriesSize) {
                // offset from the latest index:
                int offset;
                if(thisIndex > latestIndex) {
                    offset = latestIndex + (seriesSize - thisIndex);
                } else {
                    offset =  latestIndex - thisIndex;
                }

                float scale = 255f / trailSize;
                int alpha = (int) (255 - (offset * scale));
                getLinePaint().setAlpha(alpha > 0 ? alpha : 0);
                return getLinePaint();
            }
        }














}



//        GregorianCalendar gcstart = new GregorianCalendar();
//        GregorianCalendar gcstop = new GregorianCalendar();
//        GregorianCalendar gctemp = new GregorianCalendar();
//
//        gcstop.add(GregorianCalendar.MINUTE, 124);
//
//        long mstart = gcstart.getTimeInMillis();
//        long mstop = gcstop.getTimeInMillis();
//        long mdif = (mstop - mstart) + 10800000 ;
//
//        String sgcstart = String.format("Start: %1$tH:%1$tM:%1$tS", gcstart);
//        String sgcstop = String.format ("Stop : %1$tH:%1$tM:%1$tS", gcstop);
//        String sdif = String.format("Diff: %1$tH:%1$tM:%1$tS", mdif);
//
//        Log.d(TAG, "Ajusting Time");




//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }



//
///**
// * Primitive simulation of some kind of signal.  For this example,
// * we'll pretend its an ecg.  This class represents the data as a circular buffer;
// * data is added sequentially from left to right.  When the end of the buffer is reached,
// * i is reset back to 0 and simulated sampling continues.
// */
//public static class ECGModel implements XYSeries {
//
//    private final Number[] data;
//    private final long delayMs;
//    private final int blipInteral;
//    private final Thread thread;
//    private boolean keepRunning;
//    private int latestIndex;
//
//    private WeakReference<AdvancedLineAndPointRenderer> rendererRef;
//
//    /**
//     *
//     * @param size Sample size contained within this model
//     * @param updateFreqHz Frequency at which new samples are added to the model
//     */
//    public ECGModel(int size, int updateFreqHz) {
//
//        data = new Number[size];
//        for(int i = 0; i < data.length; i++) {
//            data[i] = 0;
//        }
//
//        // translate hz into delay (ms):
//        delayMs = 1000 / updateFreqHz;
//
//        // add 7 "blips" into the signal:
//        blipInteral = size / 7;
//
//        thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    while (keepRunning) {
//                        if (latestIndex >= data.length) {
//                            latestIndex = 0;
//                        }
//
//                        // generate some random data:
//                        if (latestIndex % blipInteral == 0) {
//                            // insert a "blip" to simulate a heartbeat:
//                            data[latestIndex] = (Math.random() * 10) + 3;
//                        } else {
//                            // insert a random sample:
//                            data[latestIndex] = Math.random() * 2;
//                        }
//
//                        if(latestIndex < data.length - 1) {
//                            // null out the point immediately following i, to disable
//                            // connecting i and i+1 with a line:
//                            data[latestIndex +1] = null;
//                        }
//
//                        if(rendererRef.get() != null) {
//                            rendererRef.get().setLatestIndex(latestIndex);
//                            Thread.sleep(delayMs);
//                        } else {
//                            keepRunning = false;
//                        }
//                        latestIndex++;
//                    }
//                } catch (InterruptedException e) {
//                    keepRunning = false;
//                }
//            }
//        });
//    }
//
//    public void start(final WeakReference<AdvancedLineAndPointRenderer> rendererRef) {
//        this.rendererRef = rendererRef;
//        keepRunning = true;
//        thread.start();
//    }
//
//    @Override
//    public int size() {
//        return data.length;
//    }
//
//    @Override
//    public Number getX(int index) {
//        return index;
//    }
//
//    @Override
//    public Number getY(int index) {
//        return data[index];
//    }
//
//    @Override
//    public String getTitle() {
//        return "Signal";
//    }
//}
//
//
//
//
//




//if (vel == 0){
//        m = new AcpMessage(true, 2, 4,"");
//        m.setCmd(30);
//        m.setArg(0);
//        run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m));
//
//        n = new AcpMessage(true, 3, 3,"");
//        n.setCmd(33);
//        n.setArg(0);
//        n.setMessage("0");
//        run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, n));
//
//        slots [1] [0] = 0;
//
//        }
//        else{
//        m = new AcpMessage(true, 2, 4,"");
//        m.setCmd(30);
//        m.setArg(1);
//        run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, m));
//
//        n = new AcpMessage(true, 3, 3,"");
//        n.setCmd(33);
//        n.setArg(0);
//        n.setMessage(String.valueOf(descriptor.getValue()));
//        run_smqueue.add(new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.SENDU, n));
//
//        slots [1] [0] = descriptor.getValue();
//        }



//    public void parseStatusCallback(String message){
//
//        //Log.d(TAG, "Callback received = " + message);
//        double v;
//
//        slots [1] [1] = convertStatusCalbackValue(message.substring(0,7));   // vel
//        slots [2] [1] = convertStatusCalbackValue(message.substring(7,14));  // torque
//        v  = convertStatusCalbackValue(message.substring(14,21));  // theta
//        slots [4] [1] = convertStatusCalbackValue(message.substring(21,28));  // delta
//        slots [8] [1] = convertStatusCalbackValue(message.substring(28,35));  // cvex
//        slots [6] [1] = convertStatusCalbackValue(message.substring(35,42));  // microns
//        slots [7] [1] = convertStatusCalbackValue(message.substring(42,49));  // drop
//
//    }
//
//    private double convertStatusCalbackValue(String sval){
//
//        double val;
//
//        try {
//            val = Double.valueOf(sval);
//        }
//        catch (NumberFormatException ex) {
//            val=0.0;
//            Log.d(TAG, "Failed to convert calback value");
//        }
//        return val;
//    }

