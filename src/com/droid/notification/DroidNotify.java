package com.droid.notification;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.format.DateFormat;

public class DroidNotify extends PreferenceActivity {

    private boolean active;
    private Preference service;
    public static boolean sensorOnChecked;

    public static boolean isSamsungPhoneWithTTS(Context context) {

        boolean retour = false;

        try {
            @SuppressWarnings("unused")
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo("com.samsung.SMT", 0);
            retour = true;
        } catch (PackageManager.NameNotFoundException e) {
            retour = false;
        }

        return retour;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            addPreferencesFromResource(R.xml.preferences);

            service = (Preference) findPreference("service");
            service.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    try
                    {
                        startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                    catch (Exception ex)
                    {}
                    return true;
                }
            });

            Preference serviceGoogle = (Preference) findPreference("serviceGoogle");
            serviceGoogle.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                try
                {
                    Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", "com.google.android.tts",
                            null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                catch (Exception ex)
                {}

                return true;
                }
            });

            Preference serviceSamsung = (Preference) findPreference("serviceSamsung");
            serviceSamsung.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                try
                {
                    Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", "com.samsung.SMT",
                            null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                catch (Exception ex)
                {}
                return true;
                }
            });


            OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(handleTime(newValue.toString()));
                    return true;
                }
            };
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            Preference start = (Preference) findPreference("startTime");
            start.setSummary(handleTime(mPrefs.getString("startTime", "23:00")));
            start.setOnPreferenceChangeListener(listener);

            Preference stop  = (Preference) findPreference("stopTime");
            stop.setSummary(handleTime(mPrefs.getString("stopTime", "09:00")));
            stop.setOnPreferenceChangeListener(listener);


            int sdk_int = android.os.Build.VERSION.SDK_INT;
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            if (sdk_int < 11)
            {
                PreferenceCategory categoryNotification = (PreferenceCategory) findPreference("categoryNotification");
                preferenceScreen.removePreference(categoryNotification);
            }

            final CheckBoxPreference checkboxPrefSensorOn = (CheckBoxPreference) findPreference("proxSensorOn");
            checkboxPrefSensorOn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {


                if (newValue.toString().equals("true"))
                {
                    //SetSensorProximity(true);
                    sensorOnChecked = true;
                }
                else
                {
                    //SetSensorProximity(false);
                    sensorOnChecked = false;
                }

                return true;
                }
            });

            sensorOnChecked = checkboxPrefSensorOn.isChecked();


            if (isSamsungPhoneWithTTS(getApplicationContext()) == false)
            {
                PreferenceCategory categoryNotification = (PreferenceCategory) findPreference("categoryServices");
                preferenceScreen.removePreference(categoryNotification);
            }

        }
        catch (Exception ex)
        {  }
    }

    public void onResume() {
        super.onResume();
        active = isMyServiceRunning();
        if(active) {
            service.setTitle(R.string.app_active);
            service.setSummary(R.string.app_deactive);
        }
        else {
            service.setTitle(R.string.app_inactive);
            service.setSummary(R.string.app_activate);
        }
    }

    protected void onPause() {
        super.onPause();
    }

    private String handleTime(String time) {
        String[] timeParts=time.split(":");
        int lastHour=Integer.parseInt(timeParts[0]);
        int lastMinute=Integer.parseInt(timeParts[1]);

        boolean is24HourFormat = DateFormat.is24HourFormat(this);

        if(is24HourFormat) {
            return ((lastHour < 10) ? "0" : "")
                    + Integer.toString(lastHour)
                    + ":" + ((lastMinute < 10) ? "0" : "")
                    + Integer.toString(lastMinute);
        } else {
            int myHour = lastHour % 12;
            return ((myHour == 0) ? "12" : ((myHour < 10) ? "0" : "") + Integer.toString(myHour))
                    + ":" + ((lastMinute < 10) ? "0" : "")
                    + Integer.toString(lastMinute)
                    + ((lastHour >= 12) ? " PM" : " AM");
        }
    }


    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);


        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

                if (DroidService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
        }
        return false;
    }

    }
