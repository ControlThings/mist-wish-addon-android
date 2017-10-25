package mist;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import static mist.WishBridgeJni.BRIDGE_CONNECTED;
import static mist.WishBridgeJni.BRIDGE_DISCONNECTED;

/**
 * Created by jeppe on 10/25/17.
 */

public class MistReceiver extends ResultReceiver {

    private Receiver mReceiver;

    public MistReceiver(Receiver receiver) {
        super(new Handler());
        mReceiver = receiver;
        //super();
    }

    public interface Receiver {
        public void onConnected();
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            if (resultCode == BRIDGE_CONNECTED) {
                mReceiver.onConnected();
            }
        }
    }



}
