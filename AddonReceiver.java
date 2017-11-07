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
        public void onConnected();
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            if (resultCode == WishBridgeJni.BRIDGE_CONNECTED) {
                mReceiver.onConnected();
            }
        }
    }



}
