package com.opus.virna7;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static boolean isXLargeTablet(Context context) {
        return true;
//        return (context.getResources().getConfiguration().screenLayout
//                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || NetworkPreferenceFragment.class.getName().equals(fragmentName)
                || SweepPreferenceFragment.class.getName().equals(fragmentName)
                || ServoPreferenceFragment.class.getName().equals(fragmentName)
                || MonitorPreferenceFragment.class.getName().equals(fragmentName);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_main_k_platespeed)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_main_k_scaninner)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_main_k_scanouter)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_main_k_scanposition)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_main_k_scantype)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_main_k_scanvel)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_main_k_wg20dir)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_main_k_wg20vel)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_main_k_defperiod)));

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NetworkPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_network);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_net_k_ip)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_net_k_port)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_net_k_scanpooling)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_net_k_statuscallbackperiod)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_net_k_usehttp)));

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SweepPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_sweep);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sweep_k_scaninner)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sweep_k_scanouter)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sweep_k_scanspeed)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sweep_k_dampstart)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sweep_k_dampstop)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sweep_k_mapwindow)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sweep_k_mapoffset)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sweep_k_scanfixparkwindow)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sweep_k_scanfixerrorwindow)));

        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ServoPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_servo);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_servo_k_transfera0)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_servo_k_transfera1)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_servo_k_transfera2)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_servo_k_pidp)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_servo_k_pidi)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_servo_k_pidd)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MonitorPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_monitor);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mon_k_lvdta0)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mon_k_lvdta1)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mon_k_lvdta2)));

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mon_k_lvdtmin)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mon_k_lvdtmax)));

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mon_k_lvdtoffset)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mon_k_lvdtslope)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }




}
