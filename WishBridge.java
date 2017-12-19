package addon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import fi.ct.bridge.AppBridge;
import fi.ct.bridge.CoreBridge;
import utils.Util;

class WishBridge {

    private final String TAG = "WishBridge";
    private final int TRANSACT_CODE_RELEASE_CORE = 1;

    Context _context;
    WishBridgeJni _jni;

    AddonService addonService;

    CoreBridge coreBridge = null;

    boolean mBound = false;

    /** This is set to true when startWish() has been invoked, and set to false in mConnection.onServiceStarted
     */
    boolean wishServiceStarting = false;

    Intent wish;

    private byte[] _wsid;

    WishBridge(Context context, WishBridgeJni jni, AddonService addonService) {
        this._context = context;
        this._jni = jni;
        this.addonService = addonService;

        //Note: Don't start wish service here, because we need to first get the wsid. Let WishBridgeJni start the service instead, after wsid is saved.
    }

    void startWish() {

        if (wishServiceStarting) {
            // startWish() is already starting Wish!
            return;
        }
        wishServiceStarting = true;

        wish = new Intent();
        wish.setComponent(new ComponentName("fi.ct.wish", "fi.ct.wish.Wish"));

        _wsid = _jni.getWsid();
        if (_wsid == null) {
            new AddonException("WSID is null!");
        }

        _context.startService(wish);
        _context.bindService(wish, mConnection, Context.BIND_AUTO_CREATE);
    }




    private class LocalBinder extends Binder {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Log.d(TAG, "onTransact, code: " + code);
            if (code == TRANSACT_CODE_RELEASE_CORE) {
                addonService.unbindService(mConnection);
                mConnection.onServiceDisconnected(null);
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            coreBridge = CoreBridge.Stub.asInterface(service);
            mBound = true;
            wishServiceStarting = false;
            try {
                coreBridge.register(new LocalBinder(), _wsid, bridge);
            } catch (RemoteException e) {
                Log.d(TAG, "remote exeption in open:");
            }
            _jni.setConnected(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            coreBridge = null;
            mBound = false;
            _jni.setConnected(false);
        }

    };

    /**
     * This method is called by the WishApp C99 when it needs to send a frame to Core
     *
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
            } else {
                Log.v(TAG, "wsid is null");
            }
            if (data != null) {
                //Log.v(TAG, "data len " + data.length);
            } else {
                Log.v(TAG, "data is null");
            }
            coreBridge.sendAppToCore(wsid, data);


        } catch (RemoteException e) {
            Log.v(TAG, "RemoteException occurred in receiveAppToCore");

            startWish();
        }
    }

    private AppBridge.Stub bridge = new AppBridge.Stub() {
        @Override
        public void sendCoreToApp(final byte[] data) {
            Log.d(TAG, "in sendCoreToApp");
            /* Enqueue the request on the app's main thread, implying that incoming messages from core
            to this app are executed in a serial fashion.
            If this behaviour is changed, then you must ensure that the callbacks are invoked
             */
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    _jni.receive_core_to_app(data);
                }
            });
        }
    };

    void unbind() {
        Log.d(TAG, "cleanup appBridge");
        if (mBound) {
            try {
                _context.unbindService(mConnection);
            } catch (IllegalArgumentException iae) {
                Log.d(TAG, Util.prettyPrintException(iae));
            }

            mBound = false;
        }

    }
}
