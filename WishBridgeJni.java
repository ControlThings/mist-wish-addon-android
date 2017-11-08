package addon;

import android.content.Context;
import android.os.ResultReceiver;
import android.util.Log;


class WishBridgeJni {

    static {
        System.loadLibrary("mist");
    }

    private final String TAG = "WishAppJni";

    WishBridge wishBridge;
    byte[] wsid;

    ResultReceiver receiver;

    AddonService addonService;

    WishBridgeJni(Context context, AddonService addonService) {
        Log.d(TAG, "WishBridge started");

        this.addonService = addonService;
        wishBridge = new WishBridge(context, this, addonService);
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

    void setConnected() {
        connected(true);
        addonService.connected(true);
    }

    void disconnect() {
        connected(false);
        wishBridge.unbind();
        addonService.connected(false);
    }

}




