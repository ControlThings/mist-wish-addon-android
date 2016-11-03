package fi.ct.mist.referencelibrary.bridge;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import fi.ct.mist.referencelibrary.Mist;

/**
 * Created by jeppe on 11/2/16.
 */

public class BridgeConnection implements ServiceConnection {

    Mist.BridgeBinder binder;
    BridgeServiceConnection _serviceConnection;

    private ComponentName _componentName;
    private  boolean bridgeBinded;

    public BridgeConnection(BridgeServiceConnection conn) {
        this._serviceConnection = conn;
    }

    private Connection connection = new Connection() {
        @Override
        public void BridgeConnected() {
            _serviceConnection.onBridgeConnected(_componentName, binder.getApiHandler());
            binder.removeConnection(connection);
        }
    };

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (Mist.BridgeBinder) service;
        if (!binder.binded) {
            this._componentName = name;
            binder.listen(connection);
        } else {
            _serviceConnection.onBridgeConnected(name, binder.getApiHandler());
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        _serviceConnection.onBridgeDisconnected(name);
        binder.removeConnection(connection);
    }


    public interface BridgeServiceConnection {

        public void onBridgeConnected(ComponentName name, IBinder apiHandler);

        public void onBridgeDisconnected(ComponentName name);

    }

    public interface Connection {
        public void BridgeConnected();
    }

}
