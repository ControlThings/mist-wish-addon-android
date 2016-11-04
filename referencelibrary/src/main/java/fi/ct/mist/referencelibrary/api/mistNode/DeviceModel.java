package fi.ct.mist.referencelibrary.api.mistNode;

import android.util.Log;

/**
 * Created by jan on 11/1/16.
 */

public class DeviceModel {
    private String name;
    private Endpoint root;
    MistNodeApi mistNodeApi;

    public DeviceModel(String name) {
        this.name = name;
        mistNodeApi = MistNodeApi.getInstance();

        /* Start mist app via JNI. */
        mistNodeApi.startMistApp(name);

        /* FIXME Check that the mist app was started correctly */
    }

    public void addChildEndpoint(Endpoint parent) {

    }

    public void setRootEndpoint(Endpoint root) {
        if (root != null) {
            Log.d("dev model", "root endpoint is not null, id is ");
        }
        this.root = root;

        Endpoint ep = root;
        do {
            mistNodeApi.addEndpoint(ep);
            ep = ep.getNext();
        } while (ep != null);
    }

    public void close() {
        /* Here we will first de-register the endpoints and shutdown the C mist_app */

        /* Close the Mist Android service */

    }

}
