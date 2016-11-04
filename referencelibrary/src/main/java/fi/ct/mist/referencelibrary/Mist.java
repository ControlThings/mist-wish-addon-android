package fi.ct.mist.referencelibrary;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.os.ResultReceiver;
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
    }

    public void cleanup() {
        wishBridgeJni.cleanup();
    }

    public void onBridgeConected() {
        Log.d(TAG, "Bridge Conected");
        Bundle parameters = _intent.getExtras();
        ResultReceiver resultReceiver = parameters.getParcelable("receiver");
        resultReceiver.send(123, Bundle.EMPTY);
    }

    public void close() {
        cleanup();
    }
}




