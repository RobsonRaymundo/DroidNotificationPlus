package com.droid.notification;

import android.app.Activity;
import android.app.KeyguardManager;
import android.os.Bundle;
import android.view.WindowManager;

public class DroidBackground extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*
        getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_FULLSCREEN|
        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
*/
        super.onCreate(savedInstanceState);



    }

    private void timeSleep(int time)
    {
        try {
            Thread.sleep(time);
        }
        catch (Exception e) {}
    }



    public void onWindowFocusChanged(boolean hasFocus) {
        try
        {
            super.onWindowFocusChanged(hasFocus);
            if(hasFocus)
            {
                DroidService.timeOutBackground = true;
                timeSleep(1000);
            }
        }
        finally {
            finish();
        }
    }
}