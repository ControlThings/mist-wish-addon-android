package addon;

import android.content.Context;
import android.os.ResultReceiver;
import android.util.Log;


class WishBridgeJni {

    static final int BRIDGE_CONNECTED = 32;

    static {
        System.loadLibrary("mist");
    }

    private final String TAG = "WishAppJni";

    WishBridge wishBridge;
    byte[] wsid;

    ResultReceiver receiver;

    WishBridgeJni(Context context, MistService mistService, ResultReceiver receiver) {
        Log.d(TAG, "MistBridge started");
        this.receiver = receiver;
        wishBridge = new WishBridge(context, this, mistService);
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

    void isConnected() {
        connected(true);
        if (receiver != null) {
            receiver.send(BRIDGE_CONNECTED, null);
        }
    }

    void disconnect() {
        connected(false);
        wishBridge.unbind();
    }

}




