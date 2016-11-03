package fi.ct.mist.referencelibrary;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fi.ct.mist.referencelibrary.api.node.NodeApi;
import fi.ct.mist.referencelibrary.api.node.NodeApiJni;
import fi.ct.mist.referencelibrary.bridge.BridgeConnection;
import fi.ct.mist.referencelibrary.bridge.WishBridgeJni;

public class Mist extends Service {

    private final String TAG = "Mist Service";

    WishBridgeJni wishBridgeJni;

    NodeApiJni nodeApiJni;

    Mist mistInstance;
    private IBinder apiHandler = new ApiHandler();
    private boolean binded = false;
    List<BridgeConnection.Connection> connectionList;

    Intent _intent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "in onStartCommand");
        mistInstance = this;

        this._intent = intent;

        connectionList = new ArrayList<BridgeConnection.Connection>();

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
        Log.d(TAG, "in onCreate");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "in onBind" + intent);
        IBinder bridgeBinder = new BridgeBinder(binded);
        return bridgeBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "in onUnbind: ");
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "in onDestroy");
        super.onDestroy();
    }

    public void cleanup() {
        nodeApiJni.cleanup();
        wishBridgeJni.cleanup();
    }

    public void onBridgeConected() {
        Log.d(TAG, "Bridge Conected");
        nodeApiJni = new NodeApiJni();
      /*  if (!binded) {
            for (BridgeConnection.Connection connection : connectionList) {
                connection.BridgeConnected();
            }
            binded = true;
         }*/
        onHandleIntent(_intent);
    }

    protected void onHandleIntent(Intent intent) {
        Bundle parameters = intent.getExtras();
        ResultReceiver resultReceiver = parameters.getParcelable("receiver");
        resultReceiver.send(123, Bundle.EMPTY);
    }

    public class BridgeBinder extends Binder {

        public boolean binded;

        public BridgeBinder(boolean binded) {
            this.binded = binded;
        }

        public void listen(BridgeConnection.Connection connection) {
            connectionList.add(connection);
        }

        public void removeConnection(BridgeConnection.Connection connection) {
            if (connectionList.contains(connection)){
                connectionList.remove(connection);
            }
        }

        public IBinder getApiHandler() {
            return apiHandler;
        }

    }

    public class ApiHandler extends Binder {
        public NodeApi getNodeApi() {
            return nodeApiJni.getNodeApi();
        }

        public void close() {
            cleanup();
        }
    }


}

