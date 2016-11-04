package fi.ct.mist.referencelibrary.api.mistNode;

/**
 * Created by jan on 11/1/16.
 */

class MistNodeApi {

    private MistNodeApi() {}


    private static class MistNodeApiHolder {
        private static final MistNodeApi INSTANCE = new MistNodeApi();
    }

    public static MistNodeApi getInstance() {
        return MistNodeApiHolder.INSTANCE;
    }

    synchronized native void updateBool(String epName, boolean newValue);
    synchronized native void updateInt(String epName, int newValue);
    synchronized native void updateString(String epName, String newValue);
    synchronized native void updateFloat(String epName, double newValue);
    synchronized native void addEndpoint(Endpoint ep);
    synchronized native void startMistApp(String appName);



}
