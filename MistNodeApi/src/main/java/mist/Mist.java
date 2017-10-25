package mist;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import mist.node.MistNode;


public class Mist extends Service {

    private final String TAG = "Mist Service";

    WishBridgeJni wishBridgeJni;
    Mist mistInstance;

    ResultReceiver mistReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "in onStartCommand");
        mistInstance = this;

        if (intent.hasExtra("receiver") && intent.getParcelableExtra("receiver") instanceof ResultReceiver) {
            mistReceiver = intent.getParcelableExtra("receiver");
        }

        Log.d(TAG, "name: " + getBaseContext().getPackageName());


        String appName =  intent.getStringExtra("name");


        MistNode.getInstance().startMistApp(getBaseContext());

        new Thread(new Runnable() {
            @Override
            public void run() {
                wishBridgeJni = new WishBridgeJni(getBaseContext(), mistInstance, mistReceiver);
            }
        }).start();

        return Service.START_STICKY;
    }

    private void connected() {

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




