package fi.ct.mist.referencelibrary.bridge;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import fi.ct.bridge.AppBridge;
import fi.ct.bridge.CoreBridge;
import fi.ct.mist.referencelibrary.Mist;


public class WishBridge {

    private final String TAG = "WishBridge";

    Context _context;
    WishBridgeJni _jni;

    Mist _mist;
   // WishBridge wishBridge = null;

    CoreBridge coreBridge = null;

    boolean mBound = false;

    Intent wish;

    private void startWish() {
        wish = new Intent();
        wish.setComponent(new ComponentName("fi.ct.wish", "fi.ct.wish.Wish"));
        _context.startService(wish);
        this._context.bindService(wish, mConnection, 0); //Context.BIND_AUTO_CREATE
    }

    public WishBridge(Context context, WishBridgeJni jni, Mist mist) {
        this._context = context;
        this._jni = jni;
        this._mist = mist;

        startWish();
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            coreBridge = CoreBridge.Stub.asInterface(service);
            mBound = true;
            try {
                coreBridge.open(bridge);
            } catch (RemoteException e) {
                Log.d(TAG, "remote exeption in open:");
            }
            _mist.onBridgeConected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            coreBridge = null;
            mBound = false;
        }
    };

    /**
     * This method is called by the WishApp C99 when it needs to send a frame to Core
     * @param data
     */
    public void receiveAppToCore(byte[] wsid, byte[] data) {
        Log.v(TAG, "in receiveAppToCore");
        if (!mBound) {
            Log.v(TAG, "Error: not bound, attempting rebound");

            startWish();
            return;
        }
        try {
            if (wsid != null) {
              //  Log.v(TAG, "wsid len " + wsid.length);
            }
            else {
                Log.v(TAG, "wsid is null");
            }
            if (data != null) {
                //Log.v(TAG, "data len " + data.length);
            }
            else {
                Log.v(TAG, "data is null");
            }
            coreBridge.sendAppToCore(wsid, data);


        } catch (RemoteException e) {
            //e.printStackTrace();
            Log.v(TAG, "RemoteException occurred in receiveAppToCore");
        }
    }

    private AppBridge.Stub bridge = new AppBridge.Stub() {
        @Override
        public void sendCoreToApp(byte[] data)  {
            Log.d(TAG, "in sendCoreToApp");
            _jni.receive_core_to_app(data);
        }
    };

    public void cleanup() {
       // _context.stopService(wish);
        //_context.unbindService(wish);
        Log.d(TAG, "cleanup appBridge");
        if (mBound) {
            _context.unbindService(mConnection);
            mBound = false;
        }

    }
}
