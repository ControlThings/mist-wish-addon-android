package mist;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class Mist extends Service {

    private final String TAG = "Mist Service";

    WishBridgeJni wishBridgeJni;
    Mist mistInstance;
    Intent _intent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "in onStartCommand");

        mistInstance = this;
        this._intent = intent;

        new Thread(new Runnable() {
            @Override
            public void run() {
                wishBridgeJni = new WishBridgeJni(getBaseContext(), mistInstance);
            }
        }).start();

        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wishBridgeJni.disconnect();
    }
}




