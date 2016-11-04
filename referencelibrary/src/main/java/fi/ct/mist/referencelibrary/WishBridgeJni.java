package fi.ct.mist.referencelibrary;

import android.content.Context;
import android.util.Log;


class WishBridgeJni {

    static {
        System.loadLibrary("mist");
    }

    private final String TAG = "WishAppJni";

    WishBridge wishBridge;

    WishBridgeJni(Context context, Mist mist) {
        Log.d(TAG, "MistBridge started");
        wishBridge = new WishBridge(context, this, mist);
        register(wishBridge);
    }

    /**
     * This function must be called to in order to make it possible to call WishAppBridge.receiveAppToCore from C code
     * @param wab
     */
     native void register(WishBridge wab);
     native void receive_core_to_app(byte buffer[]);
     native void connected(boolean status);

     void cleanup() {
        Log.d(TAG, "cleanup app");
       wishBridge.cleanup();
    }

}




