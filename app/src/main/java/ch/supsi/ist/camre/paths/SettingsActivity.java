package ch.supsi.ist.camre.paths;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;

public class SettingsActivity extends Activity {

    public static String HARVESTING = "HARVESTING";
    public static boolean HARVESTING_DEFAULT = false;

    public static String GPS_ONLY = "GPS_ONLY";
    public static boolean GPS_ONLY_DEFAULT = true;
    public static String GPS_ENABLED = "GPS_ENABLED";
    public static boolean GPS_ENABLED_DEFAULT = false;
    public static String PATH_ACCURACY = "PATH_ACCURACY";
    public static int PATH_ACCURACY_DEFAULT = 20;
    public static String PATH_POINTS_DISTANCE = "PATH_POINTS_DISTANCE";
    public static int PATH_POINTS_DISTANCE_DEFAULT = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("SettingsActivity: " + item.getItemId() + " = " + android.R.id.home);
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public SettingsFragment() {
            // Required empty public constructor
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            System.out.println("onSharedPreferenceChanged!! key: " + key);
            Preference connectionPref;
            if (key.equals(GPS_ONLY)) {
                connectionPref = findPreference(GPS_ONLY);
                if(sharedPreferences.getBoolean(GPS_ONLY,GPS_ONLY_DEFAULT)){
                    findPreference(GPS_ONLY).setSummary("Location is determined using only GPS");
                }else{
                    findPreference(GPS_ONLY).setSummary(
                            "Location is determined using GPS, Wi-Fi and mobile networks");
                }
            }else if (key.equals(PATH_ACCURACY)) {
                connectionPref = findPreference(PATH_ACCURACY);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary("Minimal accepted accuracy of " +
                        sharedPreferences.getInt(PATH_ACCURACY, PATH_ACCURACY_DEFAULT) + " meters");
            }else if (key.equals(PATH_POINTS_DISTANCE)){
                connectionPref = findPreference(PATH_POINTS_DISTANCE);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary("Minimal accepted distance between to point is of " +
                        sharedPreferences.getInt(PATH_POINTS_DISTANCE, PATH_POINTS_DISTANCE_DEFAULT) + " meters");
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            SharedPreferences preferences =  getPreferenceScreen().getSharedPreferences();

            if(preferences.getBoolean(GPS_ONLY,GPS_ONLY_DEFAULT)){
                findPreference(GPS_ONLY).setSummary("Location is determined using only GPS");
            }else{
                findPreference(GPS_ONLY).setSummary("Location is determined using GPS, Wi-Fi and mobile networks");
            }

            findPreference("PATH_ACCURACY").setSummary("Minimal accepted accuracy of " +
                    preferences.getInt(PATH_ACCURACY, PATH_ACCURACY_DEFAULT) + " meters");

            findPreference("PATH_POINTS_DISTANCE").setSummary(
                    "Minimal accepted distance between two point is of " +
                            preferences.getInt(PATH_POINTS_DISTANCE, PATH_POINTS_DISTANCE_DEFAULT) + " meters");
            preferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

    }

}
