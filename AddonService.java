package addon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;


public abstract class AddonService extends Service {

    private final String TAG = "Mist Service";

    WishBridgeJni wishBridgeJni;
    AddonService addonServiceInstance;

    ResultReceiver addonReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "in onStartCommand");
        addonServiceInstance = this;

        if (intent.hasExtra("receiver") && intent.getParcelableExtra("receiver") instanceof ResultReceiver) {
            addonReceiver = intent.getParcelableExtra("receiver");
        }

        Log.d(TAG, "name: " + getBaseContext().getPackageName());


        String appName =  intent.getStringExtra("name");


        //MistNode.getInstance().startMistApp(getBaseContext());
        startAddon();


        new Thread(new Runnable() {
            @Override
            public void run() {
                wishBridgeJni = new WishBridgeJni(getBaseContext(), addonServiceInstance, addonReceiver);
            }
        }).start();

        return Service.START_NOT_STICKY;
    }

    public abstract void startAddon();

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




