package mist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import fi.ct.bridge.AppBridge;
import fi.ct.bridge.CoreBridge;

class WishBridge {

    private final String TAG = "WishBridge";
    private final int releaseCore = 1;

    Context _context;
    WishBridgeJni _jni;

    Mist _mist;
    // WishBridge wishBridge = null;

    CoreBridge coreBridge = null;

    boolean mBound = false;

    Intent wish;

    private byte[] _wsid;

    void startWish() {

        /*
        // download wish app if it dosn't exist.
        try {
            _context.getPackageManager().getPackageInfo("fi.ct.wish", PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {




            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("Android/data/com.mycompany.android.games/temp/temp.apk"), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!

            try {
                PendingIntent contentIntent = PendingIntent.getActivity(_context, 0, intent, 0);
                contentIntent.send();
            } catch (PendingIntent.CanceledException err) {
                Log.d(TAG, "error: " + err);
            }



            //_context.startActivity(intent);
        }
*/

        wish = new Intent();
        wish.setComponent(new ComponentName("fi.ct.wish", "fi.ct.wish.Wish"));
        /* Save WSID to the Intent, so that Wish core's end of the bridge can see the difference between Intent's of different Wish services */
        if (_jni.wsid != null) {
            //wish.putExtra("EXTRA_WSID", _jni.wsid);
            _wsid = _jni.wsid;
        } else {
            Log.d(TAG, "WARNING FAIL cannot add EXTRA_WSID because wsid is null!");
        }
        _context.startService(wish);
        this._context.bindService(wish, mConnection, 0); //Context.BIND_AUTO_CREATE
    }

    WishBridge(Context context, WishBridgeJni jni, Mist mist) {
        this._context = context;
        this._jni = jni;
        this._mist = mist;

        //startWish();  //Note: Don't start wish service here, because we need to first get the wsid. Let WishBridgeJni start the service instead, after wsid is saved.
    }


    private class LocalBinder extends Binder {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == releaseCore) {
                _mist.unbindService(mConnection);
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            coreBridge = CoreBridge.Stub.asInterface(service);
            mBound = true;
            try {
                coreBridge.register(new LocalBinder(), _wsid, bridge);
            } catch (RemoteException e) {
                Log.d(TAG, "remote exeption in open:");
            }
            _jni.connected(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            coreBridge = null;
            mBound = false;
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
            //e.printStackTrace();
            Log.v(TAG, "RemoteException occurred in receiveAppToCore");
        }
    }

    private AppBridge.Stub bridge = new AppBridge.Stub() {
        @Override
        public void sendCoreToApp(final byte[] data) {
            Log.d(TAG, "in sendCoreToApp");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    _jni.receive_core_to_app(data);
                }
            }).start();
        }
    };

    void unbind() {
        Log.d(TAG, "cleanup appBridge");
        if (mBound) {
            _context.unbindService(mConnection);
            mBound = false;
        }

    }
}
