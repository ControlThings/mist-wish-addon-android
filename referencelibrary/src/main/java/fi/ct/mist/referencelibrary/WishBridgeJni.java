package fi.ct.mist.referencelibrary;

import android.content.Context;
import android.util.Log;


class WishBridgeJni {

    static {
        System.loadLibrary("mist");
    }

    private final String TAG = "WishAppJni";

    WishBridge wishBridge;
    byte[] wsid;

    WishBridgeJni(Context context, Mist mist) {
        Log.d(TAG, "MistBridge started");
        wishBridge = new WishBridge(context, this, mist);
        this.wsid = register(wishBridge);
        if (this.wsid == null) {
            Log.d(TAG, "this.wsid is null!");
        }
        wishBridge.startWish();
    }

    /**
     * This function must be called to in order to make it possible to call WishAppBridge.receiveAppToCore from C code
     * @param wab
     */
    native byte[] register(WishBridge wab);

    native void receive_core_to_app(byte buffer[]);
    native void connected(boolean status);

    void disconnect() {
        connected(false);
        wishBridge.unbind();
    }

}




