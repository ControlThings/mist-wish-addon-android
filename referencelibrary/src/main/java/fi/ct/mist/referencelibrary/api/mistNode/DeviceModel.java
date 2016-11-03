package fi.ct.mist.referencelibrary.api.mistNode;

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

        /* Start mist app via JNI. Save the mist_app_handle returned by JNI. */

    }

    public void addChildEndpoint(Endpoint parent) {

    }

    public void setRootEndpoint(Endpoint root) {
        this.root = root;

        Endpoint ep = root;
        do {
            mistNodeApi.addEndpoint(ep);
        } while (ep.getNext() != null);
    }

}
