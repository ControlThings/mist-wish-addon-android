package fi.ct.mist.referencelibrary.bridge;

import android.content.Context;
import android.util.Log;

import fi.ct.mist.referencelibrary.Mist;


public class WishBridgeJni {

    static {
        System.loadLibrary("mist");
    }

    private final String TAG = "WishAppJni";

    WishBridge wishBridge;

    public WishBridgeJni(Context context, Mist mist) {
        Log.d(TAG, "MistBridge started");
        wishBridge = new WishBridge(context, this, mist);
        register(wishBridge);
    }

    /**
     * This function must be called to in order to make it possible to call WishAppBridge.receiveAppToCore from C code
     * @param wab
     */
    public native void register(WishBridge wab);

    public synchronized native void receive_core_to_app(byte buffer[]);


    public native void connected(boolean status);

    public void cleanup() {
        Log.d(TAG, "cleanup app");
       wishBridge.cleanup();
    }

}
