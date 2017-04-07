package com.opus.virna7;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.Monitor;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Support3 extends AppCompatActivity {


    public static final String TAG = "SUPPORT";

    public EtherService etherserviceBinder;
    public Intent i;
    public IntentFilter intentFilter;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private SharedPreferences preferences ;
    private SweepFragment sweepfrag;
    private ServoFragment servofrag;
    private MonitorFragment monitorfrag;

    private int statuspacket = 0;

    @BindView(R.id.sw_connect) Switch sw_connect;
    @BindView(R.id.sw_callback) Switch sw_callback;
    @BindView(R.id.support_container) ViewPager pager;

    @BindView(R.id.tv_support_cbnum) TextView tv_cbnum;
    @BindView(R.id.tv_support_traffic) TextView tv_traffic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support3);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.support_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        //mViewPager.addOnPageChangeListener(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    public void onResume() {

        super.onResume();
        Log.d(TAG, "Resuming Support");
        // Start connection with USBServices
        intentFilter = new IntentFilter();
        intentFilter.addAction("ACPMESSAGE");
        intentFilter.addAction("COMMANDCALLBACK");
        intentFilter.addAction("COMMANDACK");

        registerReceiver(intentReceiver, intentFilter);

        if (etherserviceBinder == null) {
            startEtherService();
        }
    }

    public void onPause() {

        super.onPause();

        Intent intent = getIntent();
        Log.d(TAG, "Pausing Run Task DUE : " + intent);

        unregisterReceiver(intentReceiver);
        if (etherserviceBinder != null) {
            //unbindService(connection);
        }
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        int temp = mViewPager.getCurrentItem();

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            if (temp < 2){
                mViewPager.setCurrentItem(temp + 1);
            }
            //Toast.makeText(this, "Volume Up", Toast.LENGTH_LONG).show();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            if (temp > 0){
                mViewPager.setCurrentItem(temp - 1);
            }
            //Toast.makeText(this, "Volume Down", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }



    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    // Service Binding ===============================================================================================
    private ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "Support is binding to EtherService");
            etherserviceBinder = ((EtherService.EtherBinder) service).getService();
            startService(i);
            //sweepfrag.bindService(etherserviceBinder);
            etherserviceBinder.readVar(EtherService.comand_map.get("NET_ATTACHED"));
            etherserviceBinder.readVar(EtherService.comand_map.get("NET_CALLBACKENABLED"));

        }
        public void onServiceDisconnected(ComponentName className) {
            etherserviceBinder = null;
        }
    };


    private void startEtherService() {
        Log.d(TAG, "Support is starting Ether Service -->");
        i = new Intent(getBaseContext(), EtherService.class);
        bindService(i, connection, Context.BIND_AUTO_CREATE);
    }

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Log.e(TAG, "Run task is Receiving messa from Service");
            String action = intent.getAction();
            if ("ACPMESSAGE".equals(action)) {
                byte[] mesbytes = intent.getByteArrayExtra("ACPM_BYTES");
                if (mesbytes != null) {
                    AcpMessage m = new AcpMessage(false, mesbytes);
                    if (m.getCmd() == EtherService.comand_map.get("CMD_CSTATUS")){
                        //tv_traffic.setText(m.getStringPayload());
                    }else{
                        tv_traffic.setText(m.toString());
                    }
                } else {
                    Log.e(TAG, "Failed to parse ACPM_BYTES");
                }


            } else if ("COMMANDCALLBACK".equals(action)) {
                String extra = intent.getStringExtra("String");
                Log.d(TAG, "CONNAMD CALLBACK RETURNED : " + extra);

            } else if ("COMMANDACK".equals(action)) {
                int cmd_code = intent.getIntExtra("CODE", 0);
                if (cmd_code == EtherService.comand_map.get("NET_ATTACHED")){
                    //Log.d(TAG, "NET Attached informs : " + intent.getStringExtra("VALUE"));
                    sw_connect.setChecked(Boolean.valueOf(intent.getStringExtra("VALUE")));
                }
                else if (cmd_code == EtherService.comand_map.get("NET_CALLBACKENABLED")){
                    //Log.d(TAG, "NET callback enabled informs : " + intent.getStringExtra("VALUE"));
                    sw_callback.setChecked(Boolean.valueOf(intent.getStringExtra("VALUE")));
                }
                else if (cmd_code == EtherService.comand_map.get("NET_STATUSARRIVED")){
                    updateUI();
                }

            } else {
                Log.e(TAG, "Undetermined Service message received : " + action);
            }
        }
    };

    private void updateUI(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int pager_item = pager.getCurrentItem();
                tv_cbnum.setText(String.valueOf(statuspacket++));
                if (pager_item == 0){
                    sweepfrag.updateUI();
                }
                else if (pager_item == 1){
                    servofrag.updateUI();
                }
                else if (pager_item == 2){
                    monitorfrag.updateUI();
                }

            }
        });

    }

    // Tab and Views Management ===============================================================================================


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0){
                sweepfrag = SweepFragment.newInstance(position + 1);
                sweepfrag.bindService(etherserviceBinder);
                return sweepfrag;
            }
            else if (position == 1){
                servofrag = ServoFragment.newInstance(position + 1);
                servofrag.bindService(etherserviceBinder);
                return servofrag;
            }
            else {
                monitorfrag = MonitorFragment.newInstance(position + 1);
                monitorfrag.bindService(etherserviceBinder);
                return monitorfrag;
            }

        }

        @Override
        public int getCount() { return 3;}

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "OSCILANTE";
                case 1:
                    return "SERVO";
                case 2:
                    return "MONITOR";
            }
            return null;
        }
    }


    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +(scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    // Tools section ============================================================================================

    @OnClick(R.id.sw_connect)
    public void connectClick(View v){
        boolean con = sw_connect.isChecked();
        preferences.edit().putBoolean(getString(R.string.pref_net_k_autologin),con).apply();
    }

    @OnClick(R.id.sw_callback)
    public void callbackClick(View v){
        boolean con = sw_callback.isChecked();
        preferences.edit().putBoolean(getString(R.string.pref_net_k_dostatuscallback),con).apply();
    }

}


//public class Support3 extends AppCompatActivity implements ViewPager.OnPageChangeListener{
//    @Override
//    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        Log.d(TAG, "Page Scrolled");
//    }
//
//    @Override
//    public void onPageSelected(int position) {
//        Log.d(TAG, "Page Selected");
//    }
//
//    @Override
//    public void onPageScrollStateChanged(int state) {
//        Log.d(TAG, "Page Scroll state changed");
//    }