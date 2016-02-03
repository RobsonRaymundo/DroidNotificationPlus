package com.droid.notification;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.accessibilityservice.AccessibilityServiceInfo;

@SuppressLint("NewApi")
public class DroidService extends AccessibilityService implements SensorEventListener, AudioManager.OnAudioFocusChangeListener {
    static int sdk_int = android.os.Build.VERSION.SDK_INT;
    private int timeNotification;
    private boolean checkSensorProx;
    private boolean checkSensorGesture;
    private boolean checkBrightMax;
    private boolean checkExpand;
    public static boolean timeOutBackground;
    private int intAudioFocusChange = -1;
    public static boolean waitingTimeOutSensorGesture;
    public static boolean waitingTimeOutSound;
    public static boolean waitingTimeOutNofitication;
    public static boolean closeSensorProximity;
    public static boolean openSensorProximity;
    public static boolean currentCloseSensorProximity;
    private static int timeOutScreenDisplay;
    private static boolean enterLoopingWaitingTimeOutSound;

    public static boolean waitingTimeOutSensorAlwaysOn;
    public static boolean newNotification;
    private SensorManager sensorManager;

    @Override
    public void onServiceConnected() {

        super.onServiceConnected();

        if (sdk_int < 16)
        {
            AccessibilityServiceInfo localAccessibilityServiceInfo = new AccessibilityServiceInfo();
            localAccessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
            localAccessibilityServiceInfo.feedbackType = 16;
            localAccessibilityServiceInfo.notificationTimeout = 0L;
            setServiceInfo(localAccessibilityServiceInfo);
        }
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        newNotification = false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        timeOutScreenDisplay = 0;
        enterLoopingWaitingTimeOutSound = false;
        if (waitingTimeOutSensorGesture == false && waitingTimeOutSound == false && waitingTimeOutNofitication == false)
        {
            newNotification = true;
            try {
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                String packageName = (String) event.getPackageName();
                checkSensorProx = mPrefs.getBoolean("proxSensor", false);
                checkSensorGesture = mPrefs.getBoolean("proxSensorGesture", false);

                if(mPrefs.getBoolean(packageName, false)) {
                    if(shouldTurnOnScreen(mPrefs)) {
                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        if(!pm.isScreenOn() || packageName.contains("com.android.mms")) {
                            postMessageInThread();
                        }
                    }
                }
            }
            finally {
                timeSleep(1000);
                newNotification = false;
            }
        }
    }

    private boolean shouldTurnOnScreen(SharedPreferences mPrefs) {
        if(mPrefs.getBoolean("quiet", false)) {
            String startTime = mPrefs.getString("startTime", "23:00");
            String stopTime = mPrefs.getString("stopTime", "09:00");
            boolean turnOnScreen = true;

            SimpleDateFormat sdfDate = new SimpleDateFormat("H:mm");
            String currentTimeStamp = sdfDate.format(new Date());
            int currentHour = Integer.parseInt(currentTimeStamp.split("[:]+")[0]);
            int currentMinute = Integer.parseInt(currentTimeStamp.split("[:]+")[1]);

            int startHour  = Integer.parseInt(startTime.split("[:]+")[0]);
            int startMinute = Integer.parseInt(startTime.split("[:]+")[1]);

            int stopHour = Integer.parseInt(stopTime.split("[:]+")[0]);
            int stopMinute = Integer.parseInt(stopTime.split("[:]+")[1]);

            if(startHour < stopHour && currentHour > startHour && currentHour < stopHour)
                turnOnScreen = false;
            else if (startHour > stopHour && (currentHour > startHour || currentHour < stopHour))
                turnOnScreen = false;
            else if(currentHour == startHour && currentMinute >= startMinute)
                turnOnScreen = false;
            else if(currentHour == stopHour && currentMinute < stopMinute)
                turnOnScreen = false;

            return turnOnScreen;
        }
        return true;
    }
    
    private void expandCollapseStatusBar()
    {
        String typeStatus ="expand";

        if (sdk_int >= 17)
        {
            typeStatus ="expandNotificationsPanel";
        }
    	try
 		{
 			Object service = (Object) getSystemService ("statusbar");
 			Class <?> statusBarManager = Class.forName ("android.app.StatusBarManager");
 			Method expand = statusBarManager.getMethod (typeStatus);
 			expand.invoke (service);
            timeSleep(300);

         } catch (Exception e) {}
    }  
    
    private void timeSleep(int time)
    {
        try {
            Thread.sleep(time);
        }
        catch (Exception e) {}
    }

    private boolean isKeyguardLockedAllVersion(KeyguardManager km)
    {
        boolean keyLocker = false;
        try
        {

            if (sdk_int >=16)
            {       // Retornar se a proteção do teclado requer uma senha para desbloquear
                if (km.isKeyguardSecure ())
                {
                    // Retornar se o bloqueio do teclado está bloqueado
                    if (km.isKeyguardLocked())
                    {
                        keyLocker = true;
                    }
                }
            }
            else
            {
               // Se a tela do teclado está mostrando ou no modo de entrada restrito chave
                if (km.inKeyguardRestrictedInputMode())
                {
                    keyLocker = true;
                }
            }
        }
        catch(Exception e) 	{}
        return keyLocker;
    }

    private void ShowDroidBackground()
    {
        timeSleep(100);
        Intent dialogIntent = new Intent(this, DroidBackground.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //dialogIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        //dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);


        this.startActivity(dialogIntent);
    }

    private void ShowDroidBrackground(KeyguardManager km)
    {
        if (sdk_int >=16)
        {
            // Retornar se a proteção do teclado requer uma senha para desbloquear
            if (km.isKeyguardSecure ())
            {
                ShowDroidBackground();
            }
        }
        else
        {
            if (km.inKeyguardRestrictedInputMode())
            {
                ShowDroidBackground();
            }
        }
    }

    private void waitingTimeOutNotification(PowerManager pm, KeyguardManager km, int timeOutNotification)
    {
        try
        {
            double timeOutScreen = 0;
            timeOutBackground = false;
            waitingTimeOutNofitication = true;
            while (pm.isScreenOn())
            {
                timeOutScreen = timeOutScreen + 0.5;
                if ((timeOutScreen > timeOutNotification) || (timeOutBackground == true)) {
                    break;
                }

                if (sdk_int >= 16)
                {
                    if (km.isKeyguardLocked() == false)
                    {
                        break;
                    }
                }
                else
                {
                    if (km.inKeyguardRestrictedInputMode() == false)
                    {
                        break;
                    }
                }
                timeSleep(50);
            }
        }
        catch(Exception e)
        {
        }
        finally {
            waitingTimeOutNofitication = false;
        }
    }


    @SuppressWarnings("deprecation")
    private void turnOnScreen() {

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        PowerManager.WakeLock wl = null;
        checkBrightMax = mPrefs.getBoolean("brightMax", false);
        if (checkBrightMax)
        {
            wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "DroidNotification");
        }
        else
        {
            wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "DroidNotification");
        }
        KeyguardManager km = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);

        int currentTimeOut = 0;
        String timeNotificationPref = mPrefs.getString("timeNotification", "10");
        timeNotification  = Integer.parseInt(timeNotificationPref) * 10;
        int timeOutScreen = timeNotification * 100;
        KeyguardManager.KeyguardLock keyguardLock = null;

        try
        {
            if (sdk_int >=17)
            {
                currentTimeOut = getTimeout();
                setTimeout(timeOutScreen, currentTimeOut);
            }
            if (isKeyguardLockedAllVersion(km))
            {
                keyguardLock = km.newKeyguardLock(KEYGUARD_SERVICE);
                keyguardLock.disableKeyguard();
                timeSleep(100);
            }


            WaitingTimeOutSound();
            ShowDroidBackground();
            wl.acquire();
            expandCollapseStatusBar();
          //  ShowDroidBrackground(km);

            waitingTimeOutNotification(pm, km, timeNotification);
        }
        finally
        {
            try
            {
                if (wl.isHeld())
                {
                    wl.release();
                }
                setTimeout(currentTimeOut, timeOutScreen);
            }
            catch (Exception ex){
            }
            if (keyguardLock != null)
            {
                timeSleep(100);
            //    keyguardLock.reenableKeyguard();
            }
        }
    }

    private int getTimeout() {
        int screenTimeout = 0;
        try
        {
            screenTimeout = android.provider.Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, 15000);
        }
        catch (Exception ex)
        {
        }
        return screenTimeout;
    }

    private void setTimeout(int screenTimeOut, int currentTimeOut) {
        try
        {
            if (screenTimeOut > 0)
            {
                if (screenTimeOut != currentTimeOut)
                {
                    android.provider.Settings.System.putInt(getContentResolver(),
                            Settings.System.SCREEN_OFF_TIMEOUT, screenTimeOut);



                }
            }
        }
        catch (Exception ex)
        {
        }
    }

    @Override
    public void onInterrupt() {
        
    }

    private void EnabledSensorPriximity()
    {
        if (checkSensorGesture || checkSensorProx)
        {
            SetSensorProximity(true);
        }
    }

    private void DisabledSensorPriximity()
    {
        if (checkSensorGesture || checkSensorProx)
        {
            SetSensorProximity(false);
        }
    }

    private void LoopingTimeOutSensor()
    {
        boolean turnOnScreenNow = false;
        try {
            closeSensorProximity = false;
            openSensorProximity = false;
            EnabledSensorPriximity();
            int timeOutNotificationSensor = 72000; // 6000 =  10 minutos   (600 = 1 minuto)
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            waitingTimeOutSensorGesture = true;

            while (pm.isScreenOn() == false && turnOnScreenNow == false)
            {
                if (currentCloseSensorProximity)
                {
                    if (checkSensorProx == false)
                    {
                        turnOnScreenNow = true;
                    }
                }
                else
                {
                    if (checkSensorGesture)
                    {
                        if (closeSensorProximity && openSensorProximity)
                        {
                            turnOnScreenNow = true;
                        }
                    }
                    else
                    {
                        {
                            turnOnScreenNow = true;
                        }
                    }
                }

                timeOutScreenDisplay = timeOutScreenDisplay + 1;
                if (timeOutScreenDisplay > timeOutNotificationSensor ) {
                    break;
                }
                timeSleep(50);
            }
        }
        finally {
            waitingTimeOutSensorGesture = false;
            DisabledSensorPriximity();
        }

        if (turnOnScreenNow)
        {
            turnOnScreen();
        }
    }


    //implementation:
    private void postMessageInThread()  {
        Thread t = new Thread()  {
            @Override
            public void run()  {
                if (checkSensorGesture || checkSensorProx)
                {
                    LoopingTimeOutSensor();
                }
                else
                {
                    turnOnScreen();
                }
            }
        };
        t.start();
    }

    public void SetSensorProximity(boolean turnOn)
    {
        try {

            if (turnOn && sensorManager == null)
            {
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

                if(proximitySensor != null )
                {
                    sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
                    timeSleep(1000);
                }
            }

            if (turnOn == false && sensorManager != null)
            {
                sensorManager.unregisterListener(this);
              //  timeSleep(700);
                sensorManager = null;
            }

        }
        catch (Exception ex)
        {
            String msg = ex.getMessage();

        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if(event.values[0] < event.sensor.getMaximumRange()) {
                closeSensorProximity = true;
                currentCloseSensorProximity = true;
            } else {
                openSensorProximity = true;
                currentCloseSensorProximity = false;
            }
        }

        if (DroidNotify.sensorOnChecked && DroidService.currentCloseSensorProximity)
        {
            if (DroidService.waitingTimeOutSensorGesture == false && DroidService.waitingTimeOutSound == false && DroidService.waitingTimeOutNofitication == false && waitingTimeOutSensorAlwaysOn == false)
            {
                postMessageInThreadSensorAlwaysOn();
            }
        }
    }

    private void LoopingSensorAlwaysOn()
    {
        boolean turnOnScreenNow = false;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        try {
            DroidService.openSensorProximity = false;

            waitingTimeOutSensorAlwaysOn = true;
            int timeOutNotificationSensor = 20; // 6000 =  10 minutos   (600 = 1 minuto)
            int timeOutScreenDisplaySensorAlwaysOn = 0;
            while (DroidService.currentCloseSensorProximity)
            {
                timeOutScreenDisplaySensorAlwaysOn = timeOutScreenDisplaySensorAlwaysOn + 1;
                if (timeOutScreenDisplaySensorAlwaysOn > timeOutNotificationSensor ) {
                    turnOnScreenNow = true;
                }
                timeSleep(100);
                if (newNotification) break;
            }

            if (newNotification == false)
            {
                if (pm.isScreenOn() == false && turnOnScreenNow && DroidService.currentCloseSensorProximity == false)
                {
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "DroidNotification");
                    wl.acquire();
                    timeSleep(4000);
                    wl.release();
                }
            }
        }
        finally {
            waitingTimeOutSensorAlwaysOn = false;
        }

    }

    private void postMessageInThreadSensorAlwaysOn()
    {
        Thread t = new Thread()  {
            @Override
            public void run()  {
                LoopingSensorAlwaysOn();
            }
        };
        t.start();

    }



    @Override
    public void onAudioFocusChange(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
        enterLoopingWaitingTimeOutSound = true;
        intAudioFocusChange = i;
        //timeSleep(300);
    }


    private void WaitingTimeOutSound()
    {
        int timeOutSound = 0;
        try
        {
            try
            {
                waitingTimeOutSound = true;
                while (intAudioFocusChange < 0 && timeOutSound < 100 )
                {
                    if (enterLoopingWaitingTimeOutSound == false && timeOutSound == 3)
                    {
                        break;
                    }
                    timeSleep(100);
                    timeOutSound  =  timeOutSound + 1;
                }
            }
            catch (Exception ex)
            {
            }
        }
        finally {
            intAudioFocusChange = -1;
            waitingTimeOutSound = false;
            enterLoopingWaitingTimeOutSound = false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // REGISTER RECEIVER THAT HANDLES SCREEN ON AND SCREEN OFF LOGIC
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new DroidScreenReceiver();
        registerReceiver(mReceiver, filter);
    }



    @Override
    public void onStart(Intent intent, int startId) {
        if (DroidNotify.sensorOnChecked && waitingTimeOutSensorGesture == false && waitingTimeOutSound == false && waitingTimeOutNofitication == false)
        {
            boolean screenOff = intent.getBooleanExtra("screen_state", false);
            if (screenOff) {
                SetSensorProximity(true);
            }
            else
            {
                SetSensorProximity(false);
            }

        }
    }
}