package com.droid.notification;

import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.*;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.WindowManager;

import java.util.Collections;
import java.util.List;

public class DroidActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        try {

            super.onCreate(savedInstanceState);

            AppLoader task = new AppLoader();
            task.execute();

        } catch (Exception ex) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            MenuInflater inflater = getMenuInflater();

        } catch (Exception ex) {
        }

        return true;
    }

    private class AppLoader extends AsyncTask<Void, Void, Void> {
        ProgressDialog loadingDialog;
        PreferenceScreen root;

        protected void onPreExecute() {
            try {
                CharSequence csMessage;
                try {
                    csMessage = getString(R.string.app_loading);
                } catch (Exception ex) {
                    csMessage = "Loading ...";
                }
                loadingDialog = ProgressDialog.show(DroidActivity.this, "", csMessage, true);
            } catch (Exception ex) {
            }
        }


        @Override
        protected Void doInBackground(Void... nothing) {
            try {
                // Root
                root = getPreferenceManager().createPreferenceScreen(DroidActivity.this);

                // Inline preferences
                PreferenceCategory inlinePrefCat = new PreferenceCategory(DroidActivity.this);
                inlinePrefCat.setTitle(R.string.app_apps);


                root.addPreference(inlinePrefCat);

                final PackageManager pm = getPackageManager();
                //get a list of installed apps
                List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(pm));

                for (ApplicationInfo packageInfo : packages) {
                    // Checkbox preference
                    if (
                            packageInfo.packageName.contentEquals("com.icq.mobile.client") || // ICQ
                                    packageInfo.packageName.contentEquals("com.appturbo.appturboBR") || // AppDoDia
                                    packageInfo.packageName.contentEquals("com.tencent.mm") || // WeChat
                                    packageInfo.packageName.contentEquals("jp.naver.line.android") || // Line
                                    packageInfo.packageName.contentEquals("com.ebuddy.android") || // eBuddy Messenger
                                    packageInfo.packageName.contentEquals("com.nimbuzz") || // Nimbuzz
                                    packageInfo.packageName.contentEquals("com.imo.android.imoim") || // Imo Messenger
                                    packageInfo.packageName.contentEquals("com.bsb.hike") || // Hike
                                    packageInfo.packageName.contentEquals("com.sec.chaton") ||
                                    packageInfo.packageName.contentEquals("com.android.email") ||
                                    packageInfo.packageName.contentEquals("com.google.android.email") || // Emal Nexus 4
                                    packageInfo.packageName.contentEquals("com.google.android.talk") ||
                                    packageInfo.packageName.contentEquals("com.facebook.katana") ||
                                    packageInfo.packageName.contentEquals("com.facebook.orca") ||
                                    packageInfo.packageName.contentEquals("com.google.android.gm") ||
                                    //packageInfo.packageName.contentEquals("com.android.vending") ||
                                    packageInfo.packageName.contentEquals("com.google.android.apps.plus") ||
                                    packageInfo.packageName.contentEquals("com.hotmail.Z7") ||
                                    //packageInfo.packageName.contentEquals("com.linkedin.android") ||
                                    packageInfo.packageName.contentEquals("com.android.mms") ||
                                    packageInfo.packageName.contentEquals("com.android.calendar") ||
                                    packageInfo.packageName.contentEquals("com.skype.raider") ||
                                    //packageInfo.packageName.contentEquals("com.android.phone") ||
                                    packageInfo.packageName.contentEquals("com.twitter.android") ||
                                    packageInfo.packageName.contentEquals("com.viber.voip") ||
                                    packageInfo.packageName.contentEquals("com.whatsapp") ||
                                    packageInfo.packageName.contentEquals("org.telegram.messenger") ||
                                    packageInfo.packageName.contentEquals("com.yahoo.mobile.client.android.mail") ||
                                    packageInfo.packageName.contentEquals("com.yahoo.mobile.client.android.im") ||
                                    packageInfo.packageName.contentEquals("com.google.android.apps.orkut"))

                    {
                        CheckBoxPreference checkboxPref = new CheckBoxPreference(DroidActivity.this);
                       /*
                        ListPreference lp = new ListPreference(DroidActivity.this);
                        CharSequence[] entries = { "Verde", "Azul", "Amarelo" };
                        CharSequence[] entryValues = { "1", "2", "3" };
                        lp.setEntries(entries);
                        lp.setEntryValues(entryValues);
                        */


                        if (packageInfo.packageName.contentEquals("com.google.android.talk") && (packageInfo.taskAffinity.equals("android.task.googletalk"))) {
                            checkboxPref.setKey("com.google.android.gsf"); // GTalk - Estrutura dos servicos do Google
                        } else {
                            checkboxPref.setKey(packageInfo.packageName);
                        }

                        if (packageInfo.packageName.contentEquals("com.android.mms")) {
                            checkboxPref.setTitle("SMS");
                        } else if (packageInfo.packageName.contentEquals("com.facebook.orca")) {
                            checkboxPref.setTitle("Facebook Messenger");
                        } else {
                            checkboxPref.setTitle(packageInfo.loadLabel(pm));
                        }


                        inlinePrefCat.addPreference(checkboxPref);
                        //inlinePrefCat.addPreference(lp);


                    }
                }
            } catch (Exception ex) {
            }
            return null;
        }

        protected void onPostExecute(Void nothing) {
            try {
                setPreferenceScreen(root);
                loadingDialog.cancel();
            } catch (Exception ex) {
            }
        }

    }

}
