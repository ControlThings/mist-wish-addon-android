package addon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public abstract class AddonService extends Service {

    private final String TAG = "Mist Service";

    static final int BRIDGE_CONNECTED = 32;
    static final int BRIDGE_DISCONNECTED = 33;

    WishBridgeJni wishBridgeJni;

    List<ResultReceiver> addonReceiverList = new ArrayList<>();

    boolean connectedStatus;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "in onStartCommand");

        if (intent.hasExtra("receiver") && intent.getParcelableExtra("receiver") instanceof ResultReceiver) {
            ResultReceiver addonReceiver = intent.getParcelableExtra("receiver");
            addonReceiverList.add(addonReceiver);
            if (connectedStatus) {
                addonReceiver.send(BRIDGE_CONNECTED, null);
            }
        }

        return Service.START_NOT_STICKY;
    }

    public abstract void startAddon();

    public void connected(boolean status) {

        for (ResultReceiver addonReceiver : addonReceiverList) {
            if (addonReceiver != null) {
                if (status) {
                    addonReceiver.send(BRIDGE_CONNECTED, null);
                } else {
                    addonReceiver.send(BRIDGE_DISCONNECTED, null);
                }

            }
        }

        connectedStatus = status;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startAddon();
        wishBridgeJni = new WishBridgeJni(getBaseContext(), this);

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
        wishBridgeJni.setConnected(false);
    }
}




