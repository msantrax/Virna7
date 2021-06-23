package com.opus.virna7;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class Virna7 extends AppCompatActivity {

    private static final String TAG = "Virna6Park";

    private static final int UI_ANIMATION_DELAY = 300;

    private SharedPreferences preferences ;
    private Virna7Application virna;

    private View mContentView;
    private View mKeypadView;
    private View mOpenbarView;
    private View mPm5BannerView;

    private Button lbutton;
    private EditText codepwd;
    private String spwd;

    private DialogFragment dlgFragment;

    private boolean kpadvisible;

    private final Handler mHideHandler = new Handler();

    private Animation hd1;
    private Animation fade_down50;
    private Animation fade_up50;
    private Animation openy;
    private Animation slide1;

    // Service stuff
    public EtherService etherserviceBinder;
    public Intent i;
    public IntentFilter intentFilter;


// ON Something section ========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_virna7);
        ButterKnife.bind(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);


        kpadvisible = false;

        mContentView = findViewById(R.id.wallpapper);
        mKeypadView = findViewById(R.id.Keypad);
        lbutton = (Button)findViewById(R.id.bt_logged);
        codepwd = (EditText)findViewById(R.id.ACodepwd);
        mOpenbarView = findViewById(R.id.openbar);
        mPm5BannerView = findViewById(R.id.pm5banner);

        hd1 = AnimationUtils.loadAnimation(this, R.anim.acph);
        slide1 = AnimationUtils.loadAnimation(this, R.anim.acp_slide1);
        fade_down50 = AnimationUtils.loadAnimation(this, R.anim.acp_fade_down50);
        fade_up50 = AnimationUtils.loadAnimation(this, R.anim.acp_fade_up50);

        mKeypadView.setScaleY(0f);
        mKeypadView.setAlpha(0f);

        mOpenbarView.setScaleX(0f);
        mOpenbarView.setAlpha(0f);

        mContentView.startAnimation(hd1);
    }

    public void onResume(){
        super.onResume();

        mContentView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        Log.d(TAG, "Resuming Virna");

        intentFilter = new IntentFilter();
        intentFilter.addAction("ACPMESSAGE");
        intentFilter.addAction("COMMANDCALLBACK");
        registerReceiver(intentReceiver, intentFilter);

        Intent intent = getIntent();
        Log.d(TAG, "intent: " + intent);
        String action = intent.getAction();

        virna  = (Virna7Application)getApplicationContext();

        if (etherserviceBinder == null){ startEtherService();}

        virna.setProfilemanager(ProfileManager.getInstance(this.getAssets(), virna));

    }

    public void onPause(){
        super.onPause();

        Intent intent = getIntent();
        Log.d(TAG, "intent: " + intent);
        Log.d(TAG, "Pausing Virna DUE : " + intent);

        unregisterReceiver(intentReceiver);
        if (etherserviceBinder != null) {
            //unbindService(connection);
        }

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);

        AnimatorSet animset = new AnimatorSet();
        ArrayList<ObjectAnimator> lanim = getLogoAnimation(true, 3000);
        //animset.playTogether((ArrayList<Animator>)lanim);
        animset.play(lanim.get(0)).with(lanim.get(1)).with(lanim.get(2)).with(lanim.get(3));
        animset.start();
        hide();
    }


    public class BootUpReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent target = new Intent(context, Virna7.class);
            target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(target);
        }
    }

    // USB Service tasks and control ===============================================================

    private ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "Service Connection is biding on Virna...");
            etherserviceBinder = ((EtherService.EtherBinder)service).getService();
            startService(i);
            virna.setEtherserviceBinder(etherserviceBinder);
        }
        public void onServiceDisconnected(ComponentName className) {

            etherserviceBinder = null;
        }
    };

    private void startEtherService(){
        Log.d(TAG, " Virna is starting Ether Service -->");
        i = new Intent(getBaseContext(), EtherService.class);
        bindService(i, connection, Context.BIND_AUTO_CREATE);
    }

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String temp = intent.getStringExtra("STRING");
            //Log.d(TAG, "EtherService called Virna with : " + temp);
        }
    };


    private ArrayList<ObjectAnimator> getLogoAnimation(boolean show, int duration){

        float alpha_from, alpha_to;
        float trans_from, trans_to;
        ArrayList<ObjectAnimator> animations = new ArrayList<>();

        View acplogo1View = findViewById(R.id.logoacp1);
        View acplogo2View = findViewById(R.id.logoacp2);

        if (show){
            alpha_from=0f;
            alpha_to=1f;
            trans_from=400f;
            trans_to=0f;
        }
        else{
            alpha_from=1f;
            alpha_to=0f;
            trans_from=0f;
            trans_to=800f;
        }

        ObjectAnimator anm_logo1 = ObjectAnimator.ofFloat(acplogo1View, "alpha", alpha_from, alpha_to);
        anm_logo1.setInterpolator(new AccelerateDecelerateInterpolator());
        anm_logo1.setDuration(duration);
        animations.add(anm_logo1);

        ObjectAnimator trans_logo1 = ObjectAnimator.ofFloat(acplogo1View, "translationX", trans_from, trans_to);
        trans_logo1.setInterpolator(new BounceInterpolator());
        trans_logo1.setDuration(duration);
        animations.add(trans_logo1);

        ObjectAnimator anm_logo2 = ObjectAnimator.ofFloat(acplogo2View, "alpha", alpha_from, alpha_to);
        anm_logo2.setInterpolator(new AccelerateDecelerateInterpolator());
        anm_logo2.setDuration(duration);
        animations.add(anm_logo2);

        ObjectAnimator anm_logo3 = ObjectAnimator.ofFloat(mPm5BannerView, "alpha", alpha_from, alpha_to);
        anm_logo3.setInterpolator(new AccelerateDecelerateInterpolator());
        anm_logo3.setDuration(duration);
        anm_logo3.setStartDelay(500);
        animations.add(anm_logo3);

        return animations;

    }

    @OnClick(R.id.bt_logged)
    public void btclicked() {
        Log.i(TAG, "Button clicked");
    }


    @OnClick(R.id.bt_back)
    public void btbackclicked() {
        addTodken(".", (Button)findViewById(R.id.bt_back));
    }

    @OnClick(R.id.bt1)
    public void bt1clicked() {
        addTodken("1", (Button)findViewById(R.id.bt1));
    }

    @OnClick(R.id.bt2)
    public void bt2clicked() {
        addTodken("2", (Button)findViewById(R.id.bt2));
    }

    @OnClick(R.id.bt3)
    public void bt3clicked() {
        addTodken("3", (Button)findViewById(R.id.bt3));
    }

    @OnClick(R.id.bt4)
    public void bt4clicked() {
        addTodken("4", (Button)findViewById(R.id.bt4));
    }

    @OnClick(R.id.bt5)
    public void bt5clicked() {
        addTodken("5", (Button)findViewById(R.id.bt5));
    }

    @OnClick(R.id.bt6)
    public void bt6clicked() {
        addTodken("6", (Button)findViewById(R.id.bt6));
    }

    @OnClick(R.id.bt7)
    public void bt7clicked() {
        addTodken("7", (Button)findViewById(R.id.bt7));
    }

    @OnClick(R.id.bt8)
    public void bt8clicked() {
        addTodken("8", (Button)findViewById(R.id.bt8));
    }

    @OnClick(R.id.bt9)
    public void bt9clicked() {
        addTodken("9", (Button)findViewById(R.id.bt9));
    }

    @OnClick(R.id.bt0)
    public void bt0clicked() {
        addTodken("0", (Button)findViewById(R.id.bt0));
    }

    @OnClick(R.id.btkpok)
    public void btkpokclicked() {
        addTodken("+", (Button)findViewById(R.id.btkpok));
    }


    private void addTodken(String token, Button bt){

        Button btbk = (Button)findViewById(R.id.bt_back);

        AnimatorSet animset = new AnimatorSet();
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(bt, "alpha", 1f, 0f);
        anim1.setDuration(100);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(bt, "alpha", 0f, 1f);
        anim2.setDuration(100);

        animset.play(anim1).before(anim2);
        animset.start();

        if (token.equalsIgnoreCase(".")){
            // Delete buttom
            if (spwd != null && (!spwd.isEmpty())) {
                String pwdtemp = spwd.substring(0, spwd.length() - 1);
                spwd = pwdtemp;
            }
        }
        else if (token.equalsIgnoreCase("+")){
            // OK Button
            //showKeypad(false);

            mHideHandler.removeCallbacks(hideKeypadRunnable);
            if(spwd.equalsIgnoreCase("4556")){
                dlgFragment = UserDialogFragment.newInstance("Marcelo Raso", true);
                dlgFragment.show(getFragmentManager(), "dialog");
            }
            else if(spwd.equalsIgnoreCase("1")){
                dlgFragment = UserDialogFragment.newInstance("Convidado", false);
                dlgFragment.show(getFragmentManager(), "dialog");
            }
            else if (spwd.equalsIgnoreCase("0")){
                startActivity(new Intent(Virna7.this, RunSimpleActivity.class));
            }
            else if (spwd.equalsIgnoreCase("3055")){
                //SMTraffic smt = new SMTraffic(EtherService.CMDS.LOADSTATE, 0, EtherService.STATES.ATTACH);
                etherserviceBinder.doComand(EtherService.STATES.ATTACH, 0);
            }
            else if (spwd.equalsIgnoreCase("3819")){
                etherserviceBinder.doComand(EtherService.STATES.DETACH, 0);
            }
            else if (spwd.equalsIgnoreCase("7")){
                etherserviceBinder.doComand(EtherService.STATES.PING, 0);
            }

            else if (spwd.equalsIgnoreCase("666")){
                Intent intent = new Intent(this, EtherService.class);
                stopService(intent);
                finish();
                System.exit(0);
            }
            else if (spwd.equalsIgnoreCase("555")){
//                Intent i = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
//                i.putExtra("android.intent.extra.KEY_CONFIRM", true);
//                startActivity(i);
                try {
                    Log.i(TAG, "Rebooting tablet .....");
                    Process proc = Runtime.getRuntime().exec(new String[]{ "/system/bin/reboot" });
                    proc.waitFor();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }



            else if (spwd.isEmpty()){
                hide();
            }
            else{
                showWrongPwd();
            }

            return;
            //this.finish();
        }
        else{
            spwd=spwd+token;
        }

        mHideHandler.removeCallbacks(hideKeypadRunnable);
        mHideHandler.postDelayed(hideKeypadRunnable, 5000);

        codepwd.setText(spwd);
        Log.i(TAG, spwd);
    }



    private void showKeypad(boolean show){

        AnimatorSet logoanimset = new AnimatorSet();
        AnimatorSet kpadanimset = new AnimatorSet();

        ArrayList<ObjectAnimator>lanim;

        ObjectAnimator openkpad = ObjectAnimator.ofFloat(mKeypadView, "ScaleY", 0f, 1f);
        openkpad.setDuration(1000);

        ObjectAnimator closekpad = ObjectAnimator.ofFloat(mKeypadView, "ScaleY", 1f, 0f);
        closekpad.setDuration(1000);

        ObjectAnimator openkbar = ObjectAnimator.ofFloat(mOpenbarView, "ScaleX", 0f, 1f);
        openkbar.setDuration(500);

        ObjectAnimator closekbar = ObjectAnimator.ofFloat(mOpenbarView, "ScaleX", 1f, 0f);
        closekbar.setDuration(500);

        ObjectAnimator fadekbar = ObjectAnimator.ofFloat(mOpenbarView, "alpha", 1f, 0f);
        fadekbar.setDuration(500);

        ObjectAnimator fadepm5 = ObjectAnimator.ofFloat(mOpenbarView, "alpha", 1f, 0f);
        fadekbar.setDuration(500);


        if (show){
            spwd="";
            codepwd.setText(spwd);
            mOpenbarView.setAlpha(1f);
            mKeypadView.setAlpha(1f);
            lanim = getLogoAnimation(false, 1500);

            logoanimset.play(lanim.get(0)).with(lanim.get(1)).with(lanim.get(2)).with(lanim.get(3));
            logoanimset.start();

            kpadanimset.setStartDelay(1500);
            kpadanimset.play(openkpad).with(fadekbar).with(closekbar);
            kpadanimset.play(openkbar).before(openkpad);
            kpadanimset.start();

            kpadvisible=true;
            mHideHandler.removeCallbacks(hideKeypadRunnable);
            mHideHandler.postDelayed(hideKeypadRunnable, 10000);
        }
        else{
            lanim = getLogoAnimation(true, 3000);
            mOpenbarView.setAlpha(1f);

            kpadanimset.play(closekpad).with(openkbar).before(closekbar);
            kpadanimset.start();

            logoanimset.setStartDelay(1500);
            logoanimset.play(lanim.get(0)).with(lanim.get(1)).with(lanim.get(2)).with(lanim.get(3));
            logoanimset.start();

            kpadvisible=false;
        }
    }


    private final Runnable hideKeypadRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            hide();
        }
    };

    @OnClick(R.id.wallpapper)
    public void toggle() {

        if (!kpadvisible) {
            show();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void btokClicked(View v) {
        //Log.i("L17", "btok clicked !!");
        dlgFragment.dismiss();
        showKeypad(false);
        doRun();

    }

    private void showWrongPwd(){

        Context context = Virna7.this;
        String title = "Gerente de Acessos";

        Resources res = getResources();
        String text = String.format(res.getString(R.string.dlgmsg_wrongpwd), spwd );
        CharSequence styledText = Html.fromHtml(text);

        String button1String = "Tente Novamente";
        String button2String = "Desistir";
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle(title);
        ad.setMessage(styledText);


        ad.setPositiveButton(
                button1String,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Log.i(TAG, "Tente de novo");;
                    }
                }
        );

        ad.setNegativeButton(
                button2String,
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int arg1) {
                        Log.i(TAG, "Desistir");
                        hide();
                    }
                }
        );

        AlertDialog ad1 = ad.create();
        ad1.setIcon(R.drawable.guestuser);
        ad1.show();

    }




    private void hide() {
        // Hide UI first
        //Log.i(TAG, "Hide Login keypad");

        showKeypad(false);
        //mContentView.startAnimation(fade_up50);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        //mHideHandler.removeCallbacks(mShowPart2Runnable);
        //mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        lbutton.setAlpha((float) 0);
    }


    public void doRun(){

        //etherserviceBinder.doComand(EtherService.STATES.ATTACH, 0);
        //startActivity(new Intent("com.opus.virna7.Support3"));
        //startActivity(new Intent("com.opus.virna7.SettingsActivity"));
        startActivity(new Intent(Virna7.this, NavActivity.class));

    }


    @SuppressLint("InlinedApi")
    private void show() {

        boolean autenticate = preferences.getBoolean(getString(R.string.pref_main_k_autenticate), false);

        //startActivity(new Intent(Virna7.this, RunSimpleActivity.class));


        if (autenticate) {
            showKeypad(true);
        }
        else{
            doRun();
        }

//        lbutton.setAlpha((float) 1);
//        lbutton.startAnimation(slide1);

        //mContentView.startAnimation(fade_down50);
        //mContentView.setAlpha((float) 0.5);

    }
}
