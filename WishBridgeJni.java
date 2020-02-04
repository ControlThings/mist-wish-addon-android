/**
 * Copyright (C) 2020, ControlThings Oy Ab
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * @license Apache-2.0
 */
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
    private byte[] wsid;

    public byte[] getWsid() {
        return wsid;
    }

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

    void setConnected(boolean status) {
        connected(status);
        addonService.connected(status);
        if (!status) {
            wishBridge.unbind();
        }
    }

}




