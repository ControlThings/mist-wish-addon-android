package mist.node;

import android.content.Context;
import android.util.Log;

/**
 * Created by jan on 11/1/16.
 */

public class NodeModel {
    private String name;
    private Endpoint root;
    MistNodeApi mistNodeApi;

    public NodeModel(String name, Context context) {
        this.name = name;
        mistNodeApi = MistNodeApi.getInstance();

        /* Start mist app via JNI. */
        mistNodeApi.startMistApp(name, context);

        /* FIXME Check that the mist app was started correctly */
    }

    public void addChildEndpoint(Endpoint parent) {
        /* FIXME unimplemented */
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
        /* FIXME Here we will first de-register the endpoints and shutdown the C mist_app */

        /* Close the Mist Android service */

    }

}