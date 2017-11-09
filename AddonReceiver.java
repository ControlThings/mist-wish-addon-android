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
