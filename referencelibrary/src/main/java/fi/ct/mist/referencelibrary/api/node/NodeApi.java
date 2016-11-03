package fi.ct.mist.referencelibrary.api.node;

import java.util.HashMap;
import java.util.Map;

public class NodeApi {

    private final String TAG = "DeviceApi";
    NodeApiJni _jni;

    private String _name = "Android";
    private String _url = null;

    Map<String, Endpoint> endpointMap = new HashMap<String, Endpoint>();

    public NodeApi(NodeApiJni jni) {
        this._jni = jni;
    }

    public void setName(String name) {
        this._name = name;
    }

    public void setCustomUiUrl(String url) {
        this._url = url;
    }

    public String getName() {
        return _name;
    }

    public String getUrl() {
        return _url;
    }

    public void addEndpoint(final Endpoint endpoint) {

        final String epName = endpoint._name;

        endpoint.registerCallback(new Endpoint.Callback() {
            @Override
            public void cb() {
                _jni.notifyValueChange(epName);
            }
        });

        endpointMap.put(epName, endpoint);
    }

    public Map<String, Endpoint> getEndpoints() {
        return endpointMap;
    }

    public Endpoint getEndpointByName(String name) {
        if (endpointMap.containsKey(name)) {
            return endpointMap.get(name);
        }
        return null;
    }

    public void deleteEndpoint(String name) {
        if (endpointMap.containsKey(name)){
            endpointMap.remove(name);
        }
    }
}
