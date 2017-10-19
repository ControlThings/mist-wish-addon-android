package mist.node;

import android.content.Context;
import android.util.Log;

/**
 * Created by jan on 11/1/16.
 */

public class NodeModel {
    private String name;
    private Endpoint root;
    MistNode mistNode;

    public NodeModel(String name, Context context) {
        this.name = name;
        mistNode = MistNode.getInstance();

        /* Start mist app via JNI. */
        mistNode.startMistApp(name, context);

        /* FIXME Check that the mist app was started correctly */
    }

    private void addChildEndpoint(Endpoint ep) {
        mistNode.addEndpoint(ep);

        Endpoint epNext = ep.getNext();
        while (epNext != null) {
            mistNode.addEndpoint(epNext);

            if (epNext.getFirstChild() != null) {
                addChildEndpoint(epNext.getFirstChild());
            }

            epNext = epNext.getNext();
        }

    }

    public void setRootEndpoint(Endpoint root) {
        if (root != null) {
            Log.d("dev model", "root endpoint is not null, id is ");
        }
        this.root = root;

        Endpoint ep = root;
        do {
            mistNode.addEndpoint(ep);
            if (ep.getFirstChild() != null) {
                addChildEndpoint(ep.getFirstChild());
            }
            ep = ep.getNext();
        } while (ep != null);
    }

    public void close() {
        /* FIXME Here we will first de-register the endpoints and shutdown the C mist_app */

        /* Close the Mist Android service */

    }

}
