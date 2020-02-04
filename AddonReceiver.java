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

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by jeppe on 10/25/17.
 */

public class AddonReceiver extends ResultReceiver {

    private Receiver mReceiver;

    public AddonReceiver(Receiver receiver) {
        super(new Handler());
        mReceiver = receiver;
        //super();
    }

    public interface Receiver {
        /* Callback function invoked when connection to Wish Core is established */
        public void onConnected();
        /* Callback function invoked when connection to Wish Core is lost */
        public void onDisconnected();
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            if (resultCode == AddonService.BRIDGE_CONNECTED) {
                mReceiver.onConnected();
            }
            else if (resultCode == AddonService.BRIDGE_DISCONNECTED) {
                mReceiver.onDisconnected();
            }
        }
    }



}
